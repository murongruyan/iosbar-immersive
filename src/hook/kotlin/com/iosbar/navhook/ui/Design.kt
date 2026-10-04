package com.iosbar.navhook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.window.WindowDialog

/* ---------------------------------------------------------------------------------------- */
/* Tokens                                                                                    */
/* ---------------------------------------------------------------------------------------- */

/** KernelSU / InstallerX shape token: 16dp outer radius on every grouped card. */
val CardCorner: Dp = 16.dp

/** Horizontal screen margin used by the Miuix settings lists. */
val ScreenPadding: Dp = 12.dp

/** Gap between two grouped cards. */
val CardGap: Dp = 12.dp

/**
 * The shallow edge lens on the glass cards, in the AndroidLiquidGlass proportions (12/24 dp on a
 * button). Bigger values warp the whole panel instead of just lighting its border.
 */
private val CARD_LENS_HEIGHT: Dp = 12.dp
private val CARD_LENS_AMOUNT: Dp = 24.dp

@Immutable
data class AccentOption(
    val index: Int,
    val label: String,
    val light: Color,
    val dark: Color,
)

val AccentOptions: List<AccentOption> = listOf(
    AccentOption(0, "静谧蓝", Color(0xFF0A84FF), Color(0xFF4AA3FF)),
    AccentOption(1, "远山紫", Color(0xFF5E5CE6), Color(0xFF8F8DFF)),
    AccentOption(2, "晨曦粉", Color(0xFFFF375F), Color(0xFFFF6E8C)),
    AccentOption(3, "青柠绿", Color(0xFF12B76A), Color(0xFF3ED598)),
    AccentOption(4, "落日橙", Color(0xFFFF8A00), Color(0xFFFFAB3D)),
    AccentOption(5, "石墨灰", Color(0xFF5A6072), Color(0xFF9AA3B8)),
)

fun accentOption(index: Int): AccentOption =
    AccentOptions.getOrElse(index) { AccentOptions.first() }

/** Text colour modes offered by 外观与玻璃. */
const val TEXT_COLOR_AUTO = 0
const val TEXT_COLOR_WHITE = 1
const val TEXT_COLOR_BLACK = 2
const val TEXT_COLOR_CUSTOM = 3

/**
 * Resolves the ink used for every label, title and summary in the settings UI. Auto keeps the
 * scheme's own near-black / near-white pairs; the other modes collapse the whole hierarchy onto
 * one colour and derive the quieter tiers by alpha, so a custom ink stays readable at every level
 * instead of leaving half the text at its old colour.
 */
@Immutable
data class TextInk(val primary: Color, val secondary: Color, val summary: Color, val action: Color)

fun resolveTextInk(mode: Int, custom: Color, dark: Boolean): TextInk {
    val base = when (mode) {
        TEXT_COLOR_WHITE -> Color.White
        TEXT_COLOR_BLACK -> Color.Black
        TEXT_COLOR_CUSTOM -> custom
        else -> if (dark) Color(0xFFF2F2F2) else Color.Black
    }
    return TextInk(
        primary = base,
        secondary = base.copy(alpha = 0.80f),
        summary = base.copy(alpha = 0.60f),
        action = base.copy(alpha = 0.40f),
    )
}

/**
 * Miuix scheme with KernelSU surface values: near-white page, pure white cards in light mode,
 * #242424 cards on a #141416 page in dark mode. Everything else follows the chosen accent.
 */
