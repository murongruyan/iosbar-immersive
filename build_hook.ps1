$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$source = Join-Path $root 'src\hook'
$temp = Join-Path ([System.IO.Path]::GetTempPath()) ("iosbar-hook-build-" + [guid]::NewGuid().ToString('N'))

if (-not $env:JAVA_HOME) {
  $jdk21 = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -Filter 'jdk-21*' -ErrorAction SilentlyContinue |
    Select-Object -First 1
  if ($jdk21) { $env:JAVA_HOME = $jdk21.FullName }
}

$gradle = $null
if ($env:GRADLE_BIN -and (Test-Path -LiteralPath $env:GRADLE_BIN)) {
  $gradle = $env:GRADLE_BIN
} else {
  if ($IsWindows) {
    $gradleCommand = Get-Command gradle.bat -ErrorAction SilentlyContinue
    if (-not $gradleCommand) { $gradleCommand = Get-Command gradle -ErrorAction SilentlyContinue }
  } else {
    $gradleCommand = Get-Command gradle -ErrorAction SilentlyContinue
    if (-not $gradleCommand) { $gradleCommand = Get-Command gradle.bat -ErrorAction SilentlyContinue }
  }
  if ($gradleCommand) { $gradle = $gradleCommand.Source }
}
if (-not $gradle) {
  $knownGradle = Get-ChildItem -Path (Join-Path $env:USERPROFILE '.gradle\wrapper\dists') -Filter gradle.bat -Recurse -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match 'gradle-9\.(?:[6-9]|1[0-9])' } |
    Sort-Object FullName -Descending |
    Select-Object -First 1
  if ($knownGradle) { $gradle = $knownGradle.FullName }
}
if (-not $gradle) { throw 'Gradle 9.6+ is required. Set GRADLE_BIN or put gradle on PATH.' }

$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } elseif ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { $null }
if (-not $sdk) {
  $localProperties = Join-Path $source 'local.properties'
  if (Test-Path -LiteralPath $localProperties) {
    $sdkLine = Get-Content $localProperties | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
    if ($sdkLine) { $sdk = ($sdkLine -replace '^sdk\.dir=', '').Replace('\\:', ':').Replace('\\\\', '\\') }
  }
}
if (-not $sdk -or -not (Test-Path -LiteralPath $sdk)) {
  throw 'Android SDK 37 is required. Set ANDROID_HOME or ANDROID_SDK_ROOT.'
}

# Signing material: CI passes KEYSTORE_BASE64 / STORE_PASSWORD / KEY_PASSWORD through the
# environment; locally the same passwords live in src/hook/local.properties (gitignored) next to
# release.jks. A release build must be signed, so refuse to continue without them.
if (-not $env:STORE_PASSWORD -or -not $env:KEY_PASSWORD) {
  $signingProps = Join-Path $source 'local.properties'
  if (Test-Path -LiteralPath $signingProps) {
    $props = @{}
    foreach ($line in Get-Content -LiteralPath $signingProps) {
      if ($line -match '^\s*([^#=\s]+)\s*=\s*(.*)$') { $props[$Matches[1]] = $Matches[2] }
    }
    if (-not $env:STORE_PASSWORD -and $props['storePassword']) { $env:STORE_PASSWORD = $props['storePassword'] }
    if (-not $env:KEY_PASSWORD -and $props['keyPassword']) { $env:KEY_PASSWORD = $props['keyPassword'] }
  }
}
if (-not $env:STORE_PASSWORD -or -not $env:KEY_PASSWORD) {
  throw 'Release signing needs STORE_PASSWORD / KEY_PASSWORD (environment or src/hook/local.properties).'
}

New-Item -ItemType Directory -Force -Path $temp | Out-Null
if (Test-Path -LiteralPath $temp) { Remove-Item -LiteralPath $temp -Recurse -Force }
New-Item -ItemType Directory -Force -Path $temp | Out-Null
Get-ChildItem -LiteralPath $source -Force | Where-Object { $_.Name -ne 'local.properties' } |
  Copy-Item -Destination $temp -Recurse -Force
"sdk.dir=$($sdk.Replace('\', '\\'))" | Set-Content -LiteralPath (Join-Path $temp 'local.properties') -Encoding ascii
Push-Location $temp
& $gradle assembleRelease
$gradleExit = $LASTEXITCODE
Pop-Location
if ($gradleExit -ne 0) { throw 'API 102 hook build failed' }
$runtime = Join-Path $root 'runtime'
New-Item -ItemType Directory -Force -Path $runtime | Out-Null
Copy-Item (Join-Path $temp 'build\outputs\apk\release\iosbar-navhook-release.apk') (Join-Path $runtime 'iosbar-navhook.apk') -Force
Write-Output "hook written to $(Join-Path $runtime 'iosbar-navhook.apk')"
