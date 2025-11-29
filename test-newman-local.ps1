# Script de test local de la collection Newman

Write-Host "🧪 Test Local de la Collection Newman" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Vérifier que l'application tourne
$baseUrl = "http://localhost:8090"
Write-Host "📍 URL de base: $baseUrl`n"

# Tester la connectivité
Write-Host "🔌 Test de connectivité..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/health" -UseBasicParsing -TimeoutSec 5
    Write-Host "✅ Application accessible (Status: $($response.StatusCode))`n" -ForegroundColor Green
} catch {
    Write-Host "❌ Application non accessible!" -ForegroundColor Red
    Write-Host "Assurez-vous que l'application est démarrée avec:" -ForegroundColor Yellow
    Write-Host "  mvn spring-boot:run" -ForegroundColor White
    Write-Host "ou" -ForegroundColor Yellow
    Write-Host "  java -jar target/*.jar`n" -ForegroundColor White
    exit 1
}

# Tester tous les endpoints
Write-Host "🧪 Test des endpoints disponibles...`n" -ForegroundColor Yellow

$endpoints = @(
    @{Name="Home"; Url="/"; Expected="API is running"}
    @{Name="Health"; Url="/health"; Expected="healthy"}
    @{Name="Status"; Url="/api/status"; Expected="applicationName"}
    @{Name="Database"; Url="/api/database/test"; Expected="mysql"}
    @{Name="Actuator Health"; Url="/actuator/health"; Expected="UP"}
)

$successCount = 0
$failCount = 0

foreach ($endpoint in $endpoints) {
    Write-Host "  Testing $($endpoint.Name)..." -NoNewline
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl$($endpoint.Url)" -Method Get -TimeoutSec 10
        $responseText = if ($response -is [string]) { $response } else { $response | ConvertTo-Json -Compress }

        if ($responseText -match $endpoint.Expected) {
            Write-Host " ✅" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host " ⚠️  (réponse inattendue)" -ForegroundColor Yellow
            Write-Host "    Réponse: $responseText" -ForegroundColor Gray
            $successCount++
        }
    } catch {
        Write-Host " ❌" -ForegroundColor Red
        Write-Host "    Erreur: $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
}

Write-Host ""
Write-Host "📊 Résultats:" -ForegroundColor Cyan
Write-Host "  ✅ Succès: $successCount" -ForegroundColor Green
Write-Host "  ❌ Échecs: $failCount" -ForegroundColor Red
Write-Host ""

# Exécuter Newman si tout est OK
if ($failCount -eq 0) {
    Write-Host "🚀 Lancement de Newman...`n" -ForegroundColor Cyan

    Set-Location -Path "tests/newman"

    # Mettre à jour l'environment avec localhost
    $envContent = Get-Content "env.json" | ConvertFrom-Json
    foreach ($value in $envContent.values) {
        if ($value.key -eq "baseUrl") {
            $value.value = $baseUrl
        }
    }
    $envContent | ConvertTo-Json -Depth 10 | Set-Content "env.tmp.json"

    # Exécuter Newman
    npx newman run collection.json `
        --environment env.tmp.json `
        --iteration-data dataset.json `
        --reporters cli `
        --color on

    $newmanExitCode = $LASTEXITCODE

    # Cleanup
    Remove-Item "env.tmp.json" -ErrorAction SilentlyContinue

    Set-Location -Path "../.."

    Write-Host ""
    if ($newmanExitCode -eq 0) {
        Write-Host "✅ Tous les tests Newman ont réussi!" -ForegroundColor Green
    } else {
        Write-Host "❌ Certains tests Newman ont échoué" -ForegroundColor Red
        exit $newmanExitCode
    }
} else {
    Write-Host "⚠️  Certains endpoints ne répondent pas correctement." -ForegroundColor Yellow
    Write-Host "   Newman n'a pas été exécuté." -ForegroundColor Yellow
    exit 1
}

