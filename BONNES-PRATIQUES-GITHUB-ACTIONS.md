# 📚 Bonnes Pratiques GitHub Actions

## 🎯 Concepts Fondamentaux

### 1. Isolation des Jobs
Chaque job s'exécute dans un **runner séparé et isolé** :
```yaml
jobs:
  job1:
    runs-on: ubuntu-latest
    # Runner #1 - Environnement complètement isolé
  
  job2:
    runs-on: ubuntu-latest
    # Runner #2 - NOUVEAU runner, environnement vide
```

**Implications :**
- ❌ Pas de partage de système de fichiers
- ❌ Pas de partage de processus (ex: Minikube, Docker containers)
- ❌ Pas de partage de réseau
- ✅ Utiliser des artifacts pour partager des données

### 2. Partage de Données entre Jobs

#### ✅ Méthode Recommandée : Artifacts
```yaml
# Job 1: Créer et uploader
jobs:
  producer:
    runs-on: ubuntu-latest
    steps:
      - name: Create data
        run: echo "http://service.url" > data.txt
      
      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: my-data
          path: data.txt

  # Job 2: Télécharger et utiliser
  consumer:
    needs: producer
    runs-on: ubuntu-latest
    steps:
      - name: Download artifact
        uses: actions/download-artifact@v4
        with:
          name: my-data
          path: ./
      
      - name: Use data
        run: cat data.txt
```

#### ✅ Méthode Alternative : Outputs
Pour des **petites chaînes de caractères** uniquement :
```yaml
jobs:
  producer:
    runs-on: ubuntu-latest
    outputs:
      service-url: ${{ steps.get_url.outputs.url }}
    steps:
      - id: get_url
        run: echo "url=http://example.com" >> $GITHUB_OUTPUT

  consumer:
    needs: producer
    runs-on: ubuntu-latest
    steps:
      - name: Use output
        run: echo "${{ needs.producer.outputs.service-url }}"
```

**⚠️ Limitation :** Les outputs ont une limite de taille (~1MB)

---

## 🏗️ Architecture des Workflows

### Pattern : Workflow Réutilisable

```yaml
# pipeline-orchestrator.yml (MAIN)
name: CI/CD Pipeline

on: [push, pull_request]

jobs:
  build:
    uses: ./.github/workflows/build.yml
    with:
      environment: production
  
  test:
    needs: build
    uses: ./.github/workflows/test.yml
    with:
      service-url: ${{ needs.build.outputs.url }}

# build.yml (REUSABLE)
name: Build

on:
  workflow_call:
    inputs:
      environment:
        required: true
        type: string
    outputs:
      url:
        value: ${{ jobs.build.outputs.url }}

jobs:
  build:
    outputs:
      url: ${{ steps.deploy.outputs.url }}
    steps:
      # ... build steps
```

**Avantages :**
- ✅ Réutilisabilité
- ✅ Modularité
- ✅ Maintenabilité
- ✅ Testabilité

---

## 🔄 Gestion des Dépendances

### ✅ Bonne Pratique : Dépendances Explicites
```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps: [...]
  
  test:
    needs: build  # Attend que build soit terminé
    runs-on: ubuntu-latest
    steps: [...]
  
  deploy:
    needs: [build, test]  # Attend build ET test
    runs-on: ubuntu-latest
    steps: [...]
```

### ❌ Mauvaise Pratique : Recréer l'Environnement
```yaml
jobs:
  deploy:
    steps:
      - name: Setup Minikube
        run: minikube start
      - name: Deploy app
        run: kubectl apply -f k8s/
  
  test:
    needs: deploy
    steps:
      - name: Setup Minikube  # ❌ NOUVEAU cluster vide !
        run: minikube start
      - name: Test
        run: kubectl get pods  # ❌ Aucun pod !
```

### ✅ Bonne Pratique : Réutiliser les Résultats
```yaml
jobs:
  deploy:
    outputs:
      service-url: ${{ steps.get_url.outputs.url }}
    steps:
      - name: Setup Minikube
        run: minikube start
      - name: Deploy app
        run: kubectl apply -f k8s/
      - id: get_url
        run: echo "url=$(minikube service my-app --url)" >> $GITHUB_OUTPUT
  
  test:
    needs: deploy
    steps:
      - name: Test service
        run: curl ${{ needs.deploy.outputs.service-url }}/health
```

---

## ⏱️ Optimisation des Performances

### 1. Cache des Dépendances

