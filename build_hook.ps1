$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$source = Join-Path $root 'src\hook'
$temp = Join-Path ([System.IO.Path]::GetTempPath()) ("iosbar-hook-build-" + [guid]::NewGuid().ToString('N'))

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
    Where-Object { $_.FullName -match 'gradle-9\.5(?:\.0)?' } |
    Select-Object -First 1
  if ($knownGradle) { $gradle = $knownGradle.FullName }
}
if (-not $gradle) { throw 'Gradle 9.5.1 is required. Set GRADLE_BIN or put gradle on PATH.' }

$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } elseif ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { $null }
if (-not $sdk) {
  $localProperties = Join-Path $source 'local.properties'
  if (Test-Path -LiteralPath $localProperties) {
    $sdkLine = Get-Content $localProperties | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
    if ($sdkLine) { $sdk = ($sdkLine -replace '^sdk\.dir=', '').Replace('\\:', ':').Replace('\\\\', '\\') }
  }
}
if (-not $sdk -or -not (Test-Path -LiteralPath $sdk)) {
  throw 'Android SDK 36 is required. Set ANDROID_HOME or ANDROID_SDK_ROOT.'
}

New-Item -ItemType Directory -Force -Path $temp | Out-Null
if (Test-Path -LiteralPath $temp) { Remove-Item -LiteralPath $temp -Recurse -Force }
New-Item -ItemType Directory -Force -Path $temp | Out-Null
Get-ChildItem -LiteralPath $source -Force | Where-Object { $_.Name -ne 'local.properties' } |
  Copy-Item -Destination $temp -Recurse -Force
"sdk.dir=$($sdk.Replace('\', '\\'))" | Set-Content -LiteralPath (Join-Path $temp 'local.properties') -Encoding ascii
Push-Location $temp
& $gradle assembleDebug
$gradleExit = $LASTEXITCODE
Pop-Location
if ($gradleExit -ne 0) { throw 'API 102 hook build failed' }
$runtime = Join-Path $root 'runtime'
New-Item -ItemType Directory -Force -Path $runtime | Out-Null
Copy-Item (Join-Path $temp 'build\outputs\apk\debug\iosbar-navhook-debug.apk') (Join-Path $runtime 'iosbar-navhook.apk') -Force
Write-Output "hook written to $(Join-Path $runtime 'iosbar-navhook.apk')"
