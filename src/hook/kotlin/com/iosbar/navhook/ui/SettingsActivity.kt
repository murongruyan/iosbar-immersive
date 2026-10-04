package com.iosbar.navhook.ui

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults.flingBehavior
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.kyant.backdrop.isRenderEffectSupported
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.iosbar.navhook.SettingsStore
import com.iosbar.navhook.ui.liquid.DampedDragAnimation
import com.iosbar.navhook.ui.liquid.InteractiveHighlight
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.springAnimateToPage
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

// Motion values lifted from 慕容调度 (MurongComposeActivity.kt).
private const val SHELL_NAV_STIFFNESS = 380f
private const val SHELL_NAV_DAMPING = 0.72f
private const val SECONDARY_NAV_STIFFNESS = 300f
private const val SECONDARY_NAV_DAMPING = 0.78f
// Liquid droplet values for the pressed navigation item.
private const val DROPLET_PRESSED = 0.62f
private const val DROPLET_LONG_PRESSED = 1.30f
private const val ITEM_SQUASH = 0.94f

// Bottom bar metrics. Height restored to the comfortable first pass; the bar is narrowed
// instead by giving every item a fixed width and centring the pill rather than spanning the screen.
private val BarCorner = 32.dp
private val BarBottomMargin = 16.dp
private val BarPad = 7.dp
private val BarIconRow = 30.dp
private val BarIconSize = 24.dp
private val BarItemCorner = 22.dp
private val BarIndicatorWidth = 54.dp
private val BarIndicatorHeight = 30.dp

/**
 * Murong shortens the finish animation by how far the gesture already travelled, so a nearly
 * complete swipe snaps shut immediately instead of always taking the full duration.
 */
internal fun predictiveBackCommitDurationMillis(
    progress: Float,
    fullDurationMillis: Int = 180,
    minDurationMillis: Int = 72,
): Int {
    val clamped = progress.coerceIn(0f, 1f)
    if (clamped >= 1f) return 0
    val scaled = (fullDurationMillis * (1f - clamped)).toInt()
    return scaled.coerceIn(minDurationMillis, fullDurationMillis)
}

/** Swallows every pointer event so a preview route behind the live one never steals taps. */
private val BlockInput: Modifier = Modifier.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent().changes.forEach { it.consume() }
        }
    }
}

internal enum class MainTab(val label: String, val title: String, val subtitle: String) {
    HOME("主页", "iOS 沉浸式小横条", "ColorOS 17 · LSPosed"),
    TUNE("调节", "横条调节", "尺寸、圆角与颜色"),
    SETTINGS("设置", "设置", "外观、备份与关于"),
}

internal enum class SubPage(val title: String, val subtitle: String) {
    NONE("", ""),
    APPEARANCE("外观与玻璃", "强调色、卡片与底栏玻璃、文字颜色、背景"),
    PARAMS("参数调节", "编辑当前预设的尺寸与颜色"),
    BACKUP("保存与备份", "参数备份与恢复"),
    ABOUT("关于", "版本信息与运行环境"),
}

class SettingsActivity : ComponentActivity() {
    private val preferencesState = mutableStateOf<SharedPreferences?>(null)
    private val backupState = mutableStateOf<SharedPreferences?>(null)

    private val serviceListener = object : XposedServiceHelper.OnServiceListener {
        override fun onServiceBind(service: XposedService) {
            val preferences = service.getRemotePreferences(SettingsStore.PREFS_NAME)
            val backup = service.getRemotePreferences(SettingsStore.BACKUP_PREFS_NAME)
            runOnUiThread {
                preferencesState.value = preferences
                backupState.value = backup
            }
        }

        override fun onServiceDied(service: XposedService) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        XposedServiceHelper.registerListener(serviceListener)
        Handler(Looper.getMainLooper()).postDelayed({
            if (preferencesState.value == null) {
                preferencesState.value = SettingsStore.get(this)
                backupState.value = getSharedPreferences(SettingsStore.BACKUP_PREFS_NAME, MODE_PRIVATE)
            }
        }, 3000L)
        setContent {
            val preferences = preferencesState.value
            val backup = backupState.value
            if (preferences == null || backup == null) {
                LoadingScreen()
            } else {
                IosBarSettingsApp(
                    preferences = preferences,
                    backupPreferences = backup,
                    onRestartScope = { restartScope() },
                )
            }
        }
    }

    private fun restartScope() {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "killall com.android.systemui"))
            Toast.makeText(this, "正在重启 SystemUI 作用域…", Toast.LENGTH_SHORT).show()
        } catch (error: Throwable) {
            Toast.makeText(this, "重启失败: " + error.message, Toast.LENGTH_LONG).show()
        }
    }

    private companion object {
        private const val MODE_PRIVATE = Context.MODE_PRIVATE
    }
}

