# Start Master Node (PC 1)
$ErrorActionPreference = "Continue"

$workspaceRoot = (Get-Item -Path "$PSScriptRoot\..").FullName
Set-Location $workspaceRoot

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "     DC-LAB CLUSTER MASTER NODE LAUNCHER          " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# Detect Local LAN IPv4 address
$detectedIp = (Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue | 
    Where-Object { $_.InterfaceAlias -notmatch "Loopback|vEthernet|Virtual|VMware" -and $_.IPAddress -notmatch "^127\.|^169\.254\." } | 
    Select-Object -ExpandProperty IPAddress -First 1)

if (-not $detectedIp) {
    $detectedIp = "127.0.0.1"
}

Write-Host "Detected Local LAN IP : $detectedIp" -ForegroundColor Yellow
$masterIp = Read-Host "Press ENTER to use [$detectedIp], or type the Master LAN IP"
if ([string]::IsNullOrWhiteSpace($masterIp)) {
    $masterIp = $detectedIp
}

Write-Host "`nConfiguring Master with IP: $masterIp" -ForegroundColor Green
Write-Host "RMI Registry Port : 1099" -ForegroundColor Green
Write-Host "REST API Port     : 8080" -ForegroundColor Green
Write-Host "WebSocket Port    : 8080/ws" -ForegroundColor Green
Write-Host "Frontend Port     : 3000" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan

$jarPath = Join-Path $workspaceRoot "backend\target\dc-lab-backend-1.0.0-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "JAR file not found! Compiling project first..." -ForegroundColor Yellow
    & "$PSScriptRoot\build.ps1"
}

# Start Frontend in a new PowerShell window
Write-Host "`nLaunching React Frontend on port 3000..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$workspaceRoot\frontend'; Write-Host 'Starting React Frontend on 0.0.0.0:3000...' -ForegroundColor Cyan; npm run dev -- --host 0.0.0.0"

# Start Backend Master in current window
Write-Host "Launching Spring Boot Master Scheduler..." -ForegroundColor Cyan
& java "-Djava.rmi.server.hostname=$masterIp" -jar "$jarPath" "--app.mode=lan" "--app.master.ip=$masterIp" "--server.port=8080"
