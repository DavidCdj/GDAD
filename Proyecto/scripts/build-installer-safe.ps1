param(
    [string]$OutputBaseFilename = 'GimnasiaBimanual-Setup-fixed',
    [switch]$AddTimestamp
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$installerDir = Join-Path $root 'installer'
$issPath = Join-Path $installerDir 'GimnasiaBimanual.iss'
$legacyOutputDir = Join-Path $installerDir 'GimnasiaRelease'

if (-not (Test-Path $issPath)) {
    throw "No se encontró el script de Inno Setup: $issPath"
}

$isccCandidates = @(
    'C:\Program Files (x86)\Inno Setup 6\ISCC.exe',
    'C:\Users\david\AppData\Local\Programs\Inno Setup 6\ISCC.exe'
)

$isccPath = $isccCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $isccPath) {
    throw 'No se encontró ISCC.exe. Instala Inno Setup 6.'
}

# Evita compilar en paralelo: cierra compiladores previos que puedan dejar el setup bloqueado.
Get-Process -Name 'ISCC' -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

if ($AddTimestamp) {
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $OutputBaseFilename = "$OutputBaseFilename-$stamp"
}

if (Test-Path $legacyOutputDir) {
    Remove-Item -LiteralPath $legacyOutputDir -Recurse -Force -ErrorAction SilentlyContinue
}

Push-Location $installerDir
try {
    & $isccPath '/Qp' "/F$OutputBaseFilename" $issPath
    if ($LASTEXITCODE -ne 0) {
        throw "ISCC devolvió código de salida $LASTEXITCODE"
    }
} finally {
    Pop-Location
}

$setupPath = Join-Path $installerDir "output\$OutputBaseFilename.exe"
if (-not (Test-Path $setupPath)) {
    throw "No se generó el instalador esperado: $setupPath"
}

$hash = $null
for ($i = 0; $i -lt 20; $i++) {
    try {
        $hash = Get-FileHash -Algorithm SHA256 -Path $setupPath
        break
    } catch {
        Start-Sleep -Milliseconds 500
    }
}

if (-not $hash) {
    throw "No se pudo leer el instalador generado (archivo bloqueado): $setupPath"
}

$file = Get-Item -LiteralPath $setupPath
Write-Host "Instalador generado: $($file.FullName)"
Write-Host "Tamaño (bytes): $($file.Length)"
Write-Host "SHA256: $($hash.Hash)"