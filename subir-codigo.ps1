param(
    [string]$Mensagem,
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

if ([string]::IsNullOrWhiteSpace($Mensagem)) {
    $Mensagem = Read-Host "Mensagem do commit"
}

if ([string]::IsNullOrWhiteSpace($Mensagem)) {
    Write-Host "Commit cancelado: informe uma mensagem." -ForegroundColor Red
    exit 1
}

$branch = git branch --show-current
if ([string]::IsNullOrWhiteSpace($branch)) {
    Write-Host "Nao foi possivel identificar a branch atual." -ForegroundColor Red
    exit 1
}

Write-Host "Branch: $branch"
Write-Host "Alteracoes atuais:"
git status --short

if (-not $PularTestes) {
    Write-Host "Rodando testes antes do commit..."
    Invoke-Checked { .\gradlew.bat test } "Testes falharam. Corrija antes de subir codigo."
}

Invoke-Checked { git add -A } "Nao foi possivel preparar alteracoes."

$blocked = git diff --cached --name-only | Select-String -Pattern "(\.db$|\.sqlite$|\.sqlite3$|\.ctbackup$|^backups-automaticos/|^logs/|^data/)"
if ($blocked) {
    Write-Host "Commit bloqueado: ha dados locais/sensiveis staged." -ForegroundColor Red
    $blocked
    git reset
    exit 1
}

$staged = git diff --cached --name-only
if (-not $staged) {
    Write-Host "Nada para commitar."
    exit 0
}

Invoke-Checked { git commit -m $Mensagem } "Commit falhou."
Invoke-Checked { git push origin $branch } "Push falhou."

Write-Host "Codigo enviado para origin/$branch." -ForegroundColor Green
