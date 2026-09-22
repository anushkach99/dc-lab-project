# Start Worker Node (PC 2, PC 3, PC 4)
param (
    [string]$MasterIp = "",
    [string]$WorkerId = "",
    [string]$NodeType = "",
    [int]$NodeId = 0,
    [double]$Speed = 0.0
)

$workspaceRoot = (Get-Item -Path "$PSScriptRoot\..").FullName
Set-Location $workspaceRoot

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "       DC-LAB CLUSTER WORKER NODE LAUNCHER        " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# 1. Ask for Master IP if not supplied
if ([string]::IsNullOrWhiteSpace($MasterIp)) {
    $defaultMasterIp = "192.168.1.100"
    # Check if config.properties has master.ip
    $configFile = Join-Path $workspaceRoot "config\config.properties"
    if (Test-Path $configFile) {
        $props = Get-Content $configFile | ConvertFrom-StringData
        if ($props.'master.ip') { $defaultMasterIp = $props.'master.ip' }
    }
    $inputIp = Read-Host "Enter Master PC IP address (default: $defaultMasterIp)"
    if ([string]::IsNullOrWhiteSpace($inputIp)) {
        $MasterIp = $defaultMasterIp
    } else {
        $MasterIp = $inputIp
    }
}

# 2. Select Worker Profile if not provided
if ([string]::IsNullOrWhiteSpace($WorkerId)) {
    Write-Host "`nSelect Worker Role for this PC:" -ForegroundColor Yellow
    Write-Host " [1] PC 2 -> Worker: Edge-01  (Node ID: 20, Type: EDGE,  Speed: 1.0x)"
    Write-Host " [2] PC 3 -> Worker: Edge-02  (Node ID: 30, Type: EDGE,  Speed: 2.0x)"
    Write-Host " [3] PC 4 -> Worker: Cloud-01 (Node ID: 40, Type: CLOUD, Speed: 5.0x)"
    Write-Host " [4] PC 5 -> Worker: Cloud-02 (Node ID: 50, Type: CLOUD, Speed: 8.0x)"
    Write-Host " [5] Custom Worker Configuration"
    $choice = Read-Host "Choose an option [1-5] (default: 1)"

    switch ($choice) {
        "2" {
            $WorkerId = "Edge-02"; $NodeType = "EDGE"; $NodeId = 30; $Speed = 2.0
        }
        "3" {
            $WorkerId = "Cloud-01"; $NodeType = "CLOUD"; $NodeId = 40; $Speed = 5.0
        }
        "4" {
            $WorkerId = "Cloud-02"; $NodeType = "CLOUD"; $NodeId = 50; $Speed = 8.0
        }
        "5" {
            $WorkerId = Read-Host "Worker ID (e.g. Edge-03)"
            $NodeType = Read-Host "Node Type (EDGE or CLOUD)"
            $NodeId = [int](Read-Host "Node ID (e.g. 60)")
            $Speed = [double](Read-Host "Speed Multiplier (e.g. 1.5)")
        }
        default {
            $WorkerId = "Edge-01"; $NodeType = "EDGE"; $NodeId = 20; $Speed = 1.0
        }
    }
}

$jarPath = Join-Path $workspaceRoot "backend\target\dc-lab-backend-1.0.0-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    $jarPath = Join-Path $workspaceRoot "backend\target\dc-lab-backend.jar"
}

if (-not (Test-Path $jarPath)) {
    Write-Host "ERROR: dc-lab-backend JAR not found in backend\target\" -ForegroundColor Red
    Write-Host "Please ensure the JAR was built on Master or copied here." -ForegroundColor Red
    exit 1
}

Write-Host "`nStarting Worker Node with Configuration:" -ForegroundColor Green
Write-Host "Master IP        : $MasterIp" -ForegroundColor Green
Write-Host "Worker ID        : $WorkerId" -ForegroundColor Green
Write-Host "Node Type        : $NodeType" -ForegroundColor Green
Write-Host "Node ID          : $NodeId" -ForegroundColor Green
Write-Host "Speed Multiplier : $Speed" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan

& java -jar "$jarPath" "--role=worker" "--master.ip=$MasterIp" "--worker.id=$WorkerId" "--worker.type=$NodeType" "--node.id=$NodeId" "--speed=$Speed"
