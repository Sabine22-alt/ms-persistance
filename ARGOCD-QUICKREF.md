# ArgoCD - Aide-Memoire Rapide

## Setup Initial

```powershell
# 1. Builder l'image
.\build-and-load-image.ps1

# 2. Installer ArgoCD (3-5 min)
.\setup-argocd.ps1

# 3. SAUVEGARDER le mot de passe affiche !

# 4. Configurer l'app
.\setup-argocd-app.ps1
```

## Recuperer le Mot de Passe

```powershell
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | ForEach-Object { [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_)) }
```

## Acces Interface

```
URL:      https://localhost:8080
Username: admin
Password: (commande ci-dessus)
```

## Workflow Quotidien

```powershell
# 1. Modifier code
# 2. .\build-and-load-image.ps1
# 3. git commit && git push
# 4. ArgoCD sync auto (< 3 min)
# 5. kubectl get pods -n soa-local
```

## Commandes Utiles

```powershell
# Forcer sync
kubectl -n argocd patch application recipeyoulove --type merge -p '{"operation":{"sync":{}}}'

# Voir l'app
kubectl get application -n argocd

# Voir les pods
kubectl get pods -n soa-local

# Relancer port-forward
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

## Problemes Courants

### ErrImageNeverPull
```powershell
.\build-and-load-image.ps1
kubectl delete pods --all -n soa-local
```

### Can't access https://localhost:8080
```powershell
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

### Trop long a demarrer
- Normal: 3-5 minutes au premier lancement
- Verifier: `kubectl get pods -n argocd`

## Pour GitHub Actions

### 1. Creer Token
```bash
argocd login localhost:8080 --username admin
argocd account generate-token --account ci-cd
```

### 2. Ajouter Secrets GitHub
```
ARGOCD_SERVER     → https://votre-argocd.com
ARGOCD_AUTH_TOKEN → (token ci-dessus)
```

## Desinstaller ArgoCD

```powershell
kubectl delete namespace argocd
```

