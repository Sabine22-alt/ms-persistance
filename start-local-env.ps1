# 🚀 Démarrage de l'Environnement Local Complet
# Ce script démarre tous les services avec Docker Compose
# et affiche les URLs accessibles depuis votre navigateur

Write-Host "╔═══════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║    🚀 RecipeYouLove - Démarrage Environnement Local  ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Vérifier que Docker est en cours d'exécution
Write-Host "🐳 Vérification de Docker..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "✅ Docker est en cours d'exécution`n" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker n'est pas en cours d'exécution!" -ForegroundColor Red
    Write-Host "   Veuillez démarrer Docker Desktop et réessayer.`n" -ForegroundColor Yellow
    exit 1
}

# Vérifier que le fichier .env existe
if (-not (Test-Path ".env")) {
    Write-Host "⚠️  Fichier .env non trouvé, création avec valeurs par défaut...`n" -ForegroundColor Yellow
    @"
# MySQL Configuration
MYSQL_ROOT_PASSWORD=password
MYSQL_DATABASE=testdb
MYSQL_USERNAME=root
MYSQL_PORT=3306

# PhpMyAdmin Configuration
PHPMYADMIN_PORT=8081

# MongoDB Configuration
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=password
MONGO_PORT=27017

# Mongo Express Configuration
MONGO_EXPRESS_USERNAME=admin
MONGO_EXPRESS_PASSWORD=password
MONGO_EXPRESS_PORT=8082

# MinIO Configuration
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001
"@ | Out-File -FilePath ".env" -Encoding UTF8
    Write-Host "✅ Fichier .env créé`n" -ForegroundColor Green
}

# Charger les variables d'environnement
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

# Arrêter les conteneurs existants si nécessaire
Write-Host "🛑 Nettoyage des conteneurs existants..." -ForegroundColor Yellow
docker-compose down 2>$null
Write-Host "✅ Nettoyage terminé`n" -ForegroundColor Green

# Build de l'application Spring Boot
Write-Host "🔨 Build de l'application Spring Boot..." -ForegroundColor Yellow
Write-Host "   (Cette étape peut prendre quelques minutes)`n" -ForegroundColor Gray

$buildOutput = mvn clean package -DskipTests 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Échec du build Maven!" -ForegroundColor Red
    Write-Host "   Consultez les logs ci-dessus pour plus de détails`n" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Build réussi`n" -ForegroundColor Green

# Build de l'image Docker
Write-Host "🐳 Construction de l'image Docker..." -ForegroundColor Yellow
docker build -t ms-persistance:latest . | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Échec du build Docker!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Image Docker construite`n" -ForegroundColor Green

# Démarrer les services avec Docker Compose
Write-Host "🚀 Démarrage des services..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Échec du démarrage des services!" -ForegroundColor Red
    exit 1
}

Write-Host "`n⏳ Attente du démarrage complet des services (30 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Vérifier l'état des services
Write-Host "`n📊 État des services:" -ForegroundColor Cyan
docker-compose ps

# Récupérer l'adresse IP locale
$localIP = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.InterfaceAlias -notmatch 'Loopback' -and $_.IPAddress -notmatch '^169\.' } | Select-Object -First 1).IPAddress
if (-not $localIP) { $localIP = "localhost" }

# Lire les ports depuis .env
$env = @{}
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $env[$matches[1].Trim()] = $matches[2].Trim()
    }
}

Write-Host "`n╔═══════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║         🌐 URLS ACCESSIBLES DEPUIS VOTRE PC          ║" -ForegroundColor Green
Write-Host "╚═══════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

Write-Host "🚀 API Spring Boot (ms-persistance)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "   📍 Home:            http://localhost:8090/" -ForegroundColor White
Write-Host "   📍 Health:          http://localhost:8090/health" -ForegroundColor White
Write-Host "   📍 Status:          http://localhost:8090/api/status" -ForegroundColor White
Write-Host "   📍 Database Test:   http://localhost:8090/api/database/test" -ForegroundColor White
Write-Host "   📍 Actuator Health: http://localhost:8090/actuator/health" -ForegroundColor White
Write-Host "   📍 Depuis réseau:   http://${localIP}:8090/" -ForegroundColor Gray
Write-Host ""

Write-Host "💾 phpMyAdmin (Interface MySQL)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "   📍 URL:      http://localhost:$($env['PHPMYADMIN_PORT'])" -ForegroundColor White
Write-Host "   👤 User:     root" -ForegroundColor White
Write-Host "   🔑 Password: $($env['MYSQL_ROOT_PASSWORD'])" -ForegroundColor White
Write-Host "   💽 Database: $($env['MYSQL_DATABASE'])" -ForegroundColor White
Write-Host ""

Write-Host "🍃 Mongo Express (Interface MongoDB)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "   📍 URL:      http://localhost:$($env['MONGO_EXPRESS_PORT'])" -ForegroundColor White
Write-Host "   👤 User:     $($env['MONGO_EXPRESS_USERNAME'])" -ForegroundColor White
Write-Host "   🔑 Password: $($env['MONGO_EXPRESS_PASSWORD'])" -ForegroundColor White
Write-Host ""

Write-Host "📦 MinIO (Stockage S3)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "   📍 Console:  http://localhost:$($env['MINIO_CONSOLE_PORT'])" -ForegroundColor White
Write-Host "   📍 API:      http://localhost:$($env['MINIO_API_PORT'])" -ForegroundColor White
Write-Host "   👤 User:     $($env['MINIO_ROOT_USER'])" -ForegroundColor White
Write-Host "   🔑 Password: $($env['MINIO_ROOT_PASSWORD'])" -ForegroundColor White
Write-Host ""

Write-Host "🗄️  Connexions Directes aux Bases de Données" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "   MySQL:   localhost:$($env['MYSQL_PORT'])" -ForegroundColor White
Write-Host "   MongoDB: localhost:$($env['MONGO_PORT'])" -ForegroundColor White
Write-Host ""

Write-Host "╔═══════════════════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║              📋 COMMANDES UTILES                      ║" -ForegroundColor Yellow
Write-Host "╚═══════════════════════════════════════════════════════╝" -ForegroundColor Yellow
Write-Host ""
Write-Host "   🔍 Voir les logs:        docker-compose logs -f" -ForegroundColor White
Write-Host "   🔍 Logs d'un service:    docker-compose logs -f ms-persistance" -ForegroundColor White
Write-Host "   🛑 Arrêter:              docker-compose stop" -ForegroundColor White
Write-Host "   🗑️  Tout supprimer:       docker-compose down -v" -ForegroundColor White
Write-Host "   🔄 Redémarrer:           docker-compose restart" -ForegroundColor White
Write-Host "   📊 État:                 docker-compose ps" -ForegroundColor White
Write-Host ""

Write-Host "✨ Environnement prêt! Ouvrez les URLs dans votre navigateur." -ForegroundColor Green
Write-Host ""

# Proposer d'ouvrir le navigateur
$response = Read-Host "Voulez-vous ouvrir l'API dans le navigateur maintenant? (O/n)"
if ($response -ne "n" -and $response -ne "N") {
    Start-Process "http://localhost:8080/health"
}

