param(
  [string]$OutputDirectory = (Join-Path $PSScriptRoot 'dist')
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

& (Join-Path $root 'build_hook.ps1')
& (Join-Path $root 'tests/check_module.ps1')

$prop = Get-Content (Join-Path $root 'module.prop') -Raw
$version = [regex]::Match($prop, '(?m)^version=(.+)$').Groups[1].Value.Trim()
if (-not $version) { throw 'module version is missing' }

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$zip = Join-Path $OutputDirectory "iosbar-immersive-v$version.zip"
if (Test-Path -LiteralPath $zip) { Remove-Item -LiteralPath $zip -Force }

$stage = Join-Path ([System.IO.Path]::GetTempPath()) ("iosbar-package-" + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path (Join-Path $stage 'runtime') | Out-Null
foreach ($file in @('module.prop', 'customize.sh', 'uninstall.sh')) {
  Copy-Item (Join-Path $root $file) (Join-Path $stage $file)
}
Copy-Item (Join-Path $root 'META-INF') (Join-Path $stage 'META-INF') -Recurse -Force
Copy-Item (Join-Path $root 'runtime/iosbar-navhook.apk') (Join-Path $stage 'runtime/iosbar-navhook.apk')

try {
  Compress-Archive -Path (Join-Path $stage '*') -DestinationPath $zip -CompressionLevel Optimal
} finally {
  Remove-Item -LiteralPath $stage -Recurse -Force
}

Write-Output "module written to $zip"
