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
$apk = Join-Path $OutputDirectory "iosbar-navhook-v$version.apk"
Copy-Item (Join-Path $root 'runtime/iosbar-navhook.apk') $apk -Force
Write-Output "LSPosed APK written to $apk"
