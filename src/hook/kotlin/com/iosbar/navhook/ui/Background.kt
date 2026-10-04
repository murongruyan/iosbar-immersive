package com.iosbar.navhook.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.iosbar.navhook.BarConfig

/** Resolved bar colour used by the preview. */
fun resolvedBarColor(state: SettingsState, dark: Boolean): Color = when (state.colorMode) {
    BarConfig.COLOR_WHITE -> Color.White
    BarConfig.COLOR_BLACK -> Color.Black
    BarConfig.COLOR_CUSTOM -> Color(state.customColor)
    else -> if (dark) Color(0xFFEDF2FF) else Color(0xFF12151C)
}

fun resolvedBarColorLabel(mode: Int): String = when (mode) {
    BarConfig.COLOR_WHITE -> "白色"
    BarConfig.COLOR_BLACK -> "黑色"
    BarConfig.COLOR_CUSTOM -> "自定义"
    else -> "跟随系统"
}

/**
 * Longest edge kept when decoding a wallpaper. A 4000 px photo decodes to a ~48 MB ARGB bitmap at
 * full size; the background is cropped and usually blurred, so this bound loses nothing visible
 * while keeping peak memory predictable.
 */
private const val MAX_BACKGROUND_EDGE = 3200

/**
 * Decodes a wallpaper with a power-of-two sample size so oversized photos do not land in memory
 * at full resolution. Returns null when the file is missing or not a decodable image.
 */
internal fun decodeBackground(path: String, maxEdge: Int): android.graphics.Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxEdge || bounds.outHeight / sampleSize > maxEdge) {
        sampleSize *= 2
    }
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
}

/**
 * Average luminance (0..1) of a wallpaper, read from a 32 px thumbnail instead of the full size
 * image. The cards' 自动 colour mode is labelled as following the wallpaper, and it has to: a fixed
 * white film over a dark photo is what made every card look washed out. Measured once per path, so
 * it costs one tiny decode rather than a per-frame readback of the recorded layer.
 *
 * Only the *tint* is chosen from this, never a heavy opaque veil: one average over a photo cannot
 * describe the patch behind a given card, and letting it decide an opaque fill once turned a mostly
 * dark photo into near-black glass over a bright wall. A light or dark tint at the configured
 * opacity is insensitive to that error. Returns null when the file cannot be decoded.
 */
internal fun wallpaperAverageLuma(path: String): Float? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sampleSize = 1
    while (bounds.outWidth / sampleSize > 32 || bounds.outHeight / sampleSize > 32) {
        sampleSize *= 2
    }
    val bitmap = BitmapFactory.decodeFile(
        path,
        BitmapFactory.Options().apply { inSampleSize = sampleSize },
    ) ?: return null
    return try {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) {
            null
        } else {
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            var sum = 0.0
            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                sum += (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0
            }
            (sum / pixels.size).toFloat()
        }
    } finally {
        bitmap.recycle()
    }
}

/**
 * Page background: a flat Miuix surface colour, or the picked wallpaper when the user chose
 * the image mode. It is drawn inside the recorder so the floating bar can blur it.
 */
@Composable
fun PageBackground(state: SettingsState, dark: Boolean, modifier: Modifier = Modifier) {
    val path = state.backgroundPath
    // The same test the status bar and the top scrim use (see SettingsActivity): a picked file that
    // no longer exists decodes to nothing, so the page must fall back to the flat colour instead of
    // claiming it has a wallpaper.
    val wantImage = state.backgroundMode == "custom" &&
        path.isNotBlank() &&
        remember(path) { java.io.File(path).exists() }
    // Decoded off the main thread: a full resolution wallpaper decode blocks for hundreds of ms and
    // used to stall every navigation that recomposed a background layer.
    val bitmap by produceState<ImageBitmap?>(initialValue = null, wantImage, path) {
        value = if (!wantImage) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching { decodeBackground(path, MAX_BACKGROUND_EDGE)?.asImageBitmap() }.getOrNull()
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (dark) Color(0xFF141416) else Color(0xFFF5F5F7)),
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (state.backgroundBlur > 0) {
                            Modifier.blur(state.backgroundBlur.dp)
                        } else {
                            Modifier
                        }
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (dark) 0.30f else 0.10f)),
            )
        }
    }
}

