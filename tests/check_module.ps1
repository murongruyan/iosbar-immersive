$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot

foreach ($file in @(
  'module.prop',
  'customize.sh',
  'uninstall.sh',
  'runtime/iosbar-navhook.apk',
  'src/hook/resources/META-INF/xposed/scope.list',
  'src/hook/resources/META-INF/xposed/module.prop',
  'src/hook/AndroidManifest.xml',
  'src/hook/build.gradle.kts',
  'src/hook/java/com/iosbar/navhook/IosBarHook.java',
  'src/hook/java/com/iosbar/navhook/SettingsStore.java',
  'src/hook/java/com/iosbar/navhook/BarConfig.java',
  'src/hook/kotlin/com/iosbar/navhook/ui/SettingsActivity.kt',
  'src/hook/kotlin/com/iosbar/navhook/ui/SettingsState.kt',
  'src/hook/kotlin/com/iosbar/navhook/ui/Design.kt',
  'src/hook/kotlin/com/iosbar/navhook/ui/Background.kt',
  'src/hook/kotlin/com/iosbar/navhook/ui/Pages.kt',
  'src/hook/res/mipmap-anydpi-v26/ic_launcher.xml',
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

$moduleProp = Get-Content -Raw (Join-Path $root 'module.prop')
if ($moduleProp -notmatch '(?m)^version=0\.6\.0$' -or $moduleProp -notmatch '(?m)^versionCode=10$') {
  throw 'module.prop must describe v0.6.0 (versionCode 10)'
}

$hook = Get-Content -Raw (Join-Path $root 'src/hook/java/com/iosbar/navhook/IosBarHook.java')
foreach ($needle in @(
  'NavigationBarTransitions',
  'getBarBackground',
  'mSemiTransparent',
  'getBarLayoutParamsForRotation',
  'OplusNavigationHandle',
  'getGestureWidthRes',
  'onDraw',
  'mAdditionalHeightForAnimation',
  'longPressAnimProgress',
  'getRemotePreferences',
  'SettingsStore.PREFS_NAME',
  'drawRoundRect',
  'Configuration.ORIENTATION_LANDSCAPE',
  'handleWidth'
)) {
  if ($hook -notmatch [regex]::Escape($needle)) {
    throw "missing ColorOS 17 hook behavior: $needle"
  }
}
foreach ($forbiddenHook in @('applyHandleGeometry', 'pollCount', 'config poll', 'setIntField(receiver, "mHeight"', 'setIntField(receiver, "mHandleBottom"', 'setIntField(receiver, "mRadius"')) {
  if ($hook -match [regex]::Escape($forbiddenHook)) {
    throw "final-field mutation remains in hook: $forbiddenHook"
  }
}

$build = Get-Content -Raw (Join-Path $root 'src/hook/build.gradle.kts')
foreach ($needle in @(
  'compileSdk = 37',
  'targetSdk = 37',
  'id("com.android.application") version "9.4.1"',
  'org.jetbrains.kotlin.plugin.compose',
  'org.jetbrains.compose',
  'top.yukonga.miuix.kmp:miuix-ui:0.9.4',
  'top.yukonga.miuix.kmp:miuix-preference:0.9.4',
  'io.github.kyant0:backdrop:2.0.1',
  'io.github.libxposed:service:102.0.0'
)) {
  if ($build -notmatch [regex]::Escape($needle)) {
    throw "build.gradle.kts missing: $needle"
  }
}

$manifest = Get-Content -Raw (Join-Path $root 'src/hook/AndroidManifest.xml')
foreach ($needle in @('.ui.SettingsActivity', 'android.intent.category.LAUNCHER', '@style/Theme.IosBar', 'xposedsharedprefs', 'android:enableOnBackInvokedCallback="true"')) {
  if ($manifest -notmatch [regex]::Escape($needle)) {
    throw "AndroidManifest.xml missing: $needle"
  }
}

$ui = Get-Content -Raw (Join-Path $root 'src/hook/kotlin/com/iosbar/navhook/ui/SettingsActivity.kt')
foreach ($needle in @(
  'MiuixTheme',
  'rememberLayerBackdrop',
  'layerBackdrop',
  'drawBackdrop',
  'vibrancy',
  'DampedDragAnimation',
  'InteractiveHighlight',
  'LargeTitle',
  'BottomNavBar',
  'PredictiveBackHandler',
  'HorizontalPager',
  'BottomNavBar',
  'NavBarItem',
  'TabPage',
  'backAnim',
  'animateDpAsState',
  'IconButton',
  'XposedServiceHelper',
  'PickVisualMedia',
  'persistBackground',
  'restartScope',
  'WindowDialog'
)) {
  if ($ui -notmatch [regex]::Escape($needle)) {
    throw "settings shell missing: $needle"
  }
}

$pages = Get-Content -Raw (Join-Path $root 'src/hook/kotlin/com/iosbar/navhook/ui/Pages.kt')
foreach ($needle in @(
  'SettingsCard',
  'SwitchPreference',
  'SliderPreference',
  'ArrowPreference',
  'TabRow',
  'SmallTitle',
  'ColorPicker',
  'StatusCard',
  'BarPreview',
  '关于',
  '版本信息',
  '实时预览',
  '保存当前参数',
  '备份当前参数',
  '恢复备份',
  '工作中'
)) {
  if ($pages -notmatch [regex]::Escape($needle)) {
    throw "settings pages missing: $needle"
  }
}

$design = Get-Content -Raw (Join-Path $root 'src/hook/kotlin/com/iosbar/navhook/ui/Design.kt')
foreach ($needle in @('accentScheme', 'CardCorner', 'SettingIcon', 'SettingsCard', 'surfaceContainer')) {
  if ($design -notmatch [regex]::Escape($needle)) {
    throw "design system missing: $needle"
  }
}

$background = Get-Content -Raw (Join-Path $root 'src/hook/kotlin/com/iosbar/navhook/ui/Background.kt')
foreach ($needle in @('PageBackground', 'BarPreview', 'resolvedBarColor')) {
  if ($background -notmatch [regex]::Escape($needle)) {
    throw "background layer missing: $needle"
  }
}

$state = Get-Content -Raw (Join-Path $root 'src/hook/kotlin/com/iosbar/navhook/ui/SettingsState.kt')
foreach ($needle in @('backupPreferences', 'KEY_WIDTH_PORTRAIT', 'KEY_WIDTH_LANDSCAPE', 'KEY_HEIGHT', 'KEY_BOTTOM', 'KEY_RADIUS', 'KEY_ALPHA', 'KEY_COLOR_MODE', 'KEY_CUSTOM_COLOR', 'KEY_BACKGROUND_MODE', 'KEY_BACKGROUND_PATH', 'KEY_BACKGROUND_BLUR', 'KEY_ACCENT', 'KEY_GLASS_BLUR', 'KEY_GLASS_REFRACTION', 'KEY_GLASS_HIGHLIGHT', 'KEY_GLASS_TINT', 'KEY_GLASS_NAVBAR', 'fun backup', 'fun restoreBackup')) {
  if ($state -notmatch [regex]::Escape($needle)) {
    throw "settings state missing: $needle"
  }
}

$customize = Get-Content -Raw (Join-Path $root 'customize.sh')
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

Add-Type -AssemblyName System.IO.Compression.FileSystem
$apkPath = Join-Path $root 'runtime/iosbar-navhook.apk'
$archive = [System.IO.Compression.ZipFile]::OpenRead($apkPath)
try {
  $entries = @($archive.Entries | ForEach-Object { $_.FullName })
  foreach ($entry in @(
    'META-INF/xposed/java_init.list',
    'META-INF/xposed/module.prop',
    'META-INF/xposed/scope.list'
  )) {
    if ($entries -notcontains $entry) {
      throw "APK missing LSPosed metadata: $entry"
    }
  }
  $dexEntries = @($archive.Entries | Where-Object { $_.FullName -match '^classes\d*\.dex$' })
  if ($dexEntries.Count -eq 0) {
    throw 'APK contains no classes.dex'
  }
  $foundUi = $false
  foreach ($entry in $dexEntries) {
    $stream = $entry.Open()
    try {
      $memory = New-Object System.IO.MemoryStream
      $stream.CopyTo($memory)
      $text = [System.Text.Encoding]::UTF8.GetString($memory.ToArray())
      if ($text.Contains('com/iosbar/navhook/ui/SettingsActivity')) {
        $foundUi = $true
      }
    } finally {
      $stream.Dispose()
    }
  }
  if (-not $foundUi) {
    throw 'APK does not contain SettingsActivity'
  }
} finally {
  $archive.Dispose()
}

Write-Output 'hook-only v0.6.0 static checks passed'



