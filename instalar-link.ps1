param(
    [int]$Porta = 17321,
    [switch]$Rebuild
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$TaskName = "ContasTermometroLocal"
$Url = "http://localhost:$Porta"
$ProtocolUrl = "contas-termometro://abrir"
$Jar = Join-Path $Root "build\libs\contas-termometro-project-0.1.0-SNAPSHOT.jar"
$LinkFile = Join-Path $Root "Contas Termometro.url"
$DesktopLink = Join-Path ([Environment]::GetFolderPath("Desktop")) "Contas Termometro.url"
$StartupDir = [Environment]::GetFolderPath("Startup")
$StartupFile = Join-Path $StartupDir "Contas Termometro - iniciar.bat"

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

function Write-UrlShortcut {
    param(
        [string]$Path,
        [string]$TargetUrl
    )
    @"
[InternetShortcut]
URL=$TargetUrl
IconFile=$env:SystemRoot\System32\SHELL32.dll
IconIndex=220
"@ | Set-Content -Path $Path -Encoding ASCII
}

function Register-Protocol {
    $protocolRoot = "HKCU:\Software\Classes\contas-termometro"
    New-Item -Path $protocolRoot -Force | Out-Null
    New-ItemProperty -Path $protocolRoot -Name "(default)" -Value "URL:Contas Termometro" -Force | Out-Null
    New-ItemProperty -Path $protocolRoot -Name "URL Protocol" -Value "" -Force | Out-Null
    New-Item -Path "$protocolRoot\DefaultIcon" -Force | Out-Null
    New-ItemProperty -Path "$protocolRoot\DefaultIcon" -Name "(default)" -Value "$env:SystemRoot\System32\SHELL32.dll,220" -Force | Out-Null
    New-Item -Path "$protocolRoot\shell\open\command" -Force | Out-Null
    New-ItemProperty `
        -Path "$protocolRoot\shell\open\command" `
        -Name "(default)" `
        -Value "`"$Root\abrir-protocolo.bat`" `"%1`"" `
        -Force | Out-Null
}

Write-Host "== Instalando acesso simples do Contas Termometro =="

New-Item -ItemType Directory -Force -Path ".\logs" | Out-Null

if ($Rebuild -or -not (Test-Path $Jar)) {
    Write-Host "Gerando aplicacao..."
    Invoke-Checked { .\gradlew.bat bootJar } "Nao foi possivel gerar a aplicacao."
} else {
    Write-Host "Usando aplicacao ja gerada: $Jar"
}

$protocolRegistered = $false
try {
    Register-Protocol
    $protocolRegistered = $true
    Write-Host "Link local registrado: $ProtocolUrl"
} catch {
    Write-Host "Nao foi possivel registrar o protocolo local. O atalho vai apontar para $Url." -ForegroundColor Yellow
}

$shortcutTarget = if ($protocolRegistered) { $ProtocolUrl } else { $Url }
Write-UrlShortcut $LinkFile $shortcutTarget
try {
    Write-UrlShortcut $DesktopLink $shortcutTarget
    $createdDesktopLink = $true
} catch {
    Write-Host "Nao foi possivel criar atalho na area de trabalho. O link ficou em: $LinkFile" -ForegroundColor Yellow
    $createdDesktopLink = $false
}

$registeredTask = $false
try {
    $identity = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    $action = New-ScheduledTaskAction `
        -Execute "powershell.exe" `
        -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$Root\iniciar-servidor.ps1`" -Porta $Porta" `
        -WorkingDirectory $Root
    $trigger = New-ScheduledTaskTrigger -AtLogOn
    $principal = New-ScheduledTaskPrincipal -UserId $identity -LogonType Interactive -RunLevel LeastPrivilege
    $settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -MultipleInstances IgnoreNew

    Register-ScheduledTask `
        -TaskName $TaskName `
        -Action $action `
        -Trigger $trigger `
        -Principal $principal `
        -Settings $settings `
        -Description "Inicia o Contas Termometro local no login." `
        -Force | Out-Null

    Start-ScheduledTask -TaskName $TaskName
    $registeredTask = $true
    Write-Host "Tarefa agendada criada: $TaskName"
} catch {
    Write-Host "Nao foi possivel criar tarefa agendada. Usando pasta Inicializar." -ForegroundColor Yellow
    try {
        "@echo off`r`npowershell.exe -NoProfile -ExecutionPolicy Bypass -File `"$Root\iniciar-servidor.ps1`" -Porta $Porta`r`n" |
            Set-Content -Path $StartupFile -Encoding ASCII
        powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$Root\iniciar-servidor.ps1" -Porta $Porta
    } catch {
        Write-Host "Tambem nao foi possivel configurar a pasta Inicializar neste ambiente." -ForegroundColor Yellow
        Write-Host "O link foi criado, mas use .\usar.bat para iniciar o servidor quando necessario." -ForegroundColor Yellow
    }
}

$ready = $false
for ($i = 1; $i -le 30; $i++) {
    Start-Sleep -Seconds 1
    try {
        Invoke-RestMethod "$Url/api/sistema/saude" -TimeoutSec 2 | Out-Null
        $ready = $true
        break
    } catch {
    }
}

if ($ready) {
    Write-Host "Pronto. Agora basta abrir: $Url" -ForegroundColor Green
    if ($createdDesktopLink) {
        Write-Host "Atalho criado: $DesktopLink"
    } else {
        Write-Host "Atalho criado: $LinkFile"
    }
    Start-Process $Url
} else {
    if ($protocolRegistered) {
        Write-Host "Instalado. Clique em 'Contas Termometro.url' para iniciar e abrir a aplicacao." -ForegroundColor Green
    } else {
        Write-Host "Instalado, mas a aplicacao ainda nao respondeu. Rode .\usar.bat para ver o erro." -ForegroundColor Yellow
    }
}

if ($registeredTask) {
    Write-Host "Ela tambem vai iniciar automaticamente quando voce entrar no Windows."
}
