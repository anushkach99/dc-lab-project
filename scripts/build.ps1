# DC-Lab Build Script
$ErrorActionPreference = "Stop"

$workspaceRoot = (Get-Item -Path "$PSScriptRoot\..").FullName
Set-Location $workspaceRoot

$libDir = Join-Path $workspaceRoot "backend\target\lib"
$classesDir = Join-Path $workspaceRoot "backend\target\classes"
$jarFile = Join-Path $workspaceRoot "backend\target\dc-lab-backend-1.0.0-SNAPSHOT.jar"
$backendJar = Join-Path $workspaceRoot "backend\target\dc-lab-backend.jar"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Building DC-Lab Distributed Framework   " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Ensure target directory exists
New-Item -ItemType Directory -Path (Join-Path $workspaceRoot "backend\target") -Force | Out-Null

# 2. Extract dependency jars if lib directory doesn't exist
if (-not (Test-Path $libDir) -or (Get-ChildItem $libDir -Filter "*.jar").Count -eq 0) {
    Write-Host "Extracting dependency libraries..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $libDir -Force | Out-Null
    $extractDir = Join-Path $workspaceRoot "backend\target\temp_extracted"
    if (Test-Path $extractDir) { Remove-Item $extractDir -Recurse -Force }
    New-Item -ItemType Directory -Path $extractDir -Force | Out-Null
    Set-Location $extractDir
    & jar -xf "$jarFile"
    Set-Location $workspaceRoot
    Copy-Item "$extractDir\BOOT-INF\lib\*" -Destination $libDir -Force
    Remove-Item $extractDir -Recurse -Force
}

# 3. Build classpath string
$cpJars = (Get-ChildItem -Path $libDir -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"

# 4. Find all Java source files
$javaFiles = Get-ChildItem -Path "$workspaceRoot\backend\src\main\java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
Write-Host "Found $($javaFiles.Count) Java source files." -ForegroundColor Cyan

# 5. Write javac argfile
$buildDir = Join-Path $workspaceRoot "backend\target"
$sourcesFile = Join-Path $buildDir "sources.txt"
$javaFiles | Set-Content -Path $sourcesFile -Encoding ASCII

if (-not (Test-Path $classesDir)) {
    New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
}

Write-Host "Compiling Java classes with javac (--release 17 -parameters)..." -ForegroundColor Yellow
& javac --release 17 -parameters -cp "$cpJars" -d "$classesDir" "@$sourcesFile"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation FAILED with error code $LASTEXITCODE" -ForegroundColor Red
    exit 1
}
Write-Host "Compilation SUCCEEDED!" -ForegroundColor Green

# 6. Copy resources into classes directory
$resourcesDir = "$workspaceRoot\backend\src\main\resources"
if (Test-Path $resourcesDir) {
    Copy-Item "$resourcesDir\*" -Destination $classesDir -Recurse -Force
}

# 7. Update the Fat JAR
Write-Host "Repackaging Spring Boot Fat JAR & Standalone Worker classes..." -ForegroundColor Yellow
$repackDir = Join-Path $workspaceRoot "backend\target\repack"
if (Test-Path $repackDir) { Remove-Item $repackDir -Recurse -Force }
New-Item -ItemType Directory -Path $repackDir -Force | Out-Null

Set-Location $repackDir
& jar -xf "$jarFile"

# Ensure root com directory is removed from Fat JAR so Spring Boot JarLauncher uses LaunchedURLClassLoader
if (Test-Path "$repackDir\com") { Remove-Item "$repackDir\com" -Recurse -Force }

# Copy updated classes into BOOT-INF/classes
Copy-Item "$classesDir\*" -Destination "$repackDir\BOOT-INF\classes" -Recurse -Force

# Repackage jar to a temporary jar first to prevent file lock
$tempJar = Join-Path $buildDir "temp-repack.jar"
if (Test-Path $tempJar) { Remove-Item $tempJar -Force }

& jar -c0fm "$tempJar" "META-INF\MANIFEST.MF" *

Set-Location $workspaceRoot
Remove-Item $repackDir -Recurse -Force

if (Test-Path $jarFile) { Remove-Item $jarFile -Force }
Move-Item -Path $tempJar -Destination $jarFile -Force
Copy-Item -Path $jarFile -Destination $backendJar -Force

# Also build a lightweight standalone dc-lab-worker.jar
$workerJar = Join-Path $workspaceRoot "backend\target\dc-lab-worker.jar"
$workerManifest = Join-Path $buildDir "worker-manifest.txt"
"Manifest-Version: 1.0`r`nMain-Class: com.dclab.worker.WorkerNode`r`n" | Set-Content -Path $workerManifest -Encoding ASCII
Set-Location $classesDir
& jar -cfm "$workerJar" "$workerManifest" com
Set-Location $workspaceRoot
if (Test-Path $workerManifest) { Remove-Item $workerManifest -Force }

Write-Host "SUCCESS: Packages generated at:" -ForegroundColor Green
Write-Host " -> $jarFile (Master & Worker Fat JAR)" -ForegroundColor Green
Write-Host " -> $backendJar (Fat JAR alias)" -ForegroundColor Green
Write-Host " -> $workerJar (Standalone Worker JAR)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
