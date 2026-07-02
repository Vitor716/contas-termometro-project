param(
    [int]$Porta = 17321
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$PidFile = Join-Path $Root ".app.pid"
$Jar = Join-Path $Root "build\libs\contas-termometro-project-0.1.0-SNAPSHOT.jar"
$Url = "http://localhost:$Porta"

function Test-App {
    try {
        Invoke-RestMethod "$Url/api/sistema/saude" -TimeoutSec 2 | Out-Null
        return $true
    } catch {
        return $false
    }
}

if (Test-App) {
    exit 0
}

if (-not (Test-Path $Jar)) {
    & "$Root\gradlew.bat" bootJar
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

$javaw = Join-Path (Split-Path (Get-Command java.exe).Source -Parent) "javaw.exe"
if (-not (Test-Path $javaw)) {
    $javaw = "java"
}

$process = Start-Process `
    -FilePath $javaw `
    -ArgumentList @("-jar", $Jar) `
    -WorkingDirectory $Root `
    -WindowStyle Hidden `
    -PassThru

Set-Content -Path $PidFile -Value $process.Id