fun accentScheme(accent: AccentOption, dark: Boolean, ink: TextInk? = null): Colors {
    val primary = if (dark) accent.dark else accent.light
    val soft = primary.copy(alpha = if (dark) 0.24f else 0.14f)
    val softer = primary.copy(alpha = if (dark) 0.34f else 0.24f)
    return if (dark) {
        darkColorScheme(
            primary = primary,
            primaryVariant = primary,
            primaryContainer = primary,
            tertiaryContainer = soft,
            tertiaryContainerVariant = soft,
            onTertiaryContainer = primary,
            disabledPrimary = softer,
            disabledPrimaryButton = softer,
            disabledPrimarySlider = softer,
            background = Color(0xFF141416),
            surface = Color(0xFF242424),
            surfaceContainer = Color(0xFF242424),
            surfaceVariant = Color(0xFF242424),
            dividerLine = Color(0xFF343438),
            outline = Color(0xFF3A3A3E),
            onBackground = ink?.primary ?: Color(0xE6FFFFFF),
            onBackgroundVariant = ink?.summary ?: Color(0xFF8C93B0),
            onSurface = ink?.primary ?: Color(0xFFF2F2F2),
            onSurfaceSecondary = ink?.secondary ?: Color(0xCCFFFFFF),
            onSurfaceVariantSummary = ink?.summary ?: Color(0x99FFFFFF),
            onSurfaceVariantActions = ink?.action ?: Color(0x66FFFFFF),
            onSurfaceContainer = ink?.primary ?: Color(0xFFF2F2F2),
            onSurfaceContainerVariant = ink?.summary ?: Color(0xFF959595),
            onSurfaceContainerHigh = ink?.summary ?: Color(0xFFA2A2A2),
            onSurfaceContainerHighest = ink?.primary ?: Color(0xFFF2F2F2),
        )
    } else {
        lightColorScheme(
            primary = primary,
            primaryVariant = primary,
            primaryContainer = primary,
            onPrimaryVariant = softer,
            tertiaryContainer = soft,
            tertiaryContainerVariant = soft,
            onTertiaryContainer = primary,
            disabledPrimary = softer,
            disabledPrimaryButton = softer,
            disabledPrimarySlider = softer,
            background = Color(0xFFF5F5F7),
            surface = Color.White,
            surfaceContainer = Color.White,
            surfaceVariant = Color.White,
            dividerLine = Color(0xFFE6E6E9),
            outline = Color(0xFFDDDDE2),
            onBackground = ink?.primary ?: Color.Black,
            onBackgroundVariant = ink?.summary ?: Color(0xFF8C93B0),
            onSurface = ink?.primary ?: Color.Black,
            onSurfaceSecondary = ink?.secondary ?: Color(0xCC000000),
            onSurfaceVariantSummary = ink?.summary ?: Color(0x99000000),
            onSurfaceVariantActions = ink?.action ?: Color(0x66000000),
            onSurfaceContainer = ink?.primary ?: Color.Black,
            onSurfaceContainerVariant = ink?.summary ?: Color(0xFF959595),
            onSurfaceContainerHigh = ink?.summary ?: Color(0xFFA2A2A2),
            onSurfaceContainerHighest = ink?.primary ?: Color.Black,
        )
    }
}

/* ---------------------------------------------------------------------------------------- */
/* Liquid glass (floating chrome only)                                                       */
/* ---------------------------------------------------------------------------------------- */

@Immutable
data class GlassSpec(
    val blur: Dp = 16.dp,
    val refraction: Dp = 22.dp,
    val highlight: Float = 0.9f,
    val tint: Float = 0.55f,
    val elevation: Dp = 10.dp,
)

/**
 * Which glass recipe the floating chrome renders with.
 *
 * The two are not interchangeable, because the thing behind the bar is different. Over a wallpaper
 * [WALLPAPER] is right: the panel has to lift and refract a photograph, which is what
 * AndroidLiquidGlass does. Over a flat page colour there is nothing to refract - the same recipe
 * reads as a milky film with a lens artefact in it - so [KERNEL_SU] is kept, which is the port the
 * bar was originally built from and the one that already looked right there.
 */
enum class BarGlassStyle {
    /** KernelSU's FloatingBottomBar recipe: vibrant blur, plain ambient rim, no colour lift. */
    KERNEL_SU,

    /** AndroidLiquidGlass recipe: colour lift, depth lens, angled rim and an inner shadow. */
    WALLPAPER,
}