@Composable
private fun LoadingScreen() {
    MiuixTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Text("正在连接 LSPosed 服务…", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable
private fun IosBarSettingsApp(
    preferences: SharedPreferences,
    backupPreferences: SharedPreferences,
    onRestartScope: () -> Unit,
) {
    val state = rememberSettingsState(preferences, backupPreferences)
    val context = LocalContext.current
    val activity = context as? Activity
    val dark = isSystemInDarkTheme()
    val accent = accentOption(state.accent)
    val accentColor = if (dark) accent.dark else accent.light
    // Records only the page background, so the settings cards can blur the wallpaper without
    // sampling themselves. Kept apart from the page backdrop, which records the live pages for
    // the floating bar.
    val wallpaperBackdrop = rememberLayerBackdrop()
    // 自动 cards follow the wallpaper's own brightness (see resolveCardVeil). Reading a 32 px
    // thumbnail off the main thread costs one small decode per picked file - not per frame - and is
    // what lets the veil be chosen from the photo instead of a hard-coded white film.
    val wallpaperLuma by produceState<Float?>(initialValue = null, state.backgroundPath, state.backgroundMode) {
        value = if (state.backgroundMode == "custom" && state.backgroundPath.isNotBlank()) {
            withContext(Dispatchers.IO) {
                runCatching { wallpaperAverageLuma(state.backgroundPath) }.getOrNull()
            }
        } else {
            null
        }
    }
    // Blurring the shared wallpaper layer once is what keeps the cards cheap: filtering each card's
    // own backdrop cost a separate pass per visible panel and was the source of the scroll jank.
    val cardBlurPx = with(LocalDensity.current) { state.cardGlassBlur.dp.toPx() }
    SideEffect {
        val layer = wallpaperBackdrop.graphicsLayer
        layer.renderEffect = if (state.glassCards && cardBlurPx > 0f && isRenderEffectSupported()) {
            BlurEffect(radiusX = cardBlurPx, radiusY = cardBlurPx, edgeTreatment = TileMode.Clamp)
        } else {
            null
        }
    }
    val ink = resolveTextInk(
        mode = state.textColorMode,
        custom = Color(state.textColorCustom),
        dark = dark,
    )
    val colors = remember(state.accent, dark, ink) {
        accentScheme(accent, dark, if (state.textColorMode == TEXT_COLOR_AUTO) null else ink)
    }
    val glass = remember(state.glassBlur, state.glassRefraction, state.glassHighlight, state.glassTint) {
        GlassSpec(
            blur = state.glassBlur.dp,
            refraction = state.glassRefraction.dp,
            highlight = state.glassHighlight / 100f,
            tint = state.glassTint / 100f,
        )
    }
    val backdrop = rememberLayerBackdrop()
    val scope = rememberCoroutineScope()
    val backAnim = remember { Animatable(0f) }
    // 0 = settled on a tab route, 1 = settled on the sub page. Drives the push / pop transition.
    val enterAnim = remember { Animatable(0f) }

    var subPage by remember { mutableStateOf(SubPage.NONE) }
    var showRestartDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = MainTab.HOME.ordinal) { MainTab.entries.size }
    val homeListState = rememberLazyListState()
    val tuneListState = rememberLazyListState()
    val settingsListState = rememberLazyListState()
    val subListState = rememberLazyListState()
    val previewListState = rememberLazyListState()

    // settledPage, not currentPage: the route identity must not flip halfway through a swipe,
    // otherwise the predictive back preview composes and disposes mid gesture.
    val tab = MainTab.entries[pagerState.settledPage.coerceIn(0, MainTab.entries.size - 1)]
    val canGoBack = subPage != SubPage.NONE || tab != MainTab.HOME
    val previousTab = if (subPage != SubPage.NONE) tab else MainTab.HOME

    fun listStateFor(target: MainTab): LazyListState = when (target) {
        MainTab.HOME -> homeListState
        MainTab.TUNE -> tuneListState
        MainTab.SETTINGS -> settingsListState
    }

    LaunchedEffect(subPage) {
        if (subPage != SubPage.NONE) {
            subListState.scrollToItem(0)
            enterAnim.snapTo(0f)
            enterAnim.animateTo(1f, tween(durationMillis = 260, easing = FastOutSlowInEasing))
        } else {
            enterAnim.snapTo(0f)
        }
    }

    // Forward navigation always animates in; the back arrow animates out, matching the gesture.
    val openSubPage: (SubPage) -> Unit = { target ->
        scope.launch { backAnim.snapTo(0f) }
        subPage = target
    }
    val closeSubPage: () -> Unit = {
        scope.launch {
            backAnim.animateTo(1f, tween(durationMillis = 220, easing = LinearEasing))
            subPage = SubPage.NONE
            backAnim.snapTo(0f)
        }
    }

    PredictiveBackHandler(enabled = canGoBack) { progress ->
        val poppingSubPage = subPage != SubPage.NONE
        try {
            progress.collect { event -> backAnim.snapTo(event.progress) }
        } catch (cancellation: CancellationException) {
            scope.launch {
                backAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        stiffness = if (poppingSubPage) SECONDARY_NAV_STIFFNESS else SHELL_NAV_STIFFNESS,
                        dampingRatio = if (poppingSubPage) SECONDARY_NAV_DAMPING else SHELL_NAV_DAMPING,
                    ),
                )
            }
            throw cancellation
        }
        backAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = predictiveBackCommitDurationMillis(backAnim.value),
                easing = LinearEasing,
            ),
        )
        if (poppingSubPage) {
            subPage = SubPage.NONE
        } else {
            scope.launch { pagerState.scrollToPage(MainTab.HOME.ordinal) }
        }
        backAnim.snapTo(0f)
    }

    var hasImageAccess by remember { mutableStateOf(PermissionManager.hasImageAccess(context)) }
    var pendingBackgroundPick by remember { mutableStateOf(false) }
    // Set when access was granted just in time: the picker has to be launched from a composition
    // driven effect, not from inside the permission callback.
    var launchImagePicker by remember { mutableStateOf(false) }

    // Android 11+ has no runtime storage permission that can read an arbitrary file, so we follow
    // 慕容调度: send the user to 所有文件访问, and only fall back to a runtime permission below 11.
    val allFilesAccessLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hasImageAccess = PermissionManager.hasImageAccess(context)
        if (hasImageAccess) {
            Toast.makeText(context, "已获得文件访问权限", Toast.LENGTH_SHORT).show()
            if (pendingBackgroundPick) {
                pendingBackgroundPick = false
                launchImagePicker = true
            }
        } else {
            pendingBackgroundPick = false
            Toast.makeText(context, "未授予「所有文件访问」，无法读取自定义图片", Toast.LENGTH_LONG).show()
        }
    }

    val imagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasImageAccess = PermissionManager.hasImageAccess(context)
        if (granted && pendingBackgroundPick) {
            pendingBackgroundPick = false
            launchImagePicker = true
        } else {
            pendingBackgroundPick = false
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val path = persistBackground(context, uri)
            if (path != null) {
                state.backgroundPath = path
                state.backgroundMode = "custom"
                state.commit()
            } else {
                Toast.makeText(context, "所选图片读取失败，请重新选择", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(launchImagePicker) {
        if (launchImagePicker) {
            launchImagePicker = false
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    val requestImageAccess: () -> Unit = {
        val runtimePermission = PermissionManager.runtimePermission()
        if (runtimePermission != null) {
            imagePermissionLauncher.launch(runtimePermission)
        } else {
            val currentActivity = activity
            if (currentActivity == null ||
                !PermissionManager.openAllFilesAccess(currentActivity) { allFilesAccessLauncher.launch(it) }
            ) {
                pendingBackgroundPick = false
                Toast.makeText(context, "无法打开授权页面，请在系统设置中手动授予", Toast.LENGTH_LONG).show()
            }
        }
    }

    val pickBackground: () -> Unit = {
        if (PermissionManager.hasImageAccess(context)) {
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            pendingBackgroundPick = true
            requestImageAccess()
        }
    }

    // Picking "自定义图片" without actually choosing a file still renders the flat page colour
    // (see PageBackground), so the dark scrim and the light status bar icons must follow the image
    // really being usable - not just the mode - or the status bar turns into a black band on a
    // white page. The mode plus a non-blank path is not enough on its own: a picked file that has
    // since been deleted decodes to nothing and paints the same flat page.
    val wallpaperFileExists = remember(state.backgroundPath) {
        state.backgroundPath.isNotBlank() && java.io.File(state.backgroundPath).exists()
    }
    val hasWallpaper = state.backgroundMode == "custom" && wallpaperFileExists
    // The top scrim darkens over a wallpaper, so the system's dark status bar icons would sit on a
    // dark band and disappear. Light icons are also what the dark theme already asks for.
    val view = LocalView.current
    val lightStatusIcons = !dark && !hasWallpaper
    SideEffect {
        val host = view.context as? Activity ?: return@SideEffect
        WindowCompat.getInsetsController(host.window, view).isAppearanceLightStatusBars = lightStatusIcons
    }

    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentPadding = PaddingValues(
        start = ScreenPadding,
        end = ScreenPadding,
        top = statusBarInset + 12.dp,
        bottom = navBarInset + BarBottomMargin + 54.dp,
    )
    val onOpenAppearance = { openSubPage(SubPage.APPEARANCE) }
    val onOpenBackup = { openSubPage(SubPage.BACKUP) }
    val onOpenAbout = { openSubPage(SubPage.ABOUT) }
    val onOpenPresets = { openSubPage(SubPage.PARAMS) }

    CompositionLocalProvider(
        LocalSettingsState provides state,
        LocalCardBackdrop provides wallpaperBackdrop,
        LocalWallpaperLuma provides wallpaperLuma,
    ) {
    MiuixTheme(colors = colors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background),
        ) {
            // Tab level predictive back preview: only when there is no sub page on top.
            if (canGoBack && subPage == SubPage.NONE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = backAnim.value * size.width - size.width
                        }
                        .then(BlockInput),
                ) {
                    TabPage(
                        tab = previousTab,
                        state = state,
                        dark = dark,
                        accentColor = accentColor,
                        listState = previewListState,
                        contentPadding = contentPadding,
                        interactive = false,
                        onOpenAppearance = onOpenAppearance,
                        onOpenBackup = onOpenBackup,
                        onOpenAbout = onOpenAbout,
                        onOpenPresets = onOpenPresets,
                    )
                }
            }

            // Tab routes. Always composed so a sub page pushes over the real thing, and so the
            // predictive back gesture has something to reveal instead of a bare background.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Zero while a sub page is open. A layer recorded inside a translated
                        // parent reports the position it was dragged to, so the wallpaper
                        // backdrop came out shifted by exactly this offset and every card
                        // sampling it drew the wallpaper sideways, leaving a bright band down
                        // the right edge of the screen. The sub page sliding in is the whole
                        // transition; the shell behind it must stay put.
                        translationX = if (subPage == SubPage.NONE) {
                            backAnim.value * size.width
                        } else {
                            0f
                        }
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(backdrop),
                ) {
                    // A second, nested recorder that captures the wallpaper alone. The cards sample
                    // this one, so their blur never feeds back into the page they sit on.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(wallpaperBackdrop),
                    ) {
                        PageBackground(state = state, dark = dark)
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        // KernelSU pre-composes every page so a swipe never triggers composition,
                        // and drops the overscroll glow which is pure cost on a glass surface.
                        beyondViewportPageCount = 2,
                        overscrollEffect = null,
                        flingBehavior = flingBehavior(
                            state = pagerState,
                            snapAnimationSpec = PagerNavigationSpringSpec,
                        ),
                    ) { page ->
                        val pageTab = MainTab.entries[page]
                        // No per page transform: scaling a full screen layer every frame was
                        // what made the horizontal page swipe stutter.
                        TabPage(
                            tab = pageTab,
                            state = state,
                            dark = dark,
                            accentColor = accentColor,
                            listState = listStateFor(pageTab),
                            contentPadding = contentPadding,
                            interactive = subPage == SubPage.NONE,
                            onOpenAppearance = onOpenAppearance,
                            onOpenBackup = onOpenBackup,
                            onOpenAbout = onOpenAbout,
                            onOpenPresets = onOpenPresets,
                        )
                    }
                }

                TopScrim(statusBarInset, wallpaper = hasWallpaper)

                if (subPage == SubPage.NONE && tab == MainTab.TUNE) {
                    ChromeIconButton(
                        icon = MiuixIcons.Edit,
                        description = "编辑当前预设",
                        tint = accentColor,
                        endPadding = 68.dp,
                        onClick = onOpenPresets,
                    )
                }

                ChromeIconButton(
                    icon = MiuixIcons.Refresh,
                    description = "重启作用域",
                    tint = accentColor,
                    endPadding = 16.dp,
                    onClick = { showRestartDialog = true },
                )

                if (subPage == SubPage.NONE) {
                    BottomNavBar(
                        selectedIndex = tab.ordinal,
                        onSelect = { index ->
                            val target = MainTab.entries[index]
                            if (target == tab) {
                                scope.launch { listStateFor(target).animateScrollToItem(0) }
                            } else {
                                scope.launch { pagerState.springAnimateToPage(index) }
                            }
                        },
                        accentColor = accentColor,
                        glass = glass,
                        glassEnabled = state.glassNavbar,
                        glassStyle = if (hasWallpaper) BarGlassStyle.WALLPAPER else BarGlassStyle.KERNEL_SU,
                        backdrop = backdrop,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = BarBottomMargin),
                    )
                }
            }

            // Sub page layer: slides in from the right on open, follows the finger on predictive
            // back, and animates out when the back arrow is used.
            if (subPage != SubPage.NONE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = ((1f - enterAnim.value) + backAnim.value) * size.width
                        },
                ) {
                    // Was a flat surface colour, which hid the picked wallpaper on every sub page.
                    // The sub page has to paint the same PageBackground as the tab layer.
                    Box(modifier = Modifier.fillMaxSize()) {
                        PageBackground(state = state, dark = dark)
                        SubPageList(
                            subPage = subPage,
                            state = state,
                            dark = dark,
                            accentColor = accentColor,
                            listState = subListState,
                            contentPadding = contentPadding,
                            hasImageAccess = hasImageAccess,
                            pickBackground = pickBackground,
                            grantImageAccess = {
                                pendingBackgroundPick = false
                                requestImageAccess()
                            },
                            onBack = closeSubPage,
                            onRestart = { showRestartDialog = true },
                        )
                    }

                    TopScrim(statusBarInset, wallpaper = hasWallpaper)

                    ChromeIconButton(
                        icon = MiuixIcons.Refresh,
                        description = "重启作用域",
                        tint = accentColor,
                        endPadding = 16.dp,
                        onClick = { showRestartDialog = true },
                    )
                }
            }


            if (showRestartDialog) {
                GlassWindowDialog(
                    show = true,
                    onDismissRequest = { showRestartDialog = false },
                    title = "重启作用域",
                    summary = "将立即重启 SystemUI（com.android.systemui），使修改后的参数生效。",
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        DialogButton("取消", Modifier.weight(1f)) { showRestartDialog = false }
                        DialogButton("重启", Modifier.weight(1f), primary = true) {
                            showRestartDialog = false
                            onRestartScope()
                        }
                    }
                }
            }
        }
    }
    }
}

