$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$javaJar = Join-Path $root 'Proyecto.jar'
$flatlafJar = Join-Path $root 'src\java\flatlaf-3.5.1.jar'
$poiDir = Join-Path $root 'lib\poi'
$stageDir = Join-Path $root 'build\jpackage-input'
$outputDir = Join-Path $root 'build\jpackage'
$appName = 'Equipo5'

if (-not (Test-Path $javaJar)) {
    throw 'No se encontró Proyecto.jar. Ejecuta primero scripts\build-java.ps1.'
}

if (Test-Path $stageDir) {
    Remove-Item -LiteralPath $stageDir -Recurse -Force
}
if (Test-Path $outputDir) {
    Remove-Item -LiteralPath $outputDir -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $stageDir | Out-Null
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

Copy-Item -LiteralPath $javaJar -Destination (Join-Path $stageDir 'Proyecto.jar')
if (Test-Path $flatlafJar) {
    Copy-Item -LiteralPath $flatlafJar -Destination (Join-Path $stageDir (Split-Path $flatlafJar -Leaf))
}
if (Test-Path $poiDir) {
    Copy-Item -Path (Join-Path $poiDir '*') -Destination $stageDir -Force
}
Copy-Item -Path (Join-Path $root 'recursos') -Destination $stageDir -Recurse -Force
Copy-Item -Path (Join-Path $root 'dist') -Destination $stageDir -Recurse -Force

& jpackage `
    --type app-image `
    --name $appName `
    --app-version 1.0 `
    --vendor 'Equipo 5' `
    --input $stageDir `
    --dest $outputDir `
    --main-jar Proyecto.jar `
    --main-class ProyectoE5

Write-Host "App-image generado en: $outputDir\$appName"