/* ---------------------------------------------------------------------------------------- */
/* Building blocks                                                                           */
/* ---------------------------------------------------------------------------------------- */

/** 0 = auto (contrast the wallpaper), 1 = white, 2 = black, 3 = custom. */
const val CARD_COLOR_AUTO = 0
const val CARD_COLOR_WHITE = 1
const val CARD_COLOR_BLACK = 2
const val CARD_COLOR_CUSTOM = 3

/** The near-black veil used when the wallpaper itself is dark. */
private val DarkVeil = Color(0xFF1C1C1E)

/**
 * The colour painted over a glass card's blurred wallpaper.
 *
 * Auto follows the 壁纸, which is both what its own summary line promises and the only thing that
 * works for a panel this large. A card is not a pill: covering hundreds of dp with a fixed light
 * film turns the whole page into frosted plastic. Measured over the wallpaper shipped in this
 * build, a 38% white veil lifted the card interior from 29 to 115 luma - a flat grey slab that hid
 * the photo behind it completely, while the 底栏 pill sitting on the same photo read as real glass,
 * because a small pill can afford the veil a panel cannot.
 *
 * [wallpaperLuma] is the wallpaper's own average luminance, or null when there is no wallpaper.
 * The threshold is only ever asked "light or dark", never used as an opacity: one average over a
 * photo cannot describe the patch behind a given card, so it must not scale an opaque fill. A light
 * or dark tint at the user's configured opacity is insensitive to that error.
 */
fun resolveCardVeil(mode: Int, custom: Int, dark: Boolean, wallpaperLuma: Float?): Color = when (mode) {
    CARD_COLOR_WHITE -> Color.White
    CARD_COLOR_BLACK -> Color.Black
    CARD_COLOR_CUSTOM -> Color(custom)
    // No wallpaper to follow: fall back to the theme, which is what iOS does.
    else -> if (wallpaperLuma == null) (if (dark) DarkVeil else Color.White)
    else if (wallpaperLuma > 0.5f) Color.White else DarkVeil
}

/**
 * Wallpaper-only backdrop handed to the settings cards.
 *
 * A card cannot sample the backdrop it is drawn into: recording the whole page would make the card
 * blur its own previously drawn frame. The host records one layer that contains nothing but the
 * page background and hands it down here, so every card samples a stable wallpaper.
 */
val LocalCardBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/**
 * Average luminance of the picked wallpaper, or null when the page is a flat colour.
 *
 * Provided once by the host rather than decoded per card: the value is one thumbnail read of the
 * same file for every panel on the page. See [resolveCardVeil] for what it decides.
 */
val LocalWallpaperLuma = staticCompositionLocalOf<Float?> { null }