#### Maven
```yaml
- name: Cache Maven dependencies
  uses: actions/cache@v3
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: ${{ runner.os }}-maven-
```

#### NPM
```yaml
- name: Cache NPM dependencies
  uses: actions/cache@v3
  with:
    path: ~/.npm
    key: ${{ runner.os }}-npm-${{ hashFiles('**/package-lock.json') }}
    restore-keys: ${{ runner.os }}-npm-
```

### 2. Jobs Parallèles
```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps: [...]
  
  # Ces trois jobs peuvent s'exécuter EN PARALLÈLE
  lint:
    needs: build
    runs-on: ubuntu-latest
    steps: [...]
  
  test-unit:
    needs: build
    runs-on: ubuntu-latest
    steps: [...]
  
  test-security:
    needs: build
    runs-on: ubuntu-latest
    steps: [...]
```

### 3. Matrix Strategy
```yaml
jobs:
  test:
    strategy:
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
        node: [14, 16, 18]
    runs-on: ${{ matrix.os }}
    steps:
      - uses: actions/setup-node@v3
        with:
          node-version: ${{ matrix.node }}
      - run: npm test
```

---

## 🛡️ Sécurité et Secrets

### ✅ Bonnes Pratiques
```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      # ✅ Utiliser des secrets GitHub
      - name: Login to registry
        env:
          DOCKER_PASSWORD: ${{ secrets.DOCKER_PASSWORD }}
        run: echo "$DOCKER_PASSWORD" | docker login -u user --password-stdin
      
      # ✅ Ne jamais exposer les secrets dans les logs
      - name: Use API key
        env:
          API_KEY: ${{ secrets.API_KEY }}
        run: |
          # La valeur ne sera pas visible dans les logs
          curl -H "Authorization: Bearer $API_KEY" https://api.example.com
```

### ❌ Mauvaises Pratiques
```yaml
# ❌ Ne JAMAIS faire ça !
- name: Debug
  run: |
    echo "Password: ${{ secrets.PASSWORD }}"  # ❌ Exposé dans les logs
    env  # ❌ Affiche tous les secrets
```

---

## 🧪 Tests et Debugging

### 1. Logs Structurés
```yaml
- name: Deploy application
  run: |
    echo "::group::Building image"
    docker build -t myapp .
    echo "::endgroup::"
    
    echo "::group::Deploying to cluster"
    kubectl apply -f k8s/
    echo "::endgroup::"
    
    echo "::notice::Deployment successful!"
```

### 2. Conditions et Erreurs
```yaml
- name: Deploy
  run: |
    if ! kubectl apply -f k8s/; then
      echo "::error::Deployment failed!"
      kubectl get events
      exit 1
    fi
    echo "::notice::Deployment successful"
```

### 3. Continue on Error
```yaml
jobs:
  test:
    steps:
      - name: Unit tests
        run: npm test
      
      - name: Upload coverage (même si tests échouent)
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: coverage
          path: coverage/
```

---

## 📦 Gestion des Artifacts

### ✅ Bonnes Pratiques

#### 1. Nommage Clair
```yaml
- uses: actions/upload-artifact@v4
  with:
    name: test-results-${{ matrix.os }}-${{ matrix.node }}
    path: test-results/
```

#### 2. Durée de Rétention
```yaml
- uses: actions/upload-artifact@v4
  with:
    name: build-artifacts
    path: dist/
    retention-days: 7  # Au lieu du défaut (90 jours)
```

#### 3. Artifacts Conditionnels
```yaml
- name: Upload logs on failure
  if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: failure-logs
    path: logs/
```

---

## 🔧 Timeouts et Ressources

### 1. Timeouts
```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    timeout-minutes: 30  # Limite globale du job
    steps:
      - name: Integration tests
        timeout-minutes: 10  # Limite de cette étape
        run: npm run test:integration
```

### 2. Stratégie de Retry
```yaml
jobs:
  flaky-test:
    strategy:
      fail-fast: false
      max-parallel: 1
    steps:
      - name: Run tests
        uses: nick-invision/retry@v2
        with:
          timeout_minutes: 10
          max_attempts: 3
          command: npm test
```

---

## 📊 Monitoring et Notifications

### 1. Job Summary
```yaml
- name: Create summary
  run: |
    echo "### Test Results 🎯" >> $GITHUB_STEP_SUMMARY
    echo "" >> $GITHUB_STEP_SUMMARY
    echo "- ✅ 42 tests passed" >> $GITHUB_STEP_SUMMARY
    echo "- ❌ 3 tests failed" >> $GITHUB_STEP_SUMMARY
```

