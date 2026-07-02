param(
    [switch]$PularTestes
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

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

Write-Host "== Contas Termometro: configuracao local =="

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "Java nao encontrado no PATH. Instale o JDK 21+ e tente novamente." -ForegroundColor Red
    exit 1
}

$javaVersion = (cmd.exe /c "java -version 2>&1" | Select-Object -First 1)
Write-Host "Java: $javaVersion"

New-Item -ItemType Directory -Force -Path ".\logs" | Out-Null
New-Item -ItemType Directory -Force -Path ".\backups-automaticos" | Out-Null

if (-not $PularTestes) {
    Write-Host "Rodando testes..."
    Invoke-Checked { .\gradlew.bat test } "Testes falharam."
}

Write-Host "Gerando aplicacao..."
Invoke-Checked { .\gradlew.bat bootJar } "Nao foi possivel gerar a aplicacao."

Write-Host ""
Write-Host "Configuracao concluida." -ForegroundColor Green
Write-Host "Use .\usar.bat para abrir a aplicacao."
