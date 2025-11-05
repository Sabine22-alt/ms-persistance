# Guide de Configuration Kubernetes - RecipeYouLove

Ce guide vous aide à résoudre tous les problèmes liés à Kubernetes dans le projet RecipeYouLove.

## Problèmes Corrigés

✅ Configuration kubectl manquante  
✅ Erreur "connection refused localhost:8080"  
✅ Namespaces non créés avant le déploiement  
✅ Ordre incorrect des commandes dans le pipeline CI/CD  
✅ Meilleure gestion des erreurs dans les scripts de déploiement

## Configuration Initiale

### Option 1: Cluster Local avec Minikube (Recommandé pour le développement)

#### Sur Windows:
```powershell
# Vérifier la configuration actuelle
.\k8s\verify-cluster.ps1

# Si le cluster n'est pas configuré, lancez:
.\k8s\setup-local-cluster.ps1
```

#### Sur Linux/Mac:
```bash
# Vérifier la configuration actuelle
bash k8s/verify-cluster.sh

# Si le cluster n'est pas configuré, lancez:
bash k8s/setup-local-cluster.sh
```

### Option 2: Cluster Externe (Production)

Si vous utilisez un cluster externe (AKS, EKS, GKE, etc.):

1. Configurez votre kubeconfig:
```bash
# Exemple pour Azure AKS
az aks get-credentials --resource-group myResourceGroup --name myAKSCluster

# Exemple pour AWS EKS
aws eks update-kubeconfig --region region-code --name my-cluster

# Exemple pour Google GKE
gcloud container clusters get-credentials my-cluster --region=us-central1
```

2. Vérifiez la connexion:
```bash
kubectl cluster-info
kubectl get nodes
```

3. Pour le pipeline GitHub Actions, encodez votre kubeconfig en base64:
```bash
# Linux/Mac
cat ~/.kube/config | base64 -w 0

# Windows PowerShell
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes((Get-Content $env:USERPROFILE\.kube\config -Raw)))
```

4. Ajoutez ce secret dans GitHub:
   - Allez dans Settings > Secrets and variables > Actions
   - Créez un nouveau secret nommé `KUBE_CONFIG`
   - Collez la valeur base64

## Déploiement

### Déploiement Local

#### Environnement Integration:
```powershell
# Windows
cd k8s\environments\integration\scripts
.\deploy-all.ps1

# Linux/Mac
cd k8s/environments/integration/scripts
bash deploy-all.sh
```

#### Environnement Production:
```powershell
# Windows
cd k8s\environments\production\scripts
.\deploy-all.ps1

# Linux/Mac
cd k8s/environments/production/scripts
bash deploy-all.sh
```

### Déploiement via GitHub Actions

Le déploiement automatique se fait via GitHub Actions:

- **Branche `develop`** → Déploiement automatique vers Integration
- **Branche `main`** → Déploiement automatique vers Production

## Vérification du Déploiement

```bash
# Vérifier les pods
kubectl get pods -n soa-integration
kubectl get pods -n soa-production

# Vérifier les services
kubectl get svc -n soa-integration
kubectl get svc -n soa-production

# Vérifier les ingress
kubectl get ingress -n soa-integration
kubectl get ingress -n soa-production

# Voir les logs d'un pod
kubectl logs -f <pod-name> -n soa-integration
```

## Résolution des Problèmes Courants

### Erreur: "connection refused localhost:8080"

**Cause**: kubectl n'est pas configuré correctement.

**Solution**:
```bash
# Vérifier la configuration
kubectl cluster-info

# Si erreur, démarrez minikube
minikube start

# Ou configurez votre cluster externe
```

### Erreur: "namespace not found"

**Cause**: Le namespace n'existe pas.

**Solution**:
```bash
# Créer les namespaces
kubectl create namespace soa-integration
kubectl create namespace soa-production
```

### Erreur: "ImagePullBackOff"

**Cause**: Impossible de télécharger l'image Docker.

**Solution**:
```bash
# Vérifier que l'image existe
docker pull ghcr.io/emiliehascoet/recipeyoulove:latest

# Ou construire l'image localement pour Minikube
eval $(minikube docker-env)
docker build -t ghcr.io/emiliehascoet/recipeyoulove:latest .
```

### Erreur: "CrashLoopBackOff"

**Cause**: L'application plante au démarrage.

**Solution**:
```bash
# Voir les logs du pod
kubectl logs <pod-name> -n soa-integration

# Vérifier les événements
kubectl describe pod <pod-name> -n soa-integration
```

## Accès aux Applications

### Avec Minikube:

```bash
# Démarrer le tunnel (nécessaire pour accéder aux ingress)
minikube tunnel

# Ajouter à votre fichier hosts:
# Windows: C:\Windows\System32\drivers\etc\hosts
# Linux/Mac: /etc/hosts

127.0.0.1 soa-api-integration.recipeyoulove.app
127.0.0.1 soa-api.recipeyoulove.app
```

Puis accédez à:
- Integration: http://soa-api-integration.recipeyoulove.app
- Production: http://soa-api.recipeyoulove.app

### Avec un Cluster Cloud:

Les URLs dépendent de votre configuration d'ingress et de votre DNS.

## Commandes Utiles

```bash
# État du cluster Minikube
minikube status

# Arrêter Minikube
minikube stop

# Supprimer Minikube
minikube delete

# Dashboard Kubernetes
minikube dashboard

# Changer de contexte
kubectl config get-contexts
kubectl config use-context <context-name>

# Nettoyer les ressources
kubectl delete namespace soa-integration
kubectl delete namespace soa-production
```

## Architecture des Déploiements

```
RecipeYouLove/
├── k8s/
│   ├── verify-cluster.sh/ps1       # Vérifier la configuration
│   ├── setup-local-cluster.sh/ps1  # Configurer un cluster local
│   ├── install-all.sh              # Installation complète (Vault, ArgoCD, etc.)
│   └── environments/
│       ├── integration/
│       │   ├── deployment.yaml     # Manifeste Kubernetes
│       │   └── scripts/
│       │       ├── deploy-all.sh
│       │       └── deploy-all.ps1
│       └── production/
│           ├── deployment.yaml     # Manifeste Kubernetes
│           └── scripts/
│               ├── deploy-all.sh
│               └── deploy-all.ps1
```

## Support

Si vous rencontrez toujours des problèmes:

1. Vérifiez les logs: `kubectl logs <pod-name> -n <namespace>`
2. Vérifiez les événements: `kubectl get events -n <namespace> --sort-by='.lastTimestamp'`
3. Consultez la documentation Kubernetes: https://kubernetes.io/docs/