/* ---------------------------------------------------------------------------------------- */
/* Live gesture bar preview                                                                  */
/* ---------------------------------------------------------------------------------------- */

/**
 * A truthful miniature of the bottom of the screen: the bar keeps its real dp geometry, scaled
 * by the ratio between the preview width and the device width, so the preview never lies.
 */
@Composable
fun BarPreview(state: SettingsState, dark: Boolean, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.toFloat().coerceAtLeast(1f)
    val barColor = resolvedBarColor(state, dark)
    Canvas(modifier = modifier) {
        val density = this.density
        val scale = size.width / (screenWidthDp * density)
        fun px(dp: Float) = dp * density * scale

        val screen = Size(size.width, size.height)
        val radius = 14.dp.toPx()

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = if (dark) {
                    listOf(Color(0xFF2A2A30), Color(0xFF1A1A1E))
                } else {
                    listOf(Color(0xFFDDE3F5), Color(0xFFF2F4FA))
                },
            ),
            size = screen,
            cornerRadius = CornerRadius(radius),
        )

        val notchWidth = screen.width * 0.20f
        val notchHeight = 8.dp.toPx()
        drawRoundRect(
            color = Color.Black.copy(alpha = if (dark) 0.8f else 0.7f),
            topLeft = Offset((screen.width - notchWidth) / 2f, 9.dp.toPx()),
            size = Size(notchWidth, notchHeight),
            cornerRadius = CornerRadius(notchHeight / 2f),
        )

        val statusTop = 10.dp.toPx()
        val statusHeight = 5.dp.toPx()
        drawRoundRect(
            color = barColor.copy(alpha = 0.30f),
            topLeft = Offset(screen.width * 0.08f, statusTop),
            size = Size(screen.width * 0.10f, statusHeight),
            cornerRadius = CornerRadius(statusHeight / 2f),
        )
        drawRoundRect(
            color = barColor.copy(alpha = 0.30f),
            topLeft = Offset(screen.width * 0.78f, statusTop),
            size = Size(screen.width * 0.14f, statusHeight),
            cornerRadius = CornerRadius(statusHeight / 2f),
        )

        val columns = 4
        val iconSize = screen.width * 0.125f
        val gapX = (screen.width - columns * iconSize) / (columns + 1)
        val gridTop = screen.height * 0.30f
        for (row in 0 until 2) {
            for (column in 0 until columns) {
                val left = gapX + column * (iconSize + gapX)
                val top = gridTop + row * (iconSize + gapX * 0.85f)
                if (top + iconSize > screen.height * 0.80f) continue
                drawRoundRect(
                    color = Color.White.copy(alpha = if (dark) 0.10f else 0.65f),
                    topLeft = Offset(left, top),
                    size = Size(iconSize, iconSize),
                    cornerRadius = CornerRadius(iconSize * 0.29f),
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = if (dark) 0.06f else 0.38f),
                    topLeft = Offset(left + iconSize * 0.24f, top + iconSize * 0.24f),
                    size = Size(iconSize * 0.52f, iconSize * 0.52f),
                    cornerRadius = CornerRadius(iconSize * 0.16f),
                )
            }
        }

        val barWidth = px(state.widthPortrait).coerceAtMost(screen.width * 0.92f).coerceAtLeast(8f)
        val barHeight = px(state.height).coerceAtLeast(1.5f)
        val bottomMargin = px(state.bottom)
        val barRadius = if (state.radius < 0f) barHeight / 2f else px(state.radius)
        val top = (screen.height - bottomMargin - barHeight).coerceAtLeast(0f)
        drawRoundRect(
            color = barColor.copy(alpha = (state.alpha / 100f).coerceIn(0f, 1f)),
            topLeft = Offset((screen.width - barWidth) / 2f, top),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barRadius),
        )
    }
}