/** One top level route as a standalone list, reused by the pager and by predictive back. */
@Composable
private fun TabPage(
    tab: MainTab,
    state: SettingsState,
    dark: Boolean,
    accentColor: Color,
    listState: LazyListState,
    contentPadding: PaddingValues,
    interactive: Boolean,
    onOpenAppearance: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPresets: () -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .then(if (interactive) Modifier else BlockInput),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(CardGap),
        userScrollEnabled = interactive,
    ) {
        item { LargeTitle(tab.title, tab.subtitle, showBack = false, onBack = {}) }
        when (tab) {
            MainTab.HOME -> homePage(state, dark, accentColor)
            MainTab.TUNE -> tunePage(state, accentColor, onOpenPresets)
            MainTab.SETTINGS -> settingsPage(state, accentColor, onOpenAppearance, onOpenBackup, onOpenAbout)
        }
    }
}

@Composable
private fun SubPageList(
    subPage: SubPage,
    state: SettingsState,
    dark: Boolean,
    accentColor: Color,
    listState: LazyListState,
    contentPadding: PaddingValues,
    hasImageAccess: Boolean,
    pickBackground: () -> Unit,
    grantImageAccess: () -> Unit,
    onBack: () -> Unit,
    onRestart: () -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(CardGap),
    ) {
        item { LargeTitle(subPage.title, subPage.subtitle, showBack = true, onBack = onBack) }
        when (subPage) {
            SubPage.APPEARANCE -> appearancePage(
                state = state,
                dark = dark,
                accentColor = accentColor,
                hasImageAccess = hasImageAccess,
                onPickBackground = pickBackground,
                onGrantAccess = grantImageAccess,
            )
            SubPage.PARAMS -> paramsPage(state, dark, accentColor)
            SubPage.BACKUP -> backupPage(state, accentColor)
            SubPage.ABOUT -> aboutPage(state, accentColor, onRestart)
            SubPage.NONE -> Unit
        }
    }
}

