param(
    [switch]$ManterAtalho
)

$ErrorActionPreference = "SilentlyContinue"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$TaskName = "ContasTermometroLocal"
$ProtocolRoot = "HKCU:\Software\Classes\contas-termometro"
$DesktopLink = Join-Path ([Environment]::GetFolderPath("Desktop")) "Contas Termometro.url"
$StartupFile = Join-Path ([Environment]::GetFolderPath("Startup")) "Contas Termometro - iniciar.bat"

Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
Remove-Item $ProtocolRoot -Recurse -Force
Remove-Item $StartupFile -Force
if (-not $ManterAtalho) {
    Remove-Item $DesktopLink -Force
}

.\parar.bat
Write-Host "Acesso automatico removido."
