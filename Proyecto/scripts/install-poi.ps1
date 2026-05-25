# Script de instalación de Apache POI (descarga JARs en lib/poi)
# Ejecutar desde la raíz del proyecto en PowerShell:
#   .\scripts\install-poi.ps1

$destDir = "lib/poi"
New-Item -ItemType Directory -Force -Path $destDir | Out-Null

# URLs fijas a descargar (Maven Central) — versiones probadas
$urls = @(
    "https://repo1.maven.org/maven2/org/apache/poi/poi/5.2.3/poi-5.2.3.jar",
    "https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/5.2.3/poi-ooxml-5.2.3.jar",
    "https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml-full/5.2.3/poi-ooxml-full-5.2.3.jar",
    "https://repo1.maven.org/maven2/org/apache/poi/ooxml-schemas/1.4/ooxml-schemas-1.4.jar",
    "https://repo1.maven.org/maven2/org/apache/xmlbeans/xmlbeans/5.1.1/xmlbeans-5.1.1.jar",
    "https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar",
    "https://repo1.maven.org/maven2/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar"
)

$errors = @()
foreach ($url in $urls) {
    $filename = Split-Path $url -Leaf
    $out = Join-Path $destDir $filename
    Write-Host "Descargando: $url -> $out"
    try {
        Invoke-WebRequest -Uri $url -OutFile $out -UseBasicParsing -ErrorAction Stop
    } catch {
        Write-Warning ("Fallo al descargar {0} - {1}" -f $url, $_.Exception.Message)
        $errors += $url
    }
}

if ($errors.Count -gt 0) {
    Write-Warning "Algunas descargas fallaron. Revisa las URLs listadas abajo y ajusta versiones en el script si es necesario."
    $errors | ForEach-Object { Write-Host " - $_" }
} else {
    Write-Host "Descargas completadas. JARs guardados en: $destDir"
}

    Write-Host 'Para compilar y ejecutar con los JARs:'
    Write-Host '  javac -d out -cp "lib/poi/*;src" src\java\*.java'
    Write-Host '  java -cp "out;lib/poi/*" ProyectoE5'