/**
 * Fade under the status bar so content scrolling past it stays readable.
 *
 * The veil is neutral black rather than the page colour: with a wallpaper behind the page a white
 * scrim in light mode painted a bright band across the top of an otherwise dark screen.
 */
@Composable
private fun BoxScope.TopScrim(statusBarInset: Dp, wallpaper: Boolean) {
    // Over a wallpaper the scrim has to be neutral dark in both themes: a light-mode page colour
    // painted a bright white band across the top of an otherwise dark screen.
    val shade = if (wallpaper) {
        Color.Black.copy(alpha = if (isSystemInDarkTheme()) 0.45f else 0.22f)
    } else {
        MiuixTheme.colorScheme.background
    }
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(statusBarInset + 22.dp)
            .background(
                Brush.verticalGradient(
                    0f to shade,
                    0.78f to shade,
                    1f to shade.copy(alpha = 0f),
                )
            ),
    )
}

/**
 * The floating circular action in the top right corner.
 *
 * It floats over the wallpaper rather than over a card, so an opaque surfaceContainer would show up
 * as a solid white disc on every page. When the wallpaper is live it is drawn as a glass disc
 * against the same wallpaper recorder the cards use, which keeps it consistent with them; without a
 * wallpaper there is nothing to sample, so it stays a plain surface.
 */