/**
 * Grouped settings container. Without card glass this is the flat Miuix / KernelSU card; with it
 * the card becomes a liquid glass panel that blurs and tints the wallpaper behind it.
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    corner: Dp = CardCorner,
    container: Color = MiuixTheme.colorScheme.surfaceContainer,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val backdrop = LocalCardBackdrop.current
    val state = LocalSettingsState.current
    val glass = backdrop != null && state != null && state.glassCards
    if (!glass) {
        Card(
            modifier = modifier.fillMaxWidth(),
            cornerRadius = corner,
            insideMargin = contentPadding,
            colors = CardDefaults.defaultColors(color = container),
            content = content,
        )
        return
    }
    val dark = isSystemInDarkTheme()
    val shape = remember(corner) { RoundedCornerShape(corner) }
    // The tint is what keeps the card readable: at 100% the wallpaper is fully visible, at 0% the
    // card equals an opaque surface again.
    val tintAlpha = (state!!.cardGlassTint / 100f).coerceIn(0f, 1f)
    val veil = resolveCardVeil(
        mode = state.cardGlassColorMode,
        custom = state.cardGlassCustom,
        dark = dark,
        wallpaperLuma = LocalWallpaperLuma.current,
    ).copy(alpha = 1f - tintAlpha)
    // AndroidLiquidGlass recipe, adapted to this app's recorder. The demo blurs each card's own
    // rect; here that blur is already baked into the shared wallpaper layer (see LocalCardBackdrop),
    // so the card samples pre-blurred pixels and adding blur() again would silently double the
    // radius the slider promises - and pay a per-card render pass for it. What the demo has that
    // this card was missing is the colour lift, the depth lens and the lit rim, which are what
    // actually read as glass.
    val lensHeightPx = with(LocalDensity.current) { CARD_LENS_HEIGHT.toPx() }
    val lensAmountPx = with(LocalDensity.current) { CARD_LENS_AMOUNT.toPx() }
    val sheer = veil.alpha < 0.5f
    // AndroidLiquidGlass separates a glass panel from frosted plastic with three things this card
    // was still missing: a brightness lift on the sampled backdrop (its DialogContent lifts by 0.2
    // in the light theme), a lens with depthEffect so the rim reads as a bevelled edge rather than
    // a flat border, and an inner shadow that darkens the panel just inside that rim.
    //
    // The lift is what stops the card reading as grey film over a photo. It has to fade out as the
    // veil gets opaque: colorControls sits *behind* onDrawSurface, so on an opaque card it would
    // only wash out the veil colour the user picked. At full transparency there is no veil to
    // fight, so the lift runs at full strength.
    val lift = (1f - veil.alpha * 2f).coerceIn(0f, 1f)
    // The veil decides what the card looks like, but the palette decides what the text on it looks
    // like - and the two are configured independently. Picking 卡片颜色 = 黑色 in the light theme used
    // to leave near-black ink on a near-black panel: text unreadable, and the whole page reading as
    // "everything turned dark". Ink is therefore derived from the veil actually composited over
    // whatever ends up behind the text.
    //
    // That backdrop is the wallpaper, not the page colour. Measuring the veil over the page's flat
    // light background said "light, use black ink", while the card was in fact showing a dark photo
    // through a dark veil - black text on near-black glass. The wallpaper's own luminance is the
    // only honest answer for a translucent card; the page colour is the fallback for the flat
    // background, where it is exactly right.
    val pageBg = MiuixTheme.colorScheme.background
    val backdropLuma = LocalWallpaperLuma.current
    val cardInk = remember(veil, pageBg, backdropLuma) {
        val behind = if (backdropLuma == null) pageBg else Color(backdropLuma, backdropLuma, backdropLuma)
        // Thresholded on the sRGB composite, not on Color.luminance(): the latter is WCAG relative
        // luminance in linear light, where "light" only starts at sRGB 0.74. A card sitting at 0.5
        // grey is not light, and reading it as light put white text on a pale panel.
        val shown = veil.compositeOver(behind)
        val brightness = (shown.red + shown.green + shown.blue) / 3f
        if (brightness > 0.5f) Color.Black else Color.White
    }
    val base = MiuixTheme.colorScheme
    val cardScheme = remember(base, cardInk) {
        base.copy(
            onBackground = cardInk,
            onSurface = cardInk,
            onSurfaceContainer = cardInk,
            onSurfaceContainerHigh = cardInk,
            onSurfaceContainerHighest = cardInk,
            onSurfaceSecondary = cardInk.copy(alpha = 0.80f),
            onSurfaceVariantSummary = cardInk.copy(alpha = 0.60f),
            onSurfaceVariantActions = cardInk.copy(alpha = 0.80f),
            onSurfaceContainerVariant = cardInk.copy(alpha = 0.60f),
            disabledOnSurface = cardInk.copy(alpha = 0.40f),
            outline = cardInk.copy(alpha = 0.14f),
            dividerLine = cardInk.copy(alpha = 0.12f),
            sliderBackground = cardInk.copy(alpha = 0.16f),
        )
    }
    // A nested theme, not a global one: only the inside of a glass card gets the veil-derived ink,
    // so the page, the bar and the popups keep the colours the user configured for them.
    MiuixTheme(colors = cardScheme) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .drawBackdrop(
                    backdrop = backdrop!!,
                    shape = { shape },
                    effects = {
                        // colourControls first, then the lens: the lens samples whatever the chain
                        // has produced so far, so lifting after it would flatten the bevelled edge.
                        colorControls(
                            brightness = 0.16f * lift,
                            contrast = 1f + 0.06f * lift,
                            saturation = 1.5f,
                        )
                        if (lensHeightPx > 0f && lensAmountPx > 0f) {
                            lens(lensHeightPx, lensAmountPx, depthEffect = true)
                        }
                    },
                    // A lit rim is what separates a glass panel from a flat tint. Highlight.Plain is
                    // the same rim the AndroidLiquidGlass sheet uses and needs no runtime shader.
                    // It costs a GraphicsLayer per card, so it is only drawn while the card is
                    // sheer - on an opaque veil the rim is invisible against the veil anyway.
                    highlight = if (sheer) { { Highlight.Plain } } else { null },
                    // The bead of shadow just inside that rim, straight from the demo: it is what
                    // gives the panel thickness instead of letting it read as a cut-out. It is the
                    // one part of the recipe that needs a whole extra offscreen layer per card, so
                    // it stays opt-in, and even switched on it is only drawn while the card is
                    // sheer - on an opaque veil it is invisible anyway.
                    shadow = null,
                    innerShadow = if (sheer && state!!.cardInnerShadow) {
                        // InnerShadow.alpha is a multiplier on InnerShadow.color, whose own default is
                        // only 15% black - passing alpha alone lands at ~7% and is invisible. The
                        // strength belongs in the colour, and the reference project uses this effect
                        // for press feedback, so a permanent card bead has to be stronger than that.
                        {
                            InnerShadow(
                                radius = 12.dp,
                                offset = DpOffset(0.dp, 2.dp),
                                color = Color.Black.copy(alpha = 0.40f),
                            )
                        }
                    } else {
                        null
                    },
                    onDrawSurface = { drawRect(veil) },
                ),
            cornerRadius = corner,
            insideMargin = contentPadding,
            // The surface must be transparent or it paints over the backdrop the modifier just drew.
            // contentColor has to be passed explicitly as well: the Card publishes it as
            // LocalContentColor, and the nested theme above does not provide that local itself.
            colors = CardDefaults.defaultColors(color = Color.Transparent, contentColor = cardInk),
            content = content,
        )
    }
}

/**
 * Segmented control used inside the glass cards.
 *
 * Miuix's own [TabRow] paints its track with an opaque, unclipped [Color] rectangle, so on a
 * translucent card the strip shows up as a white board hanging over the card's rounded corners and
 * its selected pill disappears whenever the track and the pill resolve to the same colour. This
 * wrapper keeps the Miuix metrics but derives every colour from the card surface underneath, and
 * clips the strip to the card's own radius.
 */
