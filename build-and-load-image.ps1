# Build et Load Image pour ArgoCD Local
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "     Build et Load Image pour Kubernetes Local" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
# Verifier le contexte
$context = kubectl config current-context
Write-Host "Contexte actuel: $context" -ForegroundColor Yellow
if ($context -ne "docker-desktop") {
    Write-Host "ATTENTION - Vous n'etes pas sur docker-desktop!" -ForegroundColor Red
    Write-Host "Ce script est pour Kubernetes local uniquement.`n" -ForegroundColor Yellow
    exit 1
}
Write-Host ""
# Build Maven
Write-Host "1. Build Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERREUR - Build Maven echoue!" -ForegroundColor Red
    exit 1
}
Write-Host "OK - Build Maven termine`n" -ForegroundColor Green
# Build Docker Image
Write-Host "2. Build Docker Image..." -ForegroundColor Yellow
docker build -t ms-persistance:latest .
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERREUR - Build Docker echoue!" -ForegroundColor Red
    exit 1
}
Write-Host "OK - Image Docker creee`n" -ForegroundColor Green
# Verifier l'image
Write-Host "3. Verification de l'image..." -ForegroundColor Yellow
$imageExists = docker images ms-persistance:latest --format "{{.Repository}}:{{.Tag}}"
if ($imageExists) {
    Write-Host "OK - Image trouvee: $imageExists`n" -ForegroundColor Green
} else {
    Write-Host "ERREUR - Image non trouvee!" -ForegroundColor Red
    exit 1
}
# Pour Docker Desktop, l'image est automatiquement disponible dans Kubernetes
Write-Host "4. Image disponible pour Kubernetes..." -ForegroundColor Yellow
Write-Host "OK - Docker Desktop partage automatiquement les images avec Kubernetes`n" -ForegroundColor Green
# Redemarrer les pods si ArgoCD est installe
Write-Host "5. Redemarrage des pods (si necessaire)..." -ForegroundColor Yellow
$pods = kubectl get pods -n ms-persistance --no-headers 2>$null
if ($pods) {
    Write-Host "Suppression des pods existants pour forcer le restart..." -ForegroundColor Yellow
    kubectl delete pods --all -n ms-persistance
    Start-Sleep -Seconds 5
    Write-Host "OK - Pods redemarres`n" -ForegroundColor Green
} else {
    Write-Host "Aucun pod a redemarrer`n" -ForegroundColor Gray
}
Write-Host "================================================================" -ForegroundColor Green
Write-Host "            IMAGE PRETE POUR ARGOCD" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "L'image ms-persistance:latest est maintenant disponible." -ForegroundColor Cyan
Write-Host ""
Write-Host "Prochaines etapes:" -ForegroundColor Yellow
Write-Host "1. Si ArgoCD n'est pas encore configure:" -ForegroundColor White
Write-Host "   .\setup-argocd.ps1" -ForegroundColor White
Write-Host "   .\setup-argocd-app.ps1" -ForegroundColor White
Write-Host ""
Write-Host "2. Si ArgoCD est deja configure:" -ForegroundColor White
Write-Host "   - Attendez la synchronisation automatique (< 3 min)" -ForegroundColor White
Write-Host "   - Ou forcez: kubectl -n argocd patch application recipeyoulove --type merge -p '{\"operation\":{\"sync\":{}}}'" -ForegroundColor White
Write-Host ""
Write-Host "3. Verifier les pods:" -ForegroundColor White
Write-Host "   kubectl get pods -n soa-local" -ForegroundColor White