### 2. Annotations
```yaml
- name: Check code quality
  run: |
    echo "::warning file=app.js,line=10::Cette fonction est trop complexe"
    echo "::error file=api.js,line=42::Vulnérabilité de sécurité détectée"
```

---

## 🎯 Checklist des Bonnes Pratiques

### Avant de Créer un Workflow

- [ ] **Isolation** : Ai-je compris que chaque job est isolé ?
- [ ] **Artifacts** : Est-ce que j'utilise des artifacts pour partager des données ?
- [ ] **Outputs** : Est-ce que mes outputs sont correctement définis ?
- [ ] **Dépendances** : Les dépendances entre jobs sont-elles claires ?
- [ ] **Timeouts** : Ai-je défini des timeouts raisonnables ?
- [ ] **Cache** : Puis-je cacher des dépendances pour accélérer le build ?
- [ ] **Parallélisation** : Quels jobs peuvent s'exécuter en parallèle ?
- [ ] **Secrets** : Les secrets sont-ils bien protégés ?
- [ ] **Logs** : Les logs sont-ils clairs et structurés ?
- [ ] **Artifacts Cleanup** : La durée de rétention est-elle appropriée ?

### Debugging d'un Workflow qui Échoue

1. **Vérifier les logs** : Y a-t-il des erreurs claires ?
2. **Vérifier les artifacts** : Les données sont-elles partagées correctement ?
3. **Vérifier les outputs** : Les variables sont-elles bien transmises ?
4. **Vérifier l'isolation** : Est-ce qu'un job essaie d'accéder à des ressources d'un autre job ?
5. **Tester localement** : Peut-on reproduire le problème en local ?
6. **Activer le debug** : Ajouter des `echo` pour afficher les variables

```yaml
# Activer le mode debug
- name: Debug
  run: |
    echo "Current directory: $(pwd)"
    echo "Files: $(ls -la)"
    echo "Environment: $(env | sort)"
    echo "Service URL: ${{ env.SERVICE_URL }}"
```

---

## 📖 Ressources Utiles

- [Documentation officielle GitHub Actions](https://docs.github.com/en/actions)
- [Actions Marketplace](https://github.com/marketplace?type=actions)
- [Awesome GitHub Actions](https://github.com/sdras/awesome-actions)

---

## 🎓 Exemples Concrets

### Exemple 1 : Pipeline CI/CD Complet
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]

jobs:
  build:
    outputs:
      version: ${{ steps.version.outputs.version }}
    steps:
      - uses: actions/checkout@v4
      - id: version
        run: echo "version=$(cat version.txt)" >> $GITHUB_OUTPUT
      - run: mvn clean package
      - uses: actions/upload-artifact@v4
        with:
          name: app-jar
          path: target/*.jar

  test:
    needs: build
    steps:
      - uses: actions/checkout@v4
      - uses: actions/download-artifact@v4
        with:
          name: app-jar
      - run: mvn test

  deploy:
    needs: [build, test]
    steps:
      - uses: actions/download-artifact@v4
        with:
          name: app-jar
      - run: |
          echo "Deploying version ${{ needs.build.outputs.version }}"
          # Deploy logic here
```

### Exemple 2 : Tests avec Service
```yaml
jobs:
  deploy:
    outputs:
      url: ${{ steps.url.outputs.url }}
    steps:
      - name: Deploy
        run: kubectl apply -f k8s/
      - id: url
        run: echo "url=$(kubectl get svc --no-headers | awk '{print $4}')" >> $GITHUB_OUTPUT

  test:
    needs: deploy
    steps:
      - name: Test service
        run: |
          URL="${{ needs.deploy.outputs.url }}"
          curl -f "$URL/health" || exit 1
```

---

## ✨ Conclusion

Les bonnes pratiques GitHub Actions reposent sur :

1. **Comprendre l'isolation des jobs** 🔒
2. **Utiliser correctement les artifacts et outputs** 📦
3. **Optimiser les performances** ⚡
4. **Sécuriser les secrets** 🛡️
5. **Créer des logs clairs** 📝
6. **Tester et débugger efficacement** 🧪

En suivant ces pratiques, vos workflows seront :
- ✅ Plus fiables
- ✅ Plus rapides
- ✅ Plus faciles à maintenir
- ✅ Plus faciles à débugger

