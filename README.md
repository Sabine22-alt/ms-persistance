# 🍳 RecipeYouLove - Parent Template

Template parent pour l'architecture microservices RecipeYouLove avec CI/CD complet.

## 📋 Vue d'Ensemble

Ce repository sert de **template parent** pour tous les microservices de l'application RecipeYouLove. Il contient :
- Pipeline CI/CD complet (GitHub Actions)
- Configuration Docker et Kubernetes
- Tests d'intégration automatisés (Newman)
- Scripts de déploiement local

## 🏗️ Architecture Microservices

```
┌─────────────────────────────────────────────────────────┐
│                 PARENT REPOSITORY                       │
│              (Template + CI/CD)                         │
└─────────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Microservice│  │ Microservice│  │ Microservice│
│    #1       │  │    #2       │  │    #3       │
│  (Fork)     │  │  (Fork)     │  │  (Fork)     │
└─────────────┘  └─────────────┘  └─────────────┘
```

Chaque microservice :
1. **Fork** ce repository parent
2. **Hérite** du pipeline CI/CD
3. **Personnalise** son code métier
4. **Partage** la même infrastructure

## 🚀 Démarrage Rapide

### Pour les Développeurs (Test Local)

```powershell
# 1. Cloner le repository
git clone https://github.com/votre-org/RecipeYouLove.git
cd RecipeYouLove

# 2. Démarrer l'environnement complet
.\start-local-env.ps1

# 3. Accéder à l'application
# API :        http://localhost:8080
# phpMyAdmin : http://localhost:8081
# MinIO :      http://localhost:9001
```

### Pour Créer un Nouveau Microservice

Consultez **[CONFIGURATION-MICROSERVICES.md](CONFIGURATION-MICROSERVICES.md)** pour les instructions complètes.

## 🌐 Accès aux Services

### En Local (Développement)

| Service | URL | Identifiants |
|---------|-----|--------------|
| **API Spring Boot** | http://localhost:8080 | - |
| phpMyAdmin | http://localhost:8081 | `root` / `password` |
| Mongo Express | http://localhost:8082 | `admin` / `password` |
| MinIO Console | http://localhost:9001 | `minioadmin` / `minioadmin` |

### En CI/CD (GitHub Actions)

Les services déployés dans Minikube (GitHub Actions) sont **uniquement pour les tests automatiques**. Les URLs ne sont pas accessibles depuis l'extérieur.

## 📊 Pipeline CI/CD

```
1️⃣ Configuration & Variables
2️⃣ Build Maven
3️⃣ Check Code Coverage (80% minimum)
4️⃣ Build Docker Image
5️⃣ Check Image Security (Trivy)
6️⃣ Deploy to Kubernetes & Integration Tests (Newman)
7️⃣ Log Components URLs
```

### Déclenchement

- **Push** sur `main`, `develop`, `feat/**`, `fix/**`
- **Pull Request** vers `main`, `develop`

### Résultats

- ✅ Tests unitaires
- ✅ Couverture de code
- ✅ Sécurité de l'image
- ✅ Tests d'intégration
- 📦 Artifacts (JAR, Docker image, rapports)

## 🛠️ Scripts Disponibles

### Développement Local

| Script | Description |
|--------|-------------|
| `start-local-env.ps1` | Démarrer l'environnement Docker Compose complet |
| `stop-local-env.ps1` | Arrêter l'environnement |
| `test-newman-local.ps1` | Exécuter les tests Newman localement |
| `quick-start.ps1` | Build rapide et démarrage de l'app seule |

### ArgoCD (GitOps)

| Script | Description |
|--------|-------------|
| `setup-argocd.ps1` | Installer ArgoCD sur Kubernetes local |
| `setup-argocd-app.ps1` | Configurer une application ArgoCD |

## 🎯 Déploiement avec ArgoCD (GitOps)

### Setup Rapide

