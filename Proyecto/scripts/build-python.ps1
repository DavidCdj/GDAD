$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$python = Join-Path $root '.venv311\Scripts\python.exe'
$specs = @(
    (Join-Path $root 'DedosArriba.spec'),
    (Join-Path $root 'DedosColores.spec')
)

if (-not (Test-Path $python)) {
    throw 'No se encontró la venv del proyecto en .venv311.'
}

foreach ($spec in $specs) {
    if (-not (Test-Path $spec)) {
        throw "No se encontró el archivo spec: $spec"
    }
    & $python -m PyInstaller --noconfirm --clean $spec
}
