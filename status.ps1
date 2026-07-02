param(
    [int]$Porta = 17321
)

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root
$Url = "http://localhost:$Porta"

Write-Host "== Status Contas Termometro =="

try {
    $saude = Invoke-RestMethod "$Url/api/sistema/saude" -TimeoutSec 3
    Write-Host "Aplicacao: $($saude.status) em $Url" -ForegroundColor Green
} catch {
    Write-Host "Aplicacao indisponivel em $Url" -ForegroundColor Yellow
    exit 0
}

try {
    $backup = Invoke-RestMethod "$Url/api/backups/info" -TimeoutSec 3
    Write-Host "Banco: $($backup.caminhoBanco)"
    Write-Host "Schema: V$($backup.schemaVersion)"
    Write-Host "Tamanho: $($backup.tamanhoBytes) bytes"
    Write-Host "Atualizado em: $($backup.atualizadoEm)"
} catch {
    Write-Host "Nao foi possivel consultar informacoes do banco." -ForegroundColor Yellow
}

Write-Host ""
git status --short
