$ErrorActionPreference = "SilentlyContinue"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$PidFile = Join-Path $Root ".app.pid"
$stopped = $false

if (Test-Path $PidFile) {
    $appPid = Get-Content $PidFile | Select-Object -First 1
    if ($appPid -match "^\d+$") {
        Stop-Process -Id ([int]$appPid) -Force
        $stopped = $true
    }
    Remove-Item $PidFile -Force
}

$ports = @(17321, 8081)
foreach ($port in $ports) {
    $listeners = netstat -ano | Select-String ":$port" | ForEach-Object { ($_ -split "\s+")[-1] } | Select-Object -Unique
    foreach ($processId in $listeners) {
        if ($processId -match "^\d+$") {
            Stop-Process -Id ([int]$processId) -Force
            $stopped = $true
        }
    }
}

if ($stopped) {
    Write-Host "Aplicacao parada." -ForegroundColor Green
} else {
    Write-Host "Nenhuma aplicacao rodando nas portas 17321 ou 8081."
}