@Composable
private fun BoxScope.ChromeIconButton(
    icon: ImageVector,
    description: String,
    tint: Color,
    endPadding: Dp,
    onClick: () -> Unit,
) {
    val backdrop = LocalCardBackdrop.current
    val state = LocalSettingsState.current
    val glass = backdrop != null && state != null && state.glassCards
    val shell = Modifier
        .align(Alignment.TopEnd)
        .statusBarsPadding()
        .padding(top = 12.dp, end = endPadding)
    if (!glass) {
        IconButton(
            onClick = onClick,
            modifier = shell.shadow(elevation = 6.dp, shape = CircleShape, clip = false),
            backgroundColor = MiuixTheme.colorScheme.surfaceContainer,
            cornerRadius = 22.dp,
            minWidth = 44.dp,
            minHeight = 44.dp,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier.size(22.dp),
                tint = tint,
            )
        }
        return
    }
    val shape = remember { CircleShape }
    val tintAlpha = (state!!.cardGlassTint / 100f).coerceIn(0f, 1f)
    val veil = resolveCardVeil(
        mode = state.cardGlassColorMode,
        custom = state.cardGlassCustom,
        dark = isSystemInDarkTheme(),
        wallpaperLuma = LocalWallpaperLuma.current,
    ).copy(alpha = 1f - tintAlpha)
    Box(
        modifier = shell
            .size(44.dp)
            .drawBackdrop(
                backdrop = backdrop!!,
                shape = { shape },
                effects = { vibrancy() },
                highlight = { Highlight.Plain },
                shadow = null,
                onDrawSurface = { drawRect(veil) },
            )
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(22.dp),
            tint = tint,
        )
    }
}

