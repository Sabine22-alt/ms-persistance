# 🔧 Configuration des Microservices Fils

Guide complet pour créer et configurer un nouveau microservice basé sur ce template parent.

## 📋 Table des Matières

- [Créer un Nouveau Microservice](#créer-un-nouveau-microservice)
- [Configuration GitHub Actions](#configuration-github-actions)
- [Configuration Docker](#configuration-docker)
- [Configuration Kubernetes](#configuration-kubernetes)
- [Tests d'Intégration](#tests-dintégration)
- [Variables d'Environnement](#variables-denvironnement)
- [Bonnes Pratiques](#bonnes-pratiques)

---

## 🚀 Créer un Nouveau Microservice

### Étape 1 : Fork du Template

```bash
# 1. Sur GitHub
# Aller sur : https://github.com/votre-org/RecipeYouLove
# Cliquer sur "Fork"
# Nommer le nouveau repository : RecipeYouLove-ServiceXYZ

# 2. Cloner le fork
git clone https://github.com/votre-org/RecipeYouLove-ServiceXYZ.git
cd RecipeYouLove-ServiceXYZ
```

### Étape 2 : Personnalisation du Code

#### 2.1 Modifier le `pom.xml`

```xml
<groupId>com.recipeyoulove</groupId>
<artifactId>service-xyz</artifactId>
<version>1.0.0</version>
<name>RecipeYouLove Service XYZ</name>
<description>Service XYZ pour RecipeYouLove</description>
```

#### 2.2 Renommer le Package Java

```bash
# Ancien : com.springbootTemplate.univ.soa
# Nouveau : com.recipeyoulove.servicexyz

# Structure recommandée :
src/main/java/com/recipeyoulove/servicexyz/
├── ServiceXyzApplication.java
├── controller/
│   └── XyzController.java
├── service/
│   └── XyzService.java
├── repository/
│   └── XyzRepository.java
└── model/
    └── Xyz.java
```

#### 2.3 Adapter `application.properties`

```properties
# Application
spring.application.name=service-xyz
server.port=8080

# Base de données (si nécessaire)
spring.datasource.url=jdbc:mysql://mysql:3306/service_xyz_db
spring.datasource.username=${MYSQL_USERNAME:root}
spring.datasource.password=${MYSQL_PASSWORD:password}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

### Étape 3 : Personnaliser les Endpoints

#### Exemple de Contrôleur Minimal

```java
package com.recipeyoulove.servicexyz.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/xyz")
public class XyzController {

    @GetMapping("/health")
    public String health() {
        return "✅ Service XYZ is healthy";
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "xyz");
        status.put("version", "1.0.0");
        status.put("status", "running");
        return status;
    }

    // Vos endpoints métier ici
    @GetMapping("/data")
    public List<Xyz> getData() {
        // Votre logique
        return xyzService.findAll();
    }
}
```

---

## ⚙️ Configuration GitHub Actions

### Étape 1 : Secrets GitHub

Aller dans `Settings > Secrets and variables > Actions` et ajouter :

```
DOCKER_USERNAME     → Votre username Docker Hub
DOCKER_PASSWORD     → Votre token Docker Hub (ou password)
SONAR_TOKEN         → Token SonarCloud (optionnel)
```

#### Comment créer un Token Docker Hub ?

1. Aller sur https://hub.docker.com
2. Account Settings > Security > New Access Token
3. Copier le token
4. L'ajouter dans GitHub Secrets

### Étape 2 : Variables du Pipeline

Modifier `.github/workflows/config-vars.yml` :

```yaml
env:
  IMAGE_NAME: service-xyz              # ← Nom de votre service
  DOCKER_USERNAME: votre-username      # ← Votre username Docker Hub
  COVERAGE_THRESHOLD: "80"             # ← Seuil de couverture
  SONAR_PROJECT_KEY: recipeyoulove-xyz # ← Projet SonarCloud
```

### Étape 3 : Adapter la Collection Newman

Modifier `tests/newman/collection.json` pour tester **vos** endpoints :

```json
{
  "info": { "name": "Service XYZ Tests" },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/api/xyz/health"
      },
      "event": [{
        "listen": "test",
        "script": {
          "exec": [
            "pm.test('Status is 200', function() {",
            "  pm.response.to.have.status(200);",
            "});"
          ]
        }
      }]
    },
    {
      "name": "Get Data",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/api/xyz/data"
      }
    }
  ]
}
```

### Étape 4 : Tester Localement Avant de Pusher

```powershell
# 1. Build
mvn clean package

# 2. Démarrer l'environnement
.\start-local-env.ps1

# 3. Tester les endpoints
curl http://localhost:8080/api/xyz/health

# 4. Tester Newman
.\test-newman-local.ps1

# 5. Si tout OK → Commit & Push
git add .
git commit -m "feat: configuration initiale service XYZ"
git push
```

---

## 🐳 Configuration Docker

### Dockerfile (déjà présent, à adapter si nécessaire)

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR
COPY target/*.jar app.jar

# Exposer le port
EXPOSE 8080

# Santé du conteneur
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml (personnaliser)

Modifier la section du service :

```yaml
services:
  service-xyz:
    image: service-xyz:latest
    container_name: service-xyz-local
    restart: unless-stopped
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/service_xyz_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
    ports:
      - "8080:8080"
    networks:
      - microservices-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 40s
```

---

## ☸️ Configuration Kubernetes

### Manifests à Adapter

#### `k8s/minikube/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: service-xyz
  labels:
    app: service-xyz
spec:
  replicas: 1
  selector:
    matchLabels:
      app: service-xyz
  template:
    metadata:
      labels:
        app: service-xyz
    spec:
      containers:
      - name: service-xyz
        image: service-xyz:latest
        imagePullPolicy: Never
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:mysql://mysql:3306/service_xyz_db
        - name: SPRING_DATASOURCE_USERNAME
          value: root
        - name: SPRING_DATASOURCE_PASSWORD
          value: password
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

#### `k8s/minikube/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: service-xyz
spec:
  type: NodePort
  selector:
    app: service-xyz
  ports:
  - port: 8080
    targetPort: 8080
    nodePort: 30080  # Choisir un port unique entre 30000-32767
```

---

## 🧪 Tests d'Intégration

### Structure des Tests Newman

```
tests/
└── newman/
    ├── collection.json    # ← Vos tests d'API
    ├── dataset.json       # ← Données de test
    ├── env.json          # ← Configuration environnement
    └── package.json      # ← Dépendances Newman
```

### Exemple de Test Complet

```json
{
  "name": "Create XYZ Item",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/api/xyz/items",
    "header": [
      { "key": "Content-Type", "value": "application/json" }
    ],
    "body": {
      "mode": "raw",
      "raw": "{\"name\": \"{{itemName}}\", \"value\": \"{{itemValue}}\"}"
    }
  },
  "event": [{
    "listen": "test",
    "script": {
      "exec": [
        "pm.test('Status is 201 Created', function() {",
        "  pm.response.to.have.status(201);",
        "});",
        "",
        "pm.test('Response has id', function() {",
        "  const json = pm.response.json();",
        "  pm.expect(json).to.have.property('id');",
        "  pm.environment.set('createdItemId', json.id);",
        "});"
      ]
    }
  }]
}
```

---

## 🔐 Variables d'Environnement

### Fichier `.env` (pour Docker Compose)

```bash
# Service XYZ
SERVICE_XYZ_PORT=8080

# MySQL
MYSQL_ROOT_PASSWORD=password
MYSQL_DATABASE=service_xyz_db
MYSQL_USERNAME=root
MYSQL_PORT=3306

# phpMyAdmin
PHPMYADMIN_PORT=8081

# Autres services si nécessaire
```

### Variables GitHub Actions

Les variables sont automatiquement injectées par le pipeline. Pas besoin de configuration supplémentaire.

---

## ✅ Bonnes Pratiques

### 1. Nommage des Microservices

```
✅ BON : service-xyz, service-recipe, service-user
❌ MAUVAIS : xyz, myService, Service_1
```

### 2. Ports des Services

| Service | Port Local | NodePort K8s | Description |
|---------|-----------|--------------|-------------|
| Service XYZ | 8080 | 30080 | API principale |
| Service Recipe | 8081 | 30081 | Service recettes |
| Service User | 8082 | 30082 | Gestion utilisateurs |
| phpMyAdmin | 8090 | - | Base de données |

### 3. Structure des Endpoints

```
✅ BON :
GET    /api/xyz/items
POST   /api/xyz/items
GET    /api/xyz/items/{id}
PUT    /api/xyz/items/{id}
DELETE /api/xyz/items/{id}

❌ MAUVAIS :
GET /getItems
POST /createItem
```

### 4. Health Checks

**Toujours** implémenter :
- `/actuator/health` (Spring Boot Actuator)
- `/api/{service}/health` (custom)

### 5. Logs

```java
// Utiliser SLF4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(XyzController.class);

logger.info("Request received: {}", request);
logger.error("Error occurred: {}", e.getMessage(), e);
```

### 6. Gestion des Erreurs

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        logger.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An error occurred"));
    }
}
```

---

## 🔄 Workflow Complet de Configuration

```
1. Fork du template parent
   ↓
2. Personnaliser pom.xml + package Java
   ↓
3. Implémenter les endpoints métier
   ↓
4. Créer les tests Newman
   ↓
5. Tester localement (.\start-local-env.ps1)
   ↓
6. Configurer les secrets GitHub
   ↓
7. Adapter config-vars.yml
   ↓
8. Commit & Push
   ↓
9. Vérifier le pipeline GitHub Actions
   ↓
10. ✅ Microservice prêt !
```

---

## 🚀 Configuration ArgoCD pour les Microservices Fils

### Pourquoi ArgoCD ?

ArgoCD permet le **déploiement automatique** des microservices via GitOps :
- Push Git → ArgoCD détecte → Déploie automatiquement
- Interface visuelle pour suivre les déploiements
- Rollback facile en cas de problème

### Setup ArgoCD Local

#### 1. Prérequis

```powershell
# Kubernetes DOIT être activé dans Docker Desktop
# Docker Desktop > Settings > Kubernetes > Enable Kubernetes
```

#### 2. Builder l'Image (CRITIQUE)

```powershell
# TOUJOURS builder l'image AVANT d'installer ArgoCD
.\build-and-load-image.ps1

# Sinon: ErrImageNeverPull
```

#### 3. Installer ArgoCD (une fois)

```powershell
.\setup-argocd.ps1

# IMPORTANT: Sauvegarder le mot de passe affiché !
# Exemple: admin / H4sh3dP4ssw0rd123
```

**Note** : ArgoCD prend **3-5 minutes** à démarrer. C'est normal !

#### 4. Configurer Votre Microservice

```powershell
.\setup-argocd-app.ps1

# Répondre aux questions:
# - URL repo: https://github.com/votre-org/RecipeYouLove-ServiceXYZ
# - Branche: main (ou develop)
# - Nom app: service-xyz
```

### Récupérer le Mot de Passe ArgoCD

#### Si Vous Avez Perdu le Mot de Passe

```powershell
# Récupérer le mot de passe admin
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | ForEach-Object { [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_)) }
```

#### Changer le Mot de Passe

```powershell
# Via l'interface ArgoCD
# User Info > Update Password

# Ou en ligne de commande (si argocd CLI installé)
argocd account update-password
```

### ArgoCD dans le Pipeline GitHub Actions

Pour déployer automatiquement via GitHub Actions vers ArgoCD :

#### 1. Secrets à Configurer

```
GitHub Repo > Settings > Secrets and variables > Actions

ARGOCD_SERVER     → https://votre-argocd.com (ou localhost pour test)
ARGOCD_AUTH_TOKEN → Token d'authentification ArgoCD
```

#### 2. Générer un Token ArgoCD

```bash
# Se connecter à ArgoCD
argocd login <ARGOCD_SERVER> --username admin

# Créer un token pour CI/CD
argocd account generate-token --account ci-cd
```

#### 3. Ajouter un Job dans le Pipeline

```yaml
deploy-argocd:
  name: "Deploy via ArgoCD"
  runs-on: ubuntu-22.04
  needs: [build-docker-image]
  steps:
    - name: Sync ArgoCD Application
      run: |
        # Installer ArgoCD CLI
        curl -sSL -o argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
        chmod +x argocd
        
        # Login
        ./argocd login ${{ secrets.ARGOCD_SERVER }} --auth-token ${{ secrets.ARGOCD_AUTH_TOKEN }} --insecure
        
        # Sync l'application
        ./argocd app sync recipeyoulove --force
        
        # Attendre que le sync soit terminé
        ./argocd app wait recipeyoulove --health
```

### Workflow Complet pour Microservices Fils

```
┌─────────────────────────────────────────┐
│ 1. Fork du Template Parent              │
│    github.com/parent/RecipeYouLove      │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 2. Personnaliser le Code                │
│    - Modifier pom.xml                   │
│    - Renommer packages                  │
│    - Implémenter endpoints métier       │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 3. Tester Localement                    │
│    .\start-local-env.ps1                │
│    http://localhost:8080                │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 4. Setup ArgoCD Local                   │
│    .\build-and-load-image.ps1           │
│    .\setup-argocd.ps1                   │
│    .\setup-argocd-app.ps1               │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 5. Push Code                            │
│    git commit && git push               │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 6. GitHub Actions (CI)                  │
│    Build → Tests → Docker Image         │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│ 7. ArgoCD (CD) - AUTOMATIQUE            │
│    Détecte changement Git               │
│    Déploie dans Kubernetes              │
│    < 3 minutes                          │
└─────────────────────────────────────────┘
```

### Vérifications

```powershell
# Voir l'état de l'application
kubectl get application -n argocd

# Voir les pods déployés
kubectl get pods -n soa-local

# Voir les logs d'un pod
kubectl logs -n soa-local -l app=ms-persistance

# Forcer une synchronisation
kubectl -n argocd patch application recipeyoulove --type merge -p '{"operation":{"sync":{}}}'
```

### Problèmes Courants ArgoCD

#### "ErrImageNeverPull"

**Cause** : Image Docker n'existe pas localement

**Solution** :
```powershell
.\build-and-load-image.ps1
kubectl delete pods --all -n soa-local
```

#### "OutOfSync" dans ArgoCD

**Cause** : Différence entre Git et Kubernetes

**Solution** :
```powershell
# Forcer un sync
kubectl -n argocd patch application recipeyoulove --type merge -p '{"operation":{"sync":{}}}'
```

#### "Application Takes Too Long"

**Cause** : ArgoCD prend 3-5 min au premier démarrage

**Solution** : Patience ! Vérifier les logs :
```powershell
kubectl logs -n argocd deployment/argocd-application-controller
```

#### "Can't Access https://localhost:8080"

**Cause** : Port-forward pas actif

**Solution** :
```powershell
# Démarrer le port-forward
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

## 🆘 Troubleshooting

### Le pipeline échoue au build Maven

```bash
# Vérifier en local
mvn clean package

# Vérifier les dépendances
mvn dependency:tree
```

### Les tests Newman échouent

```powershell
# Tester localement
.\start-local-env.ps1
.\test-newman-local.ps1

# Vérifier les endpoints
curl http://localhost:8080/api/xyz/health
```

### L'image Docker ne se build pas

```bash
# Build manuel
mvn clean package -DskipTests
docker build -t service-xyz:latest .

# Vérifier l'image
docker run -p 8080:8080 service-xyz:latest
```

### Le déploiement Kubernetes échoue

```bash
# Vérifier les manifests
kubectl apply -f k8s/minikube/ --dry-run=client

# Voir les logs
kubectl logs -l app=service-xyz -n soa-integration
```

---

## 📞 Support

- **Issues** : https://github.com/votre-org/RecipeYouLove/issues
- **Documentation complète** : [GUIDE-DEVELOPPEUR.md](GUIDE-DEVELOPPEUR.md)

---

## ✨ Checklist Finale

Avant de considérer votre microservice comme "prêt" :

- [ ] Code métier implémenté
- [ ] Tests unitaires (couverture > 80%)
- [ ] Tests d'intégration Newman
- [ ] `pom.xml` personnalisé
- [ ] `application.properties` configuré
- [ ] Dockerfile adapté
- [ ] Manifests Kubernetes adaptés
- [ ] Secrets GitHub configurés
- [ ] Pipeline CI/CD passe en vert
- [ ] Tests locaux réussis
- [ ] Documentation à jour

**Une fois tous les points validés, votre microservice est prêt à être utilisé !** 🎉

