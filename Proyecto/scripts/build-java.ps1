$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $root 'out'
$srcDir = Join-Path $root 'src\java'
$flatlafJar = Join-Path $srcDir 'flatlaf-3.5.1.jar'
$poiDir = Join-Path $root 'lib\poi'
$jarPath = Join-Path $root 'Proyecto.jar'
$exePath = Join-Path $root 'Equipo5.exe'

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$classpathParts = @($srcDir, $outDir)
if (Test-Path $flatlafJar) { $classpathParts += $flatlafJar }
if (Test-Path $poiDir) { $classpathParts += (Join-Path $poiDir '*') }
$classpath = ($classpathParts -join ';')

$javaFiles = Get-ChildItem -Path $srcDir -Filter '*.java' -File | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -d $outDir -cp $classpath @javaFiles

if (Test-Path $jarPath) { Remove-Item $jarPath -Force }
Push-Location $outDir
try {
    & jar --create --file $jarPath --main-class ProyectoE5 -C $outDir .
} finally {
    Pop-Location
}

if (Test-Path $exePath) { Remove-Item $exePath -Force }
Write-Host 'Proyecto.jar generado. Si quieres recrear Equipo5.exe, usa Launch4j con el jar generado.'