@Composable
fun GlassTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    corner: Dp = 12.dp,
) {
    val scheme = MiuixTheme.colorScheme
    // The cards are translucent, so the strip has to be translucent too. A low-alpha wash of the
    // page ink reads as "recessed track" on both a dark glass card and a light one, unlike the
    // scheme's surface, which is opaque white in light mode.
    val track = scheme.onSurface.copy(alpha = 0.06f)
    // The pill is a fill of the page ink rather than the scheme's container: that keeps it visible
    // against the track in both themes, and it is tinted by the selected content colour so the
    // label on top stays legible.
    val pill = scheme.onSurface.copy(alpha = 0.14f)
    TabRow(
        tabs = tabs,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = onTabSelected,
        modifier = modifier.clip(RoundedCornerShape(corner)),
        colors = TabRowDefaults.tabRowColors(
            backgroundColor = track,
            contentColor = scheme.onSurfaceVariantSummary,
            selectedBackgroundColor = pill,
            selectedContentColor = scheme.onSurface,
        ),
    )
}

/** Leading 24dp monochrome icon, exactly like the KernelSU / LSPosed preference rows. */
@Composable
fun SettingIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MiuixTheme.colorScheme.onSurface,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier.size(24.dp),
        tint = tint,
    )
}


/** Hairline divider inset so it starts where the row title starts. */
@Composable
fun InsetDivider(startIndent: Dp = 50.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startIndent)
            .height(1.dp)
            .background(MiuixTheme.colorScheme.dividerLine),
    )
}

