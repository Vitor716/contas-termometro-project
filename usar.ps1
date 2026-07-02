param(
    [switch]$Rebuild,
    [switch]$NaoAbrirNavegador,
    [int]$Porta = 17321
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$PidFile = Join-Path $Root ".app.pid"
$Jar = Join-Path $Root "build\libs\contas-termometro-project-0.1.0-SNAPSHOT.jar"
$Url = "http://localhost:$Porta"

function Invoke-Checked {
    param(
        [scriptblock]$Command,
        [string]$ErrorMessage
    )
    & $Command
    if ($LASTEXITCODE -ne 0) {
        Write-Host $ErrorMessage -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

function Test-App {
    try {
        Invoke-RestMethod "$Url/api/sistema/saude" -TimeoutSec 2 | Out-Null
        return $true
    } catch {
        return $false
    }
}

New-Item -ItemType Directory -Force -Path ".\logs" | Out-Null

if (Test-App) {
    Write-Host "Aplicacao ja esta rodando em $Url" -ForegroundColor Green
    if (-not $NaoAbrirNavegador) { Start-Process $Url }
    exit 0
}

if ($Rebuild -or -not (Test-Path $Jar)) {
    Write-Host "Gerando aplicacao..."
    Invoke-Checked { .\gradlew.bat bootJar } "Nao foi possivel gerar a aplicacao."
}

Write-Host "Iniciando aplicacao em $Url ..."
$process = Start-Process `
    -FilePath "java" `
    -ArgumentList @("-jar", $Jar) `
    -WorkingDirectory $Root `
    -WindowStyle Hidden `
    -PassThru

Set-Content -Path $PidFile -Value $process.Id

$ready = $false
for ($i = 1; $i -le 30; $i++) {
    Start-Sleep -Seconds 1
    if ($process.HasExited) { break }
    if (Test-App) {
        $ready = $true
        break
    }
}

if (-not $ready) {
    Write-Host "A aplicacao nao respondeu. Rode .\gradlew.bat bootRun para ver o erro detalhado." -ForegroundColor Red
    exit 1
}

Write-Host "Aplicacao pronta: $Url" -ForegroundColor Green
if (-not $NaoAbrirNavegador) {
    Start-Process $Url
}
