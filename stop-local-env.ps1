# 🛑 Arrêt de l'Environnement Local

Write-Host "╔═══════════════════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║    🛑 Arrêt de l'Environnement RecipeYouLove         ║" -ForegroundColor Yellow
Write-Host "╚═══════════════════════════════════════════════════════╝" -ForegroundColor Yellow
Write-Host ""

$choice = Read-Host @"
Que voulez-vous faire ?
  1. Arrêter les services (les conteneurs restent)
  2. Arrêter et supprimer les conteneurs
  3. Arrêter, supprimer les conteneurs ET les volumes (⚠️ perte de données)

Votre choix (1/2/3)
"@

Write-Host ""

switch ($choice) {
    "1" {
        Write-Host "🛑 Arrêt des services..." -ForegroundColor Yellow
        docker-compose stop
        Write-Host "✅ Services arrêtés. Utilisez 'docker-compose start' pour redémarrer.`n" -ForegroundColor Green
    }
    "2" {
        Write-Host "🗑️  Arrêt et suppression des conteneurs..." -ForegroundColor Yellow
        docker-compose down
        Write-Host "✅ Conteneurs supprimés. Les volumes sont conservés.`n" -ForegroundColor Green
    }
    "3" {
        Write-Host "⚠️  ATTENTION: Cette action supprimera TOUTES les données!" -ForegroundColor Red
        $confirm = Read-Host "Êtes-vous sûr ? (tapez 'OUI' pour confirmer)"
        if ($confirm -eq "OUI") {
            Write-Host "🗑️  Suppression complète..." -ForegroundColor Yellow
            docker-compose down -v
            Write-Host "✅ Tout a été supprimé (conteneurs + volumes).`n" -ForegroundColor Green
        } else {
            Write-Host "❌ Opération annulée.`n" -ForegroundColor Yellow
        }
    }
    default {
        Write-Host "❌ Choix invalide. Aucune action effectuée.`n" -ForegroundColor Red
    }
}

Write-Host "📊 État actuel des conteneurs:" -ForegroundColor Cyan
docker ps -a --filter "name=local"