/** Label / value row used in the device information card. */
@Composable
fun KeyValueRow(
    label: String,
    value: String,
    valueColor: Color = MiuixTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MiuixTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            textAlign = TextAlign.End,
        )
    }
}

/** Murong-style outlined action button: accent tinted fill, accent border, centred label. */
@Composable
fun OutlinedActionButton(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.15f))
            .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )
    }
}

/** Bold accent card title, matching the Murong info card header. */
@Composable
fun CardTitle(text: String, accent: Color) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = accent,
    )
}

/** Small readout pill used by the preview card footer. */
@Composable
fun MetricPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MiuixTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

/** The gesture bar mark, drawn as a rounded gradient square with a white bar inside. */
@Composable
fun BarMark(modifier: Modifier = Modifier, size: Dp = 96.dp, accent: Color) {
    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.29f))
                .background(
                    Brush.linearGradient(
                        colors = listOf(accent, accent.copy(alpha = 0.7f)),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    )
                ),
        )
        Box(
            modifier = Modifier
                .size(size)
                .drawBehind {
                    val barWidth = this.size.width * 0.46f
                    val barHeight = this.size.height * 0.10f
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.4f),
                        topLeft = Offset((this.size.width - barWidth * 0.7f) / 2f, this.size.height * 0.31f),
                        size = Size(barWidth * 0.7f, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2f),
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset((this.size.width - barWidth) / 2f, this.size.height * 0.57f),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2f),
                    )
                },
        )
    }
}

/**
 * Miuix's [WindowDialog] paints its own opaque surface, which is why the restart prompt reads as a
 * white slab on a page whose every other surface is glass. This wrapper keeps Miuix's metrics and
 * content slot but swaps that slab for the very recipe the cards use: the page's recorded wallpaper
 * layer, a colour lift, a lit rim and a translucent film over it.
 *
 * The dialog lives in its own window, but the recorded backdrop is a layer this process already
 * owns, so the dialog can sample it exactly like a card does. When there is no backdrop to sample
 * (glass cards switched off, flat page) Miuix's opaque surface stays: a dialog has to stay readable.
 */
@Composable
fun GlassWindowDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    summary: String,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalCardBackdrop.current
    val dark = isSystemInDarkTheme()
    val corner = 28.dp
    // AndroidLiquidGlass's sheet film, shrunk to a dialog. Slightly more opaque than a card: the
    // dialog sits over busy content, and its buttons have to stay obviously tappable.
    val film = if (dark) Color(0xFF121212).copy(alpha = 0.66f) else Color(0xFFFAFAFA).copy(alpha = 0.66f)
    val glass = if (backdrop == null) {
        Modifier
    } else {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { RoundedCornerShape(corner) },
            effects = {
                // Same order as the cards: lift first, then the lens, so the bevelled rim the lens
                // adds is not flattened by the colour filter.
                padding = maxOf(padding, 40.dp.toPx())
                colorControls(brightness = if (dark) 0.06f else 0.16f, contrast = 1f, saturation = 1.5f)
                lens(12.dp.toPx(), 24.dp.toPx(), depthEffect = true)
            },
            highlight = { Highlight.Plain },
            shadow = null,
            innerShadow = null,
            onDrawSurface = { drawRect(film) },
        )
    }
    WindowDialog(
        show = show,
        modifier = glass,
        onDismissRequest = onDismissRequest,
        title = title,
        summary = summary,
        backgroundColor = if (backdrop == null) MiuixTheme.colorScheme.surfaceContainer else Color.Transparent,
        cornerRadius = corner,
        content = content,
    )
}


