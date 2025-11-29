﻿# Setup ArgoCD Local (Docker Desktop Kubernetes)
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "     Installation ArgoCD sur Kubernetes Local" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
# Verifier Kubernetes
Write-Host "Verification de Kubernetes..." -ForegroundColor Yellow

# Verifier le contexte actuel
$currentContext = kubectl config current-context 2>$null

if ($currentContext -ne "docker-desktop") {
    Write-Host "ATTENTION - Contexte actuel: $currentContext" -ForegroundColor Yellow
    Write-Host "Ce n'est pas le contexte Docker Desktop local." -ForegroundColor Yellow
    Write-Host ""

    # Verifier si docker-desktop existe
    $contexts = kubectl config get-contexts -o name 2>$null
    if ($contexts -match "docker-desktop") {
        Write-Host "Basculement vers docker-desktop..." -ForegroundColor Yellow
        kubectl config use-context docker-desktop 2>$null | Out-Null
        Start-Sleep -Seconds 2
        $currentContext = "docker-desktop"
        Write-Host "OK - Contexte bascule vers docker-desktop`n" -ForegroundColor Green
    } else {
        Write-Host "ERREUR - Contexte docker-desktop introuvable!" -ForegroundColor Red
        Write-Host "   Activez Kubernetes dans Docker Desktop:" -ForegroundColor Yellow
        Write-Host "   Docker Desktop > Settings > Kubernetes > Enable Kubernetes`n" -ForegroundColor White
        exit 1
    }
}

# Tester la connexion
try {
    $testConnection = kubectl cluster-info 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Connection failed"
    }
    Write-Host "OK - Kubernetes actif (contexte: $currentContext)`n" -ForegroundColor Green
} catch {
    Write-Host "ERREUR - Impossible de se connecter a Kubernetes!" -ForegroundColor Red
    Write-Host "   Verifiez que Kubernetes est demarre dans Docker Desktop." -ForegroundColor Yellow
    Write-Host "   Docker Desktop > Settings > Kubernetes > Enable Kubernetes`n" -ForegroundColor White
    exit 1
}
# Creer namespace ArgoCD
Write-Host "Creation du namespace ArgoCD..." -ForegroundColor Yellow
kubectl create namespace argocd 2>$null
Write-Host "OK - Namespace cree`n" -ForegroundColor Green
# Installer ArgoCD
Write-Host "Installation d'ArgoCD..." -ForegroundColor Yellow
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
Write-Host "Attente du demarrage d'ArgoCD (30 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
# Attendre que tous les pods soient prets
Write-Host "Verification du deploiement..." -ForegroundColor Yellow
kubectl wait --for=condition=available --timeout=180s deployment --all -n argocd
Write-Host "`nOK - ArgoCD installe avec succes!`n" -ForegroundColor Green
# Recuperer le mot de passe admin initial
Write-Host "Recuperation du mot de passe admin..." -ForegroundColor Yellow
$argoPassword = kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | ForEach-Object { [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_)) }
# Exposer ArgoCD en port-forward
Write-Host "Demarrage du port-forward..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "kubectl port-forward svc/argocd-server -n argocd 8080:443"
Start-Sleep -Seconds 3
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "            ARGOCD PRET A L'EMPLOI" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Interface Web ArgoCD :" -ForegroundColor Cyan
Write-Host "   URL :      https://localhost:8080" -ForegroundColor White
Write-Host "   Username : admin" -ForegroundColor White
Write-Host "   Password : $argoPassword" -ForegroundColor White
Write-Host ""
Write-Host "ATTENTION : Accepter le certificat auto-signe dans le navigateur`n" -ForegroundColor Yellow
Write-Host "Commandes Utiles :" -ForegroundColor Cyan
Write-Host "   - Voir les apps :     kubectl get applications -n argocd" -ForegroundColor White
Write-Host "   - Logs ArgoCD :       kubectl logs -n argocd deployment/argocd-server" -ForegroundColor White
Write-Host "   - Desinstaller :      kubectl delete namespace argocd`n" -ForegroundColor White
Write-Host "Prochaine etape : Creer une Application ArgoCD" -ForegroundColor Cyan
Write-Host "   Executer : .\setup-argocd-app.ps1`n" -ForegroundColor White
# Proposer d'ouvrir le navigateur
$response = Read-Host "Voulez-vous ouvrir ArgoCD dans le navigateur ? (O/n)"
if ($response -ne "n" -and $response -ne "N") {
    Start-Process "https://localhost:8080"
}
