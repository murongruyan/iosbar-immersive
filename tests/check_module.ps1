$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot

foreach ($file in @(
  'module.prop',
  'customize.sh',
  'uninstall.sh',
  'runtime/iosbar-navhook.apk',
  'src/hook/resources/META-INF/xposed/scope.list',
  'META-INF/com/google/android/update-binary',
  'META-INF/com/google/android/updater-script'
)) {
  if (-not (Test-Path -LiteralPath (Join-Path $root $file))) {
    throw "missing required file: $file"
  }
}

foreach ($forbidden in @(
  'system',
  'overlay-src',
  'scripts/metamodule.sh',
  'post-fs-data.sh',
  'post-mount.sh',
  'late-load.sh',
  'service.sh'
)) {
  if (Test-Path -LiteralPath (Join-Path $root $forbidden)) {
    throw "forbidden overlay or mount payload remains: $forbidden"
  }
}

$scope = @(Get-Content (Join-Path $root 'src/hook/resources/META-INF/xposed/scope.list') |
  Where-Object { $_.Trim() })
if ($scope.Count -ne 1 -or $scope[0].Trim() -ne 'com.android.systemui') {
  throw 'hook scope must contain only com.android.systemui'
}

$source = Get-Content (Join-Path $root 'src/hook/java/com/iosbar/navhook/IosBarHook.java') -Raw
foreach ($needle in @(
  'NavigationBarTransitions',
  'getBarBackground',
  'mSemiTransparent',
  'getBarLayoutParamsForRotation',
  'mPortraitWidth',
  'mLandscapeWidth'
)) {
  if ($source -notmatch [regex]::Escape($needle)) {
    throw "missing hook behavior: $needle"
  }
}

$customize = Get-Content (Join-Path $root 'customize.sh') -Raw
if ($customize -match 'mount --bind|metamodule|PUIThemed|OplusGestureWidth|NavigationBarModeGestural') {
  throw 'installer must not mount or replace overlay APKs'
}

foreach ($legacyPackage in @(
  'com.android.internal.systemui.navbar.gestural',
  'com.iosbar.oplus.width',
  'com.iosbar.systemui.dimen'
)) {
  if ($customize -notmatch [regex]::Escape($legacyPackage)) {
    throw "installer does not clean legacy package: $legacyPackage"
  }
}
if ($customize -notmatch '/data/\*' -or $customize -notmatch 'pm path') {
  throw 'legacy package cleanup must be limited to data-backed packages'
}

if ($customize -notmatch 'legacy' -or $customize -notmatch 'disable') {
  throw 'installer must clean legacy payloads and stale disable marker'
}

Write-Output 'hook-only module static checks passed'
