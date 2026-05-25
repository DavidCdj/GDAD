$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$paths = @(
    (Join-Path $root 'build'),
    (Join-Path $root 'dist'),
    (Join-Path $root 'out'),
    (Join-Path $root 'Proyecto.jar'),
    (Join-Path $root 'Equipo5.exe'),
    (Join-Path $root 'installer\output')
)

foreach ($path in $paths) {
    if (Test-Path $path) {
        Remove-Item -LiteralPath $path -Recurse -Force
    }
}

Get-ChildItem -Path (Join-Path $root 'src\java') -Filter '*.class' -Force -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
Get-ChildItem -Path (Join-Path $root 'src\python') -Filter '*.pyc' -Recurse -Force -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
Get-ChildItem -Path (Join-Path $root 'src\python') -Directory -Filter '__pycache__' -Recurse -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
