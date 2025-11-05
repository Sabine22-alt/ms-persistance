# Script PowerShell de déploiement pour l'environnement Integration
# Ce script déploie tous les microservices de l'environnement Integration

Write-Host "🧪 Déploiement sur l'environnement INTEGRATION" -ForegroundColor Cyan
Write-Host ""

# Configuration
$namespace = "soa-integration"

# Vérifier que kubectl est configuré
Write-Host "🔍 Vérification de la connexion Kubernetes..." -ForegroundColor Yellow
$clusterInfo = kubectl cluster-info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Impossible de se connecter au cluster Kubernetes" -ForegroundColor Red
    Write-Host "   Erreur: $clusterInfo" -ForegroundColor Red
    Write-Host ""
    Write-Host "Solutions:" -ForegroundColor Yellow
    Write-Host "   1. Démarrez Minikube: minikube start" -ForegroundColor Gray
    Write-Host "   2. Vérifiez votre configuration kubeconfig" -ForegroundColor Gray
    exit 1
}
Write-Host "✅ Connexion au cluster OK" -ForegroundColor Green
Write-Host ""

# Créer le namespace s'il n'existe pas
Write-Host "📦 Création du namespace $namespace..." -ForegroundColor Yellow
kubectl create namespace $namespace --dry-run=client -o yaml | kubectl apply -f -
Write-Host "✅ Namespace $namespace prêt" -ForegroundColor Green
Write-Host ""

# Vérifier Vault
Write-Host "🔐 Vérification de Vault..." -ForegroundColor Yellow
$vaultPods = kubectl get pods -n vault -l app.kubernetes.io/name=vault -o jsonpath='{.items[0].status.phase}' 2>$null
if ($vaultPods -ne "Running") {
    Write-Host "⚠️  Vault n'est pas opérationnel" -ForegroundColor Yellow
} else {
    Write-Host "✅ Vault opérationnel" -ForegroundColor Green
}
Write-Host ""

# Déployer tous les microservices
Write-Host "🚀 Déploiement des microservices..." -ForegroundColor Cyan

# Déployer le manifeste principal
if (Test-Path "deployment.yaml") {
    Write-Host "   ✓ Déploiement du service principal..." -ForegroundColor Green
    kubectl apply -f deployment.yaml
}

# Déployer tous les microservices dans le dossier microservices/
if (Test-Path "microservices") {
    Get-ChildItem "microservices" -Directory | ForEach-Object {
        $servicePath = Join-Path $_.FullName "deployment.yaml"
        if (Test-Path $servicePath) {
            Write-Host "   ✓ Déploiement de $($_.Name)..." -ForegroundColor Green
            kubectl apply -f $servicePath
        }
    }
}

Write-Host ""
Write-Host "⏳ Attente de la disponibilité des pods..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "✅ Déploiement terminé sur Integration" -ForegroundColor Green
Write-Host ""

Write-Host "📊 État des pods:" -ForegroundColor Cyan
kubectl get pods -n $namespace

Write-Host ""
Write-Host "🌐 Services:" -ForegroundColor Cyan
kubectl get svc -n $namespace

Write-Host ""
Write-Host "🔗 Ingress:" -ForegroundColor Cyan
kubectl get ingress -n $namespace

Write-Host ""
Write-Host "💡 Pour voir les logs d'un pod:" -ForegroundColor Yellow
Write-Host "   kubectl logs -f <pod-name> -n $namespace" -ForegroundColor Gray
Write-Host ""
Write-Host "💡 Pour accéder via port-forward:" -ForegroundColor Yellow
Write-Host "   kubectl port-forward -n $namespace svc/<service-name> 8080:8080" -ForegroundColor Gray