/** The huge flat left aligned title - the single most recognisable KernelSU / LSPosed cue. */
@Composable
private fun LargeTitle(
    title: String,
    subtitle: String,
    showBack: Boolean,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp, bottom = 2.dp)) {
        if (showBack) {
            IconButton(
                onClick = onBack,
                backgroundColor = Color.Transparent,
                minWidth = 40.dp,
                minHeight = 40.dp,
            ) {
                Icon(
                    imageVector = MiuixIcons.ChevronBackward,
                    contentDescription = "返回",
                    modifier = Modifier.size(26.dp),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onBackground,
        )
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onBackgroundVariant,
            )
        }
    }
}

/**
 * Floating pill navigation bar, a direct port of KernelSU's FloatingBottomBar.
 *
 * The pill is one tab wide and slides continuously with DampedDragAnimation, so pressing a tab
 * selects it immediately, holding keeps it swollen (pressedScale), and dragging scrubs across
 * tabs with velocity driven squash / stretch. InteractiveHighlight adds the specular blob that
 * follows the finger.
 */
@Composable
private fun BottomNavBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    accentColor: Color,
    glass: GlassSpec,
    glassEnabled: Boolean,
    glassStyle: BarGlassStyle,
    backdrop: com.kyant.backdrop.Backdrop,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    val pillShape = remember { CircleShape }
    // Over a wallpaper the panel has to lift the photo it is sampling; over the flat page colour
    // there is nothing to lift, and a brightness bump on a flat fill just washes the theme out.
    val wallpaperStyle = glassStyle == BarGlassStyle.WALLPAPER
    // The tint slider is the inverse of the veil's opacity: a higher 通透度 leaves more of the
    // blurred wallpaper visible, a lower one pushes the pill back towards an opaque surface.
    val glassVeil = (1f - glass.tint).coerceIn(0f, 1f)
    // AndroidLiquidGlass keeps its own container at 0.4 alpha in both themes rather than tying it
    // to the theme surface, which is what makes the panel read as smoked glass over a photo.
    val wallpaperContainer = if (dark) Color(0xFF121212).copy(alpha = 0.4f) else Color(0xFFFAFAFA).copy(alpha = 0.4f)
    val containerColor = if (!glassEnabled) {
        MiuixTheme.colorScheme.surfaceContainer
    } else if (wallpaperStyle) {
        wallpaperContainer.copy(alpha = wallpaperContainer.alpha * (0.4f + 0.6f * glassVeil))
    } else {
        MiuixTheme.colorScheme.surfaceContainer.copy(alpha = glassVeil)
    }
    // One shared effect block for the bar and its 56dp twin: they must stay pixel-identical or the
    // recorded highlight row stops lining up with the pill it refracts.
    val barEffects: com.kyant.backdrop.BackdropEffectScope.() -> Unit = {
        // Covers the blur radius and the refraction rim when the effects below do not raise the
        // padding themselves, so the recorded layer always has the pixels the shader samples.
        padding = maxOf(padding, 40.dp.toPx())
        if (wallpaperStyle) {
            colorControls(
                brightness = if (dark) 0.06f else 0.20f,
                contrast = 1f,
                saturation = 1.5f,
            )
        } else {
            vibrancy()
        }
        blur(glass.blur.toPx())
        val refractionPx = glass.refraction.toPx()
        if (refractionPx > 0f) {
            lens(
                refractionHeight = refractionPx,
                refractionAmount = refractionPx,
                depthEffect = wallpaperStyle,
            )
        }
    }
    // KernelSU lights the rim evenly (Ambient). AndroidLiquidGlass angles it with a wider falloff,
    // which is what reads as a light source above the panel rather than a uniform sticker edge.
    val barHighlight: () -> Highlight? = {
        Highlight(
            width = 1.dp,
            alpha = glass.highlight.coerceIn(0f, 1f),
            style = if (wallpaperStyle) {
                HighlightStyle.Default(angle = 45f, falloff = 2f)
            } else {
                HighlightStyle.Ambient
            },
        )
    }
    val density = LocalDensity.current
    val animationScope = rememberCoroutineScope()
    // Records the tab row itself so the pill can refract the icons and labels underneath it.
    val tabsBackdrop = rememberLayerBackdrop()
    val combinedBackdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop)
    val tabs = listOf(
        Triple(MainTab.HOME, MiuixIcons.Home, "主页"),
        Triple(MainTab.TUNE, MiuixIcons.Tune, "调节"),
        Triple(MainTab.SETTINGS, MiuixIcons.Settings, "设置"),
    )

    var tabWidthPx by remember { mutableFloatStateOf(0f) }
    var totalWidthPx by remember { mutableFloatStateOf(0f) }
    var activeIndex by remember { mutableIntStateOf(selectedIndex) }
    val onSelectUpdated by rememberUpdatedState(onSelect)

    fun indexAt(positionX: Float): Int {
        if (tabWidthPx == 0f) return activeIndex
        val horizontalPaddingPx = with(density) { 4.dp.toPx() }
        return ((positionX - horizontalPaddingPx) / tabWidthPx)
            .toInt()
            .coerceIn(0, tabs.size - 1)
    }

    val dampedDragAnimation = remember(animationScope, density) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = selectedIndex.toFloat(),
            valueRange = 0f..(tabs.size - 1).toFloat(),
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            // KernelSU uses 78f / 56f for its four tab, 304dp bar. Ours is narrower, so the
            // swell is dialled back to stay inside the pill instead of spilling over the edge.
            pressedScale = 1.18f,
            canDrag = { offset -> offset.x in 0f..totalWidthPx },
            onDragStarted = { position ->
                updateValue(indexAt(position.x).toFloat())
            },
            onDragStopped = {
                val targetIndex = targetValue.roundToInt().coerceIn(0, tabs.size - 1)
                if (activeIndex != targetIndex) {
                    activeIndex = targetIndex
                    onSelectUpdated(targetIndex)
                }
                updateValue(targetIndex.toFloat())
            },
            onDragCancelled = {
                updateValue(activeIndex.toFloat())
            },
            onDrag = { _, dragAmount ->
                if (tabWidthPx > 0f && dragAmount.x != 0f) {
                    updateValue(
                        (targetValue + dragAmount.x / tabWidthPx)
                            .coerceIn(0f, (tabs.size - 1).toFloat()),
                    )
                }
            },
        )
    }

    LaunchedEffect(selectedIndex) {
        if (activeIndex != selectedIndex) {
            activeIndex = selectedIndex
            dampedDragAnimation.animateToValue(selectedIndex.toFloat())
        }
    }

    val interactiveHighlight = remember(animationScope, tabWidthPx, dampedDragAnimation) {
        InteractiveHighlight(
            animationScope = animationScope,
            position = { size, _ ->
                Offset(
                    (dampedDragAnimation.value + 0.5f) * tabWidthPx,
                    size.height / 2f,
                )
            },
        )
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) { detectTapGestures { } }
            .width(IntrinsicSize.Min),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier
                .onGloballyPositioned { coords ->
                    totalWidthPx = coords.size.width.toFloat()
                    val contentWidthPx = totalWidthPx - with(density) { 8.dp.toPx() }
                    tabWidthPx = (contentWidthPx / tabs.size).coerceAtLeast(0f)
                }
                .selectableGroup()
                .then(
                    if (glassEnabled) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { pillShape },
                            effects = barEffects,
                            highlight = barHighlight,
                            layerBlock = {
                                val width = size.width.coerceAtLeast(1f)
                                val s = lerp(1f, 1f + 16.dp.toPx() / width, dampedDragAnimation.pressProgress)
                                scaleX = s
                                scaleY = s
                            },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                    } else {
                        Modifier
                            .shadow(elevation = 8.dp, shape = pillShape, clip = false)
                            .clip(pillShape)
                            .background(containerColor)
                    }
                )
                .then(
                    if (glassEnabled) {
                        interactiveHighlight.modifier.then(interactiveHighlight.gestureModifier)
                    } else {
                        Modifier
                    }
                )
                .then(dampedDragAnimation.modifier)
                .height(64.dp)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, (_, icon, label) ->
                NavBarItem(
                    icon = icon,
                    label = label,
                    selected = index == activeIndex,
                    accentColor = accentColor,
                    // No press-driven content scale: enlarging the label made it look blurry.
                    // defaultMinSize is what gives the Row a usable min intrinsic width; without
                    // it IntrinsicSize.Min measures every weighted child as zero and the bar collapses.
                    modifier = Modifier.defaultMinSize(minWidth = 84.dp),
                )
            }
        }

        if (glassEnabled) {
            // The duplicate, invisible row that KernelSU records into tabsBackdrop. Without it the
            // pill would paint the page behind the bar and swallow the selected icon.
            Row(
                modifier = Modifier
                    .clearAndSetSemantics {}
                    .alpha(0f)
                    .layerBackdrop(tabsBackdrop)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { pillShape },
                        effects = barEffects,
                        highlight = barHighlight,
                        onDrawSurface = { drawRect(containerColor) },
                    )
                    .then(interactiveHighlight.modifier)
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { (_, icon, label) ->
                    NavBarItem(
                        icon = icon,
                        label = label,
                        selected = true,
                        accentColor = accentColor,
                        modifier = Modifier.defaultMinSize(minWidth = 84.dp),
                    )
                }
            }
        }

        if (tabWidthPx > 0f) {
            val tabWidthDp = with(density) { tabWidthPx.toDp() }
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .graphicsLayer {
                        translationX = dampedDragAnimation.value * tabWidthPx
                    }
                    .then(
                        if (glassEnabled) {
                            Modifier.drawBackdrop(
                                backdrop = combinedBackdrop,
                                shape = { pillShape },
                                effects = {
                                    val progress = dampedDragAnimation.pressProgress
                                    lens(
                                        refractionHeight = 10.dp.toPx() * progress,
                                        refractionAmount = 14.dp.toPx() * progress,
                                        depthEffect = true,
                                        chromaticAberration = false,
                                    )
                                },
                                highlight = {
                                    Highlight(
                                        width = 1.dp,
                                        alpha = dampedDragAnimation.pressProgress,
                                        style = HighlightStyle.Ambient,
                                    )
                                },
                                layerBlock = {
                                    scaleX = dampedDragAnimation.scaleX
                                    scaleY = dampedDragAnimation.scaleY
                                    val velocity = dampedDragAnimation.velocity / 10f
                                    scaleX /= 1f - (velocity * 0.75f).coerceIn(-0.2f, 0.2f)
                                    scaleY *= 1f - (velocity * 0.25f).coerceIn(-0.2f, 0.2f)
                                },
                                onDrawSurface = {
                                    val progress = dampedDragAnimation.pressProgress
                                    drawRect(
                                        color = if (!dark) {
                                            Color.Black.copy(alpha = 0.1f)
                                        } else {
                                            Color.White.copy(alpha = 0.1f)
                                        },
                                        alpha = 1f - progress,
                                    )
                                    drawRect(Color.Black.copy(alpha = 0.03f * progress))
                                },
                            )
                        } else {
                            Modifier
                                .clip(pillShape)
                                .background(accentColor.copy(alpha = 0.15f))
                        }
                    )
                    .height(56.dp)
                    .width(tabWidthDp),
            )
        }
    }
}

@Composable
private fun RowScope.NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(BarIconSize),
            tint = if (selected) accentColor else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) accentColor else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
internal fun DialogButton(
    text: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    val tint = MiuixTheme.colorScheme.primary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (primary) tint else MiuixTheme.colorScheme.secondaryVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (primary) Color.White else MiuixTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Copies the picked image into our own filesDir so it survives the picker's transient grant.
 * Verifies the copy actually decodes before reporting success, the same way 慕容调度 does.
 */
internal fun persistBackground(context: Context, uri: Uri): String? {
    return try {
        val destination = File(context.filesDir, "background.img")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destination).use { output -> input.copyTo(output) }
        } ?: return null
        val decodable = decodeBackground(destination.absolutePath, 64) != null
        destination.absolutePath.takeIf { decodable }
    } catch (error: Throwable) {
        Log.w("IosBarSettings", "failed to persist background", error)
        null
    }
}
