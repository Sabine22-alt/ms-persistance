# Configuration Application ArgoCD
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "     Configuration Application ArgoCD" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
# Verifier qu'ArgoCD est installe
Write-Host "Verification d'ArgoCD..." -ForegroundColor Yellow
$argocdRunning = kubectl get namespace argocd 2>$null
if (-not $argocdRunning) {
    Write-Host "ERREUR - ArgoCD n'est pas installe!" -ForegroundColor Red
    Write-Host "   Executez d'abord : .\setup-argocd.ps1`n" -ForegroundColor Yellow
    exit 1
}
Write-Host "OK - ArgoCD installe`n" -ForegroundColor Green
# Demander les informations du repo
Write-Host "Configuration de l'application...`n" -ForegroundColor Cyan
$repoUrl = Read-Host "URL du repository Git (ex: https://github.com/user/RecipeYouLove)"
$branch = Read-Host "Branche a surveiller (defaut: main)"
if ([string]::IsNullOrWhiteSpace($branch)) { $branch = "main" }
$appName = Read-Host "Nom de l'application (defaut: recipeyoulove)"
if ([string]::IsNullOrWhiteSpace($appName)) { $appName = "recipeyoulove" }
Write-Host ""
# Creer le namespace de destination
Write-Host "Creation du namespace de destination..." -ForegroundColor Yellow
kubectl create namespace soa-local 2>$null
Write-Host "OK - Namespace cree`n" -ForegroundColor Green
# Creer le manifeste ArgoCD Application
Write-Host "Creation du manifeste ArgoCD..." -ForegroundColor Yellow
$appManifest = @"
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: $appName
  namespace: argocd
spec:
  project: default
  source:
    repoURL: $repoUrl
    targetRevision: $branch
    path: k8s/minikube
  destination:
    server: https://kubernetes.default.svc
    namespace: soa-local
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      allowEmpty: false
    syncOptions:
    - CreateNamespace=true
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
"@
$appManifest | Out-File -FilePath "argocd-app.yaml" -Encoding UTF8
Write-Host "OK - Manifeste cree : argocd-app.yaml`n" -ForegroundColor Green
# Appliquer le manifeste
Write-Host "Deploiement de l'application dans ArgoCD..." -ForegroundColor Yellow
kubectl apply -f argocd-app.yaml
Write-Host "`nOK - Application configuree!`n" -ForegroundColor Green
# Synchroniser immediatement
Write-Host "Synchronisation initiale..." -ForegroundColor Yellow
kubectl -n argocd patch application $appName --type merge -p '{\"operation\":{\"initiatedBy\":{\"username\":\"admin\"},\"sync\":{}}}'
Start-Sleep -Seconds 5
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "         APPLICATION ARGOCD CONFIGUREE" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Accedez a l'interface ArgoCD :" -ForegroundColor Cyan
Write-Host "   https://localhost:8080`n" -ForegroundColor White
Write-Host "Etat de l'application :" -ForegroundColor Cyan
kubectl get application -n argocd $appName
Write-Host "`nArgoCD va maintenant :" -ForegroundColor Cyan
Write-Host "   1. Surveiller votre repository Git" -ForegroundColor White
Write-Host "   2. Synchroniser automatiquement les changements" -ForegroundColor White
Write-Host "   3. Deployer dans le namespace soa-local`n" -ForegroundColor White
Write-Host "Commandes Utiles :" -ForegroundColor Cyan
Write-Host "   - Voir l'app :        kubectl get application $appName -n argocd" -ForegroundColor White
Write-Host "   - Forcer un sync :    kubectl -n argocd patch application $appName --type merge -p '{\"operation\":{\"sync\":{}}}'" -ForegroundColor White
Write-Host "   - Voir les ressources: kubectl get all -n soa-local" -ForegroundColor White
Write-Host "   - Supprimer l'app :   kubectl delete application $appName -n argocd`n" -ForegroundColor White
Write-Host "Maintenant, a chaque push Git, ArgoCD deploie automatiquement !" -ForegroundColor Green