```powershell
# 1. Builder l'image (IMPORTANT - sinon ErrImageNeverPull)
.\build-and-load-image.ps1

# 2. Installer ArgoCD (une fois, prend 3-5 min)
.\setup-argocd.ps1
# Mot de passe affiche dans le terminal

# 3. Configurer votre app
.\setup-argocd-app.ps1
# Entrer l'URL de votre repo Git

# 4. Interface Web
https://localhost:8080
# Login: admin / Password: (affiche a l'etape 2)
```

### Workflow Quotidien

```
1. Modifier code
2. .\build-and-load-image.ps1
3. git commit && git push
4. ArgoCD synchronise automatiquement (< 3 min)
5. Verifier: kubectl get pods -n soa-local
```

### Notes Importantes

- ⏱️ **ArgoCD prend 3-5 minutes** à démarrer au premier lancement
- 🔑 **Mot de passe admin** : sauvegardé dans le terminal lors du setup
- 🐳 **Image Docker** : doit être buildée localement AVANT le déploiement
- 🔄 **Sync automatique** : max 3 minutes après un push Git

## 📚 Documentation

### Pour Démarrer

- **[README.md](README.md)** (ce fichier) - Vue d'ensemble et démarrage rapide

### Pour Développer

- **[GUIDE-DEVELOPPEUR.md](GUIDE-DEVELOPPEUR.md)** - Guide complet développeur
  - Setup environnement
  - Tests (unitaires + Newman)
  - **ArgoCD : Setup, mot de passe, troubleshooting**
  - Pipeline CI/CD expliqué
  - Debugging

### Pour Créer un Microservice

- **[CONFIGURATION-MICROSERVICES.md](CONFIGURATION-MICROSERVICES.md)** - Configuration microservices fils
  - Fork et personnalisation
  - Configuration GitHub Actions
  - Configuration Docker/Kubernetes
  - **ArgoCD pour microservices fils**
  - **Récupération mot de passe ArgoCD**
  - Tests d'intégration

## 🔧 Configuration Requise

### Développement Local

- **Java** 17+
- **Maven** 3.8+
- **Docker Desktop** (avec Kubernetes optionnel)
- **PowerShell** 5.1+
- **Git**

### CI/CD (GitHub Actions)

Rien à installer, tout est automatique !

## 🎯 Cas d'Usage

### Je veux tester l'application localement

```powershell
.\start-local-env.ps1
# Ouvrir http://localhost:8080
```

### Je veux créer un nouveau microservice

Voir **[CONFIGURATION-MICROSERVICES.md](CONFIGURATION-MICROSERVICES.md)**

### Je veux comprendre le pipeline CI/CD

Voir **[GUIDE-DEVELOPPEUR.md](GUIDE-DEVELOPPEUR.md)** section "Pipeline CI/CD"

### Je veux modifier la collection Newman

Modifier `tests/newman/collection.json` puis :
```powershell
.\test-newman-local.ps1
```

## 🤝 Contribution

1. Fork le repository
2. Créer une branche : `git checkout -b feat/ma-fonctionnalite`
3. Commit : `git commit -m "feat: ma fonctionnalité"`
4. Push : `git push origin feat/ma-fonctionnalite`
5. Créer une Pull Request

## 📝 Conventions de Commit

```
feat: nouvelle fonctionnalité
fix: correction de bug
docs: documentation
refactor: refactoring
test: ajout de tests
chore: tâches diverses
```

## 🐛 Support

- **Issues** : https://github.com/votre-org/RecipeYouLove/issues
- **Discussions** : https://github.com/votre-org/RecipeYouLove/discussions

## 📄 Licence

Ce projet est sous licence MIT. Voir [LICENSE](LICENSE) pour plus de détails.

---

## ⚡ TL;DR (Trop Long, Pas Lu)

```powershell
# Démarrer tout en local
.\start-local-env.ps1

# Accéder
# http://localhost:8080       → API
# http://localhost:8081       → phpMyAdmin
# http://localhost:9001       → MinIO

# Arrêter
.\stop-local-env.ps1

# Créer un microservice → Lire CONFIGURATION-MICROSERVICES.md
```

🎉 **C'est tout !**

