# 👨‍💻 Guide du Développeur

Guide complet pour développer, tester et déployer sur RecipeYouLove.

## 📋 Table des Matières

- [Environnement de Développement](#environnement-de-développement)
- [Démarrage Rapide](#démarrage-rapide)
- [Architecture du Projet](#architecture-du-projet)
- [Pipeline CI/CD](#pipeline-cicd)
- [Tests](#tests)
- [Déploiement](#déploiement)
- [Debugging](#debugging)
- [FAQ](#faq)

---

## 💻 Environnement de Développement

### Prérequis

```bash
# Java
java -version  # Doit être 17+

# Maven
mvn -version   # Doit être 3.8+

# Docker
docker --version
docker-compose --version

# Git
git --version
```

### Installation Recommandée

- **IDE** : IntelliJ IDEA Community ou VS Code
- **Extensions VS Code** :
  - Spring Boot Extension Pack
  - Java Extension Pack
  - Docker
  - YAML

### Configuration IDE

#### IntelliJ IDEA

1. File > Project Structure > Project SDK : Java 17
2. File > Settings > Build > Build Tools > Maven : Use Maven wrapper
3. Enable Lombok plugin

#### VS Code

```json
// .vscode/settings.json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.ls.java.home": "C:\\Program Files\\Java\\jdk-17"
}
```

---

## 🎯 ArgoCD - Déploiement GitOps

### Qu'est-ce qu'ArgoCD ?

ArgoCD = **Déploiement automatique** sur Kubernetes via Git
- Push code → ArgoCD détecte → Déploie automatiquement
- Interface web pour visualiser les déploiements
- Rollback facile

### Setup Initial (10 minutes)

```powershell
# 1. Builder l'image
.\build-and-load-image.ps1

# 2. Installer ArgoCD (prend 3-5 min à démarrer)
.\setup-argocd.ps1

# 3. SAUVEGARDER le mot de passe affiché !
# Exemple: admin / H4sh3dP4ssw0rd123

# 4. Configurer l'application
.\setup-argocd-app.ps1
```

### Récupérer le Mot de Passe ArgoCD

#### Méthode 1 : Via kubectl (RAPIDE)

```powershell
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | ForEach-Object { [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_)) }
```

#### Méthode 2 : Via le Secret Kubernetes

```powershell
# Voir le secret en base64
kubectl get secret argocd-initial-admin-secret -n argocd -o yaml

# Decoder manuellement
# Copier la valeur de "password" et décoder sur https://www.base64decode.org/
```

#### Changer le Mot de Passe

```powershell
# Via l'interface ArgoCD
# 1. Login avec le mot de passe actuel
# 2. User Info (en haut à droite)
# 3. Update Password
```

### Accéder à ArgoCD

```
URL:      https://localhost:8080
Username: admin
Password: (récupéré ci-dessus)

⚠️ Accepter le certificat auto-signé dans le navigateur
```

### Workflow Quotidien

```powershell
# 1. Modifier le code

# 2. Tester localement
.\start-local-env.ps1

# 3. Builder l'image
.\build-and-load-image.ps1

# 4. Push
git add .
git commit -m "feat: nouvelle fonctionnalité"
git push

# 5. ArgoCD déploie automatiquement (< 3 min)
# Vérifier dans l'interface: https://localhost:8080

# 6. Vérifier les pods
kubectl get pods -n soa-local
```

### Commandes Utiles ArgoCD

```powershell
# Voir l'application
kubectl get application -n argocd

# Forcer une synchronisation
kubectl -n argocd patch application recipeyoulove --type merge -p '{"operation":{"sync":{}}}'

# Voir les logs ArgoCD
kubectl logs -n argocd deployment/argocd-application-controller

# Redémarrer le port-forward si nécessaire
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

### ArgoCD dans GitHub Actions (CI/CD Complet)

Pour un déploiement automatique via GitHub Actions :

#### 1. Créer un Token ArgoCD

```bash
# Installer ArgoCD CLI (une fois)
# https://argo-cd.readthedocs.io/en/stable/cli_installation/

# Se connecter
argocd login localhost:8080 --username admin

# Créer un token pour CI/CD
argocd account generate-token --account ci-cd
```

#### 2. Ajouter aux Secrets GitHub

```
GitHub > Settings > Secrets and variables > Actions

ARGOCD_SERVER     → https://votre-argocd.com
ARGOCD_AUTH_TOKEN → (token généré ci-dessus)
```

#### 3. Le Pipeline Déploie Automatiquement

```yaml
# Dans .github/workflows/deploy-argocd.yml
deploy-argocd:
  runs-on: ubuntu-22.04
  needs: [build-docker-image]
  steps:
    - name: Sync ArgoCD
      run: |
        argocd login ${{ secrets.ARGOCD_SERVER }} --auth-token ${{ secrets.ARGOCD_AUTH_TOKEN }}
        argocd app sync recipeyoulove
        argocd app wait recipeyoulove --health
```

### Troubleshooting ArgoCD

#### ArgoCD prend trop de temps

**Normal** : 3-5 minutes au premier démarrage
```powershell
# Vérifier les pods
kubectl get pods -n argocd

# Attendre que tous soient "Running"
```

#### Can't access https://localhost:8080

```powershell
# Vérifier que le port-forward est actif
# Il doit y avoir une fenêtre PowerShell ouverte avec:
# "kubectl port-forward svc/argocd-server -n argocd 8080:443"

# Si fermée, relancer:
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

#### ErrImageNeverPull

```powershell
# L'image n'existe pas localement
.\build-and-load-image.ps1
kubectl delete pods --all -n soa-local
```

#### OutOfSync

```powershell
# Forcer la synchronisation
kubectl -n argocd patch application recipeyoulove --type merge -p '{"operation":{"sync":{}}}'
```

## 🚀 Démarrage Rapide

### Option 1 : Environnement Complet (Docker Compose)

```powershell
# Démarrer TOUT (API + MySQL + phpMyAdmin + MinIO + MongoDB)
.\start-local-env.ps1
```

**Avantages** :
- ✅ Environnement identique à la production
- ✅ Base de données automatiquement créée
- ✅ Tous les services disponibles
- ✅ URLs accessibles dans le navigateur

**URLs** :
- API : http://localhost:8080
- phpMyAdmin : http://localhost:8081
- MinIO : http://localhost:9001

### Option 2 : Application Seule (Maven)

```bash
# Démarrer uniquement l'API (développement rapide)
mvn spring-boot:run
```

**Avantages** :
- ✅ Démarrage ultra-rapide (~10 secondes)
- ✅ Hot reload activé
- ✅ Logs directement dans le terminal

**Inconvénient** :
- ❌ Pas de base de données (à moins de la démarrer séparément)

### Option 3 : Quick Start

```powershell
# Build + Démarrage rapide
.\quick-start.ps1
```

---

## 🏗️ Architecture du Projet

### Structure des Dossiers

```
RecipeYouLove/
├── .github/workflows/          # Pipeline CI/CD
│   ├── pipeline-orchestrator.yml  # Orchestration globale
│   ├── config-vars.yml            # Configuration & variables
│   ├── build-maven.yml            # Build Maven
│   ├── check-coverage.yml         # Couverture de code
│   ├── build-docker-image.yml     # Build image Docker
│   ├── check-conformity-image.yml # Sécurité image
│   ├── deploy-kubernetes.yml      # Déploiement K8s + Tests
│   └── log-components.yml         # Affichage URLs
│
├── src/
│   ├── main/java/com/springbootTemplate/univ/soa/
│   │   ├── Application.java       # Point d'entrée
│   │   └── controller/            # Contrôleurs REST
│   │       ├── HomeController.java
│   │       └── DatabaseController.java
│   │
│   ├── main/resources/
│   │   ├── application.properties # Configuration Spring
│   │   └── META-INF/
│   │       └── spring.factories   # Auto-configuration
│   │
│   └── test/java/                 # Tests unitaires
│       └── com/springbootTemplate/univ/soa/
│           └── controller/
│               ├── HomeControllerTest.java
│               └── DatabaseControllerTest.java
│
├── k8s/minikube/                  # Manifests Kubernetes
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── mysql.yaml
│   ├── phpmyadmin.yaml
│   └── configmap.yaml
│
├── tests/newman/                  # Tests d'intégration
│   ├── collection.json            # Collection Postman/Newman
│   ├── dataset.json               # Données de test
│   └── env.json                   # Environnement
│
├── docker-compose.yml             # Orchestration Docker locale
├── Dockerfile                     # Image Docker de l'app
├── pom.xml                        # Configuration Maven
│
├── start-local-env.ps1            # Démarrer environnement local
├── stop-local-env.ps1             # Arrêter environnement
├── test-newman-local.ps1          # Tests Newman locaux
└── quick-start.ps1                # Build et démarrage rapide
```

### Architecture Applicative

```
┌─────────────────────────────────────────────┐
│           Spring Boot Application           │
├─────────────────────────────────────────────┤
│  Controllers (REST API)                     │
│  ├─ HomeController                          │
│  └─ DatabaseController                      │
├─────────────────────────────────────────────┤
│  Services (Business Logic)                  │
├─────────────────────────────────────────────┤
│  Repositories (Data Access)                 │
├─────────────────────────────────────────────┤
│  MySQL Database                             │
└─────────────────────────────────────────────┘
```

---

## 🔄 Pipeline CI/CD

### Vue d'Ensemble

```
┌─────────────────────────────────────────────────┐
│ 1️⃣ Configuration & Variables                    │
│    - Définition des variables globales          │
│    - Calcul de l'image tag                      │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 2️⃣ Build Maven                                  │
│    - Compilation du code                        │
│    - Exécution tests unitaires                  │
│    - Packaging JAR                              │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 3️⃣ Check Code Coverage                          │
│    - Analyse Jacoco                             │
│    - Vérification seuil (80%)                   │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 4️⃣ Build Docker Image                           │
│    - Construction de l'image                    │
│    - Tag avec SHA du commit                     │
│    - Upload en artifact                         │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 5️⃣ Check Image Security                         │
│    - Scan Trivy                                 │
│    - Vérification vulnérabilités                │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 6️⃣ Deploy to Kubernetes & Integration Tests     │
│    - Setup Minikube                             │
│    - Deploy MySQL + Application                 │
│    - Wait for readiness                         │
│    - Run Newman tests                           │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 7️⃣ Log Components URLs                          │
│    - Affiche instructions d'accès               │
│    - Génère artifact avec URLs                  │
└─────────────────────────────────────────────────┘
```

### Déclencheurs

Le pipeline s'exécute sur :

```yaml
on:
  push:
    branches:
      - main
      - develop
      - 'feat/**'
      - 'fix/**'
  pull_request:
    branches:
      - main
      - develop
```

### Résultats du Pipeline

#### ✅ Si Tout Passe

- Code compilé
- Tests unitaires OK (couverture ≥ 80%)
- Image Docker créée
- Aucune vulnérabilité critique
- Tests d'intégration réussis
- Artifacts disponibles

#### ❌ Si Échec

Le pipeline s'arrête à la première erreur. Consultez les logs pour identifier le problème.

### Artifacts Générés

| Artifact | Description | Rétention |
|----------|-------------|-----------|
| `app-jar` | JAR de l'application | 7 jours |
| `docker-image` | Image Docker (tar) | 1 jour |
| `jacoco-report` | Rapport de couverture | 7 jours |
| `trivy-results` | Scan de sécurité | 7 jours |
| `newman-results` | Résultats tests Newman | 7 jours |
| `service-url` | URL du service déployé | 1 jour |
| `component-urls` | Instructions d'accès | 7 jours |

---

## 🧪 Tests

### Tests Unitaires (JUnit + Mockito)

#### Exécuter les Tests

```bash
# Tous les tests
mvn test

# Un test spécifique
mvn test -Dtest=HomeControllerTest

# Avec couverture
mvn test jacoco:report
# Rapport dans : target/site/jacoco/index.html
```

#### Exemple de Test

```java
@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHealth() throws Exception {
        mockMvc.perform(get("/health"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("healthy")));
    }
}
```

### Tests d'Intégration (Newman)

#### Exécuter les Tests

```powershell
# Avec environnement Docker Compose
.\start-local-env.ps1
.\test-newman-local.ps1

# Ou manuellement
cd tests/newman
npm install
npx newman run collection.json --environment env.json
```

#### Structure d'un Test Newman

```json
{
  "name": "Test API Health",
  "request": {
    "method": "GET",
    "url": "{{baseUrl}}/health"
  },
  "event": [{
    "listen": "test",
    "script": {
      "exec": [
        "pm.test('Status code is 200', function() {",
        "  pm.response.to.have.status(200);",
        "});",
        "",
        "pm.test('Response contains healthy', function() {",
        "  pm.expect(pm.response.text()).to.include('healthy');",
        "});"
      ]
    }
  }]
}
```

#### Assertions Communes

```javascript
// Status code
pm.response.to.have.status(200);

// JSON response
const json = pm.response.json();
pm.expect(json).to.have.property('id');
pm.expect(json.status).to.equal('success');

// Response time
pm.expect(pm.response.responseTime).to.be.below(2000);

// Headers
pm.response.to.have.header('Content-Type');

// Body contains
pm.expect(pm.response.text()).to.include('success');
```

---

## 🚀 Déploiement

### Déploiement Local

#### Docker Compose (Recommandé)

```powershell
# Démarrer
.\start-local-env.ps1

# Voir les logs
docker-compose logs -f

# Redémarrer un service
docker-compose restart ms-persistance

# Arrêter
.\stop-local-env.ps1
```



### Déploiement CI/CD

Le déploiement est **automatique** via GitHub Actions :

1. **Push** sur `main`, `develop`, `feat/**`, `fix/**`
2. Pipeline s'exécute
3. Si tous les tests passent → Déployé dans Minikube (CI/CD)
4. Tests d'intégration Newman exécutés automatiquement

**Note** : Le déploiement CI/CD est pour les **tests automatiques**, pas pour un accès externe.

---

## 🐛 Debugging

### Logs de l'Application

#### En Local (Maven)

```bash
mvn spring-boot:run
# Les logs s'affichent directement
```

#### En Docker Compose

```powershell
# Tous les services
docker-compose logs -f

# Un service spécifique
docker-compose logs -f ms-persistance

# Dernières 100 lignes
docker-compose logs --tail=100 ms-persistance
```

#### En Kubernetes

```bash
# Logs en temps réel
kubectl logs -f -l app=ms-persistance -n soa-local

# Logs des 5 dernières minutes
kubectl logs --since=5m -l app=ms-persistance -n soa-local

# Logs d'un pod spécifique
kubectl logs <pod-name> -n soa-local
```

### Problèmes Courants

#### 1. Port déjà utilisé

```powershell
# Trouver le processus
netstat -ano | findstr :8080

# Tuer le processus
taskkill /PID <PID> /F
```

#### 2. Tests Newman échouent

```powershell
# Vérifier que l'API répond
curl http://localhost:8080/health

# Vérifier les logs de l'API
docker-compose logs ms-persistance

# Redémarrer l'environnement
docker-compose restart ms-persistance
```

#### 3. Base de données ne se connecte pas

```powershell
# Vérifier MySQL
docker-compose logs mysql

# Vérifier la connexion
docker-compose exec mysql mysql -uroot -ppassword -e "SHOW DATABASES;"

# Recréer la base de données
docker-compose down -v
docker-compose up -d
```

#### 4. Build Maven échoue

```bash
# Nettoyer complètement
mvn clean

# Forcer la mise à jour des dépendances
mvn clean install -U

# Skip tests temporairement
mvn clean package -DskipTests
```

#### 5. Image Docker ne se build pas

```bash
# Build manuel avec logs
docker build -t ms-persistance:latest . --progress=plain

# Nettoyer les images non utilisées
docker system prune -a
```

### Mode Debug Spring Boot

#### application.properties

```properties
# Activer debug logs
logging.level.root=DEBUG
logging.level.com.springbootTemplate=DEBUG

# SQL logs
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

#### IntelliJ IDEA

1. Run > Edit Configurations
2. Add New Configuration > Spring Boot
3. Main class : `Application`
4. Click Debug button

#### VS Code

```json
// .vscode/launch.json
{
  "configurations": [
    {
      "type": "java",
      "name": "Debug Spring Boot",
      "request": "launch",
      "mainClass": "com.springbootTemplate.univ.soa.Application"
    }
  ]
}
```

---

## ❓ FAQ

### Q: Comment accéder aux services déployés dans GitHub Actions ?

**R:** Les services dans GitHub Actions (Minikube) ne sont **pas accessibles** depuis l'extérieur. C'est uniquement pour les tests automatiques. Pour tester vous-même, utilisez :
```powershell
.\start-local-env.ps1
```

### Q: Quelle est la différence entre Docker Compose et Kubernetes local ?

**R:**
- **Docker Compose** : Plus simple, démarrage rapide, URLs `localhost`
- **Kubernetes** : Plus proche de la production, mais plus complexe

Pour le développement → **Docker Compose**

### Q: Comment modifier les tests Newman ?

**R:**
1. Éditer `tests/newman/collection.json`
2. Tester localement : `.\test-newman-local.ps1`
3. Si OK → Commit & Push

### Q: Le pipeline échoue, comment savoir pourquoi ?

**R:**
1. Aller sur GitHub Actions
2. Cliquer sur le workflow qui a échoué
3. Cliquer sur le job en rouge
4. Développer les étapes pour voir les erreurs
5. Corriger et re-pusher

### Q: Comment augmenter la couverture de code ?

**R:**
1. Identifier les classes non testées :
   ```bash
   mvn jacoco:report
   # Ouvrir target/site/jacoco/index.html
   ```
2. Ajouter des tests unitaires
3. Vérifier : `mvn test jacoco:report`

### Q: Puis-je utiliser PostgreSQL au lieu de MySQL ?

**R:** Oui, modifier `docker-compose.yml` et `application.properties` :
```properties
spring.datasource.url=jdbc:postgresql://postgres:5432/mydb
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Q: Comment ajouter une dépendance Maven ?

**R:**
1. Ajouter dans `pom.xml` :
   ```xml
   <dependency>
       <groupId>com.example</groupId>
       <artifactId>my-lib</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```
2. Reload Maven : `mvn clean install`

---

## 📚 Ressources

### Documentation Officielle

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Maven](https://maven.apache.org/)
- [Docker](https://docs.docker.com/)
- [Kubernetes](https://kubernetes.io/docs/)
- [Newman](https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/)

### Commandes Utiles

```bash
# Maven
mvn clean package          # Build
mvn test                   # Tests
mvn spring-boot:run        # Run app
mvn dependency:tree        # Voir dépendances

# Docker
docker ps                  # Conteneurs actifs
docker logs <container>    # Logs
docker exec -it <c> bash   # Shell dans conteneur

# Kubernetes
kubectl get pods           # Liste pods
kubectl describe pod <p>   # Détails pod
kubectl logs <pod>         # Logs pod
kubectl port-forward       # Port forward

# Git
git status                 # État du dépôt
git add .                  # Ajouter tous les fichiers
git commit -m "message"    # Commit
git push                   # Push
```

---

## 🎉 Vous êtes Prêt !

Maintenant vous savez :
- ✅ Démarrer l'environnement local
- ✅ Développer et tester
- ✅ Comprendre le pipeline CI/CD
- ✅ Débugger les problèmes
- ✅ Déployer votre code

**Bon développement !** 🚀

Pour créer un nouveau microservice → [CONFIGURATION-MICROSERVICES.md](CONFIGURATION-MICROSERVICES.md)

