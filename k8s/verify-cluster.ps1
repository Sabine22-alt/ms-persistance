    Write-Host "   Installez kubectl : https://kubernetes.io/docs/tasks/tools/" -ForegroundColor Yellow
    exit 1
}
$kubectlVersion = kubectl version --client --short 2>&1
Write-Host "   ✅ kubectl installé : $kubectlVersion" -ForegroundColor Green
Write-Host ""

# Vérifier la configuration kubeconfig
Write-Host "2. Vérification de la configuration kubeconfig..." -ForegroundColor Yellow
$kubeconfigPath = "$env:USERPROFILE\.kube\config"
if (!(Test-Path $kubeconfigPath)) {
    Write-Host "❌ Fichier kubeconfig non trouvé ($kubeconfigPath)" -ForegroundColor Red
    Write-Host "   Options:" -ForegroundColor Yellow
    Write-Host "   - Configurez un cluster Kubernetes (minikube, kind, k3s, etc.)" -ForegroundColor Gray
    Write-Host "   - Ou définissez la variable KUBECONFIG" -ForegroundColor Gray
    exit 1
}
Write-Host "   ✅ Fichier kubeconfig trouvé" -ForegroundColor Green
Write-Host ""

# Vérifier la connexion au cluster
Write-Host "3. Vérification de la connexion au cluster..." -ForegroundColor Yellow
$clusterInfo = kubectl cluster-info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Impossible de se connecter au cluster Kubernetes" -ForegroundColor Red
    Write-Host ""
    Write-Host "Détails de l'erreur:" -ForegroundColor Yellow
    Write-Host $clusterInfo -ForegroundColor Red
    Write-Host ""
    Write-Host "Solutions possibles:" -ForegroundColor Yellow
    Write-Host "   1. Démarrez minikube : minikube start" -ForegroundColor Gray
    Write-Host "   2. Vérifiez que votre cluster est en cours d'exécution" -ForegroundColor Gray
    Write-Host "   3. Vérifiez votre configuration kubeconfig" -ForegroundColor Gray
    exit 1
}
Write-Host "   ✅ Connexion au cluster réussie" -ForegroundColor Green
Write-Host ""

# Afficher les informations du cluster
Write-Host "4. Informations du cluster:" -ForegroundColor Yellow
kubectl cluster-info
Write-Host ""

# Vérifier les nodes
Write-Host "5. Nodes disponibles:" -ForegroundColor Yellow
kubectl get nodes
Write-Host ""

# Vérifier les namespaces requis
Write-Host "6. Vérification des namespaces requis..." -ForegroundColor Yellow
$namespaces = @("soa-integration", "soa-production")
foreach ($ns in $namespaces) {
    $nsExists = kubectl get namespace $ns 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ Namespace $ns existe" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  Namespace $ns n'existe pas (sera créé lors du déploiement)" -ForegroundColor Yellow
    }
}
Write-Host ""

Write-Host "==========================================" -ForegroundColor Green
Write-Host "✅ Vérification terminée avec succès" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Le cluster Kubernetes est prêt pour le déploiement." -ForegroundColor Cyan
Write-Host ""
# Script PowerShell de vérification de la configuration Kubernetes
# Ce script vérifie que kubectl est correctement configuré et que le cluster est accessible

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🔍 Vérification du Cluster Kubernetes" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier si kubectl est installé
Write-Host "1. Vérification de kubectl..." -ForegroundColor Yellow
if (!(Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "❌ kubectl n'est pas installé" -ForegroundColor Red
# Script PowerShell de configuration d'un cluster Kubernetes local avec Minikube
# Utilisez ce script si vous n'avez pas de cluster Kubernetes configuré

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🚀 Configuration d'un Cluster Local" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier si minikube est installé
Write-Host "1. Vérification de Minikube..." -ForegroundColor Yellow
if (!(Get-Command minikube -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Minikube n'est pas installé" -ForegroundColor Red
    Write-Host "   Installez Minikube depuis: https://minikube.sigs.k8s.io/docs/start/" -ForegroundColor Yellow
    Write-Host "   Ou utilisez Chocolatey: choco install minikube" -ForegroundColor Gray
    exit 1
} else {
    $minikubeVersion = minikube version --short 2>&1
    Write-Host "   ✅ Minikube installé: $minikubeVersion" -ForegroundColor Green
}
Write-Host ""

# Vérifier si kubectl est installé
Write-Host "2. Vérification de kubectl..." -ForegroundColor Yellow
if (!(Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "❌ kubectl n'est pas installé" -ForegroundColor Red
    Write-Host "   Installez kubectl depuis: https://kubernetes.io/docs/tasks/tools/" -ForegroundColor Yellow
    Write-Host "   Ou utilisez Chocolatey: choco install kubernetes-cli" -ForegroundColor Gray
    exit 1
} else {
    $kubectlVersion = kubectl version --client --short 2>&1
    Write-Host "   ✅ kubectl installé: $kubectlVersion" -ForegroundColor Green
}
Write-Host ""

# Démarrer Minikube
Write-Host "3. Démarrage de Minikube..." -ForegroundColor Yellow
$minikubeStatus = minikube status 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ℹ️  Minikube est déjà en cours d'exécution" -ForegroundColor Gray
} else {
    Write-Host "   🚀 Démarrage du cluster..." -ForegroundColor Cyan
    minikube start --driver=docker --cpus=2 --memory=4096
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   ❌ Erreur lors du démarrage de Minikube" -ForegroundColor Red
        Write-Host "   Essayez avec: minikube start --driver=hyperv" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "   ✅ Minikube démarré" -ForegroundColor Green
}
Write-Host ""

# Activer les addons nécessaires
Write-Host "4. Configuration des addons..." -ForegroundColor Yellow
Write-Host "   📦 Activation de ingress..." -ForegroundColor Cyan
minikube addons enable ingress
Write-Host "   📦 Activation de metrics-server..." -ForegroundColor Cyan
minikube addons enable metrics-server
Write-Host "   ✅ Addons configurés" -ForegroundColor Green
Write-Host ""

# Créer les namespaces
Write-Host "5. Création des namespaces..." -ForegroundColor Yellow
kubectl create namespace soa-integration --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace soa-production --dry-run=client -o yaml | kubectl apply -f -
Write-Host "   ✅ Namespaces créés" -ForegroundColor Green
Write-Host ""

# Vérifier le cluster
Write-Host "6. Vérification du cluster..." -ForegroundColor Yellow
kubectl cluster-info
Write-Host ""
kubectl get nodes
Write-Host ""

Write-Host "==========================================" -ForegroundColor Green
Write-Host "✅ Cluster Local Configuré" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Le cluster Kubernetes local est prêt!" -ForegroundColor Cyan
Write-Host ""
Write-Host "Commandes utiles:" -ForegroundColor Yellow
Write-Host "  - État du cluster    : minikube status" -ForegroundColor Gray
Write-Host "  - Arrêter le cluster : minikube stop" -ForegroundColor Gray
Write-Host "  - Supprimer le cluster : minikube delete" -ForegroundColor Gray
Write-Host "  - Dashboard          : minikube dashboard" -ForegroundColor Gray
Write-Host "  - Tunnel pour ingress: minikube tunnel (à exécuter dans un terminal séparé)" -ForegroundColor Gray
Write-Host ""

