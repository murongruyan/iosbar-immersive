package com.iosbar.navhook.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.iosbar.navhook.BarConfig
import com.iosbar.navhook.SettingsStore
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

/** One switchable set of bar parameters. The active preset is what the hook consumes. */
@Immutable
data class BarPreset(
    val id: String,
    val name: String,
    val widthPortrait: Float,
    val widthLandscape: Float,
    val height: Float,
    val bottom: Float,
    val radius: Float,
    val alpha: Int,
    val colorMode: Int,
    val customColor: Int,
) {
    fun summary(): String {
        val radiusText = if (radius < 0f) "胶囊" else String.format("%.1f dp", radius)
        return widthPortrait.roundToInt().toString() + " / " + widthLandscape.roundToInt() +
            " dp · 高 " + String.format("%.1f", height) + " dp · 边距 " +
            String.format("%.1f", bottom) + " dp · " + radiusText +
            " · " + resolvedBarColorLabel(colorMode)
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("wp", widthPortrait.toDouble())
        .put("wl", widthLandscape.toDouble())
        .put("h", height.toDouble())
        .put("b", bottom.toDouble())
        .put("r", radius.toDouble())
        .put("a", alpha)
        .put("cm", colorMode)
        .put("cc", customColor)

    companion object {
        fun fromJson(json: JSONObject): BarPreset = BarPreset(
            id = json.optString("id"),
            name = json.optString("name"),
            widthPortrait = json.optDouble("wp", SettingsStore.DEFAULT_WIDTH_PORTRAIT.toDouble()).toFloat(),
            widthLandscape = json.optDouble("wl", SettingsStore.DEFAULT_WIDTH_LANDSCAPE.toDouble()).toFloat(),
            height = json.optDouble("h", SettingsStore.DEFAULT_HEIGHT.toDouble()).toFloat(),
            bottom = json.optDouble("b", SettingsStore.DEFAULT_BOTTOM.toDouble()).toFloat(),
            radius = json.optDouble("r", SettingsStore.DEFAULT_RADIUS.toDouble()).toFloat(),
            alpha = json.optInt("a", SettingsStore.DEFAULT_ALPHA),
            colorMode = json.optInt("cm", SettingsStore.DEFAULT_COLOR_MODE),
            customColor = json.optInt("cc", SettingsStore.DEFAULT_CUSTOM_COLOR),
        )
    }
}

private const val PRESET_IOS = "ios"
private const val PRESET_COLOROS = "coloros"
private const val PRESET_CURRENT = "current"

@Stable
class SettingsState(
    private val preferences: SharedPreferences,
    private val backupPreferences: SharedPreferences,
) {
    var enabled by mutableStateOf(
        SettingsStore.getBoolean(preferences, SettingsStore.KEY_ENABLED, SettingsStore.DEFAULT_ENABLED)
    )
    var immersive by mutableStateOf(
        SettingsStore.getBoolean(preferences, SettingsStore.KEY_IMMERSIVE, SettingsStore.DEFAULT_IMMERSIVE)
    )
    var removeScrim by mutableStateOf(
        SettingsStore.getBoolean(preferences, SettingsStore.KEY_REMOVE_SCRIM, SettingsStore.DEFAULT_REMOVE_SCRIM)
    )
    var widthPortrait by mutableFloatStateOf(
        SettingsStore.getFloat(preferences, SettingsStore.KEY_WIDTH_PORTRAIT, SettingsStore.DEFAULT_WIDTH_PORTRAIT)
    )
    var widthLandscape by mutableFloatStateOf(
        SettingsStore.getFloat(preferences, SettingsStore.KEY_WIDTH_LANDSCAPE, SettingsStore.DEFAULT_WIDTH_LANDSCAPE)
    )
    var height by mutableFloatStateOf(
        SettingsStore.getFloat(preferences, SettingsStore.KEY_HEIGHT, SettingsStore.DEFAULT_HEIGHT)
    )
    var bottom by mutableFloatStateOf(
        SettingsStore.getFloat(preferences, SettingsStore.KEY_BOTTOM, SettingsStore.DEFAULT_BOTTOM)
    )
    var radius by mutableFloatStateOf(
        SettingsStore.getFloat(preferences, SettingsStore.KEY_RADIUS, SettingsStore.DEFAULT_RADIUS)
    )
    var alpha by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_ALPHA, SettingsStore.DEFAULT_ALPHA)
    )
    var colorMode by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_COLOR_MODE, SettingsStore.DEFAULT_COLOR_MODE)
    )
    var customColor by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_CUSTOM_COLOR, SettingsStore.DEFAULT_CUSTOM_COLOR)
    )
    var backgroundMode by mutableStateOf(
        SettingsStore.getString(preferences, SettingsStore.KEY_BACKGROUND_MODE, SettingsStore.DEFAULT_BACKGROUND_MODE)
    )
    var backgroundPath by mutableStateOf(
        SettingsStore.getString(preferences, SettingsStore.KEY_BACKGROUND_PATH, SettingsStore.DEFAULT_BACKGROUND_PATH)
    )
    var backgroundBlur by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_BACKGROUND_BLUR, SettingsStore.DEFAULT_BACKGROUND_BLUR)
    )
    var accent by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_ACCENT, SettingsStore.DEFAULT_ACCENT)
    )
    var glassBlur by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_GLASS_BLUR, SettingsStore.DEFAULT_GLASS_BLUR)
    )
    var glassRefraction by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_GLASS_REFRACTION, SettingsStore.DEFAULT_GLASS_REFRACTION)
    )
    var glassHighlight by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_GLASS_HIGHLIGHT, SettingsStore.DEFAULT_GLASS_HIGHLIGHT)
    )
    var glassTint by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_GLASS_TINT, SettingsStore.DEFAULT_GLASS_TINT)
    )
    var glassNavbar by mutableStateOf(
        SettingsStore.getBoolean(preferences, SettingsStore.KEY_GLASS_NAVBAR, SettingsStore.DEFAULT_GLASS_NAVBAR)
    )
    var glassCards by mutableStateOf(
        SettingsStore.getBoolean(preferences, SettingsStore.KEY_GLASS_CARDS, SettingsStore.DEFAULT_GLASS_CARDS)
    )
    var cardGlassBlur by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_CARD_GLASS_BLUR, SettingsStore.DEFAULT_CARD_GLASS_BLUR)
    )
    var cardGlassTint by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_CARD_GLASS_TINT, SettingsStore.DEFAULT_CARD_GLASS_TINT)
    )
    var cardGlassColorMode by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_CARD_GLASS_COLOR, SettingsStore.DEFAULT_CARD_GLASS_COLOR)
    )
    var cardGlassCustom by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_CARD_GLASS_CUSTOM, SettingsStore.DEFAULT_CARD_GLASS_CUSTOM)
    )
    var cardInnerShadow by mutableStateOf(
        SettingsStore.getBoolean(
            preferences,
            SettingsStore.KEY_CARD_INNER_SHADOW,
            SettingsStore.DEFAULT_CARD_INNER_SHADOW
        )
    )
    var textColorMode by mutableIntStateOf(
        SettingsStore.getInt(preferences, SettingsStore.KEY_TEXT_COLOR, SettingsStore.DEFAULT_TEXT_COLOR)
    )
    var textColorCustom by mutableIntStateOf(
        SettingsStore.getInt(
            preferences,
            SettingsStore.KEY_TEXT_COLOR_CUSTOM,
            SettingsStore.DEFAULT_TEXT_COLOR_CUSTOM
        )
    )

    var presets by mutableStateOf<List<BarPreset>>(emptyList())
        private set
    var activePresetId by mutableStateOf("")
        private set

    init {
        val stored = SettingsStore.getString(preferences, SettingsStore.KEY_PRESETS, "")
        val parsed = decodePresets(stored)
        if (parsed.isEmpty()) {
            val ios = BarPreset(
                PRESET_IOS, "iOS 预设",
                SettingsStore.DEFAULT_WIDTH_PORTRAIT, SettingsStore.DEFAULT_WIDTH_LANDSCAPE,
                SettingsStore.DEFAULT_HEIGHT, SettingsStore.DEFAULT_BOTTOM, SettingsStore.DEFAULT_RADIUS,
                SettingsStore.DEFAULT_ALPHA, BarConfig.COLOR_AUTO, SettingsStore.DEFAULT_CUSTOM_COLOR,
            )
            val colorOs = BarPreset(
                PRESET_COLOROS, "ColorOS 默认", 120f, 200f, 4f, 7f, 2f, 100,
                BarConfig.COLOR_AUTO, SettingsStore.DEFAULT_CUSTOM_COLOR,
            )
            // Keep whatever the user already had as its own preset instead of silently applying
            // a built-in over their live values.
            val matchesBuiltIn = listOf(ios, colorOs).firstOrNull { it.matchesLive() }
            presets = if (matchesBuiltIn != null) {
                listOf(ios, colorOs)
            } else {
                listOf(liveAsPreset(PRESET_CURRENT, "当前参数"), ios, colorOs)
            }
            activePresetId = matchesBuiltIn?.id ?: PRESET_CURRENT
        } else {
            presets = parsed
            val storedActive = SettingsStore.getString(preferences, SettingsStore.KEY_ACTIVE_PRESET, "")
            activePresetId = parsed.firstOrNull { it.id == storedActive }?.id ?: parsed.first().id
        }
    }

    private fun liveAsPreset(id: String, name: String) = BarPreset(
        id = id,
        name = name,
        widthPortrait = widthPortrait,
        widthLandscape = widthLandscape,
        height = height,
        bottom = bottom,
        radius = radius,
        alpha = alpha,
        colorMode = colorMode,
        customColor = customColor,
    )

    private fun BarPreset.matchesLive(): Boolean =
        widthPortrait == this@SettingsState.widthPortrait &&
            widthLandscape == this@SettingsState.widthLandscape &&
            height == this@SettingsState.height &&
            bottom == this@SettingsState.bottom &&
            radius == this@SettingsState.radius &&
            alpha == this@SettingsState.alpha &&
            colorMode == this@SettingsState.colorMode

    private fun encodePresets(): String {
        val array = JSONArray()
        presets.forEach { array.put(it.toJson()) }
        return array.toString()
    }

    private fun decodePresets(raw: String): List<BarPreset> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                array.optJSONObject(index)?.let { BarPreset.fromJson(it) }
            }.filter { it.id.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun SharedPreferences.Editor.putAll(): SharedPreferences.Editor = this
        .putBoolean(SettingsStore.KEY_ENABLED, enabled)
        .putBoolean(SettingsStore.KEY_IMMERSIVE, immersive)
        .putBoolean(SettingsStore.KEY_REMOVE_SCRIM, removeScrim)
        .putFloat(SettingsStore.KEY_WIDTH_PORTRAIT, widthPortrait)
        .putFloat(SettingsStore.KEY_WIDTH_LANDSCAPE, widthLandscape)
        .putFloat(SettingsStore.KEY_HEIGHT, height)
        .putFloat(SettingsStore.KEY_BOTTOM, bottom)
        .putFloat(SettingsStore.KEY_RADIUS, radius)
        .putInt(SettingsStore.KEY_ALPHA, alpha)
        .putInt(SettingsStore.KEY_COLOR_MODE, colorMode)
        .putInt(SettingsStore.KEY_CUSTOM_COLOR, customColor)
        .putString(SettingsStore.KEY_BACKGROUND_MODE, backgroundMode)
        .putString(SettingsStore.KEY_BACKGROUND_PATH, backgroundPath)
        .putInt(SettingsStore.KEY_BACKGROUND_BLUR, backgroundBlur)
        .putInt(SettingsStore.KEY_ACCENT, accent)
        .putInt(SettingsStore.KEY_GLASS_BLUR, glassBlur)
        .putInt(SettingsStore.KEY_GLASS_REFRACTION, glassRefraction)
        .putInt(SettingsStore.KEY_GLASS_HIGHLIGHT, glassHighlight)
        .putInt(SettingsStore.KEY_GLASS_TINT, glassTint)
        .putBoolean(SettingsStore.KEY_GLASS_NAVBAR, glassNavbar)
        .putBoolean(SettingsStore.KEY_GLASS_CARDS, glassCards)
        .putInt(SettingsStore.KEY_CARD_GLASS_BLUR, cardGlassBlur)
        .putInt(SettingsStore.KEY_CARD_GLASS_TINT, cardGlassTint)
        .putInt(SettingsStore.KEY_CARD_GLASS_COLOR, cardGlassColorMode)
        .putInt(SettingsStore.KEY_CARD_GLASS_CUSTOM, cardGlassCustom)
        .putBoolean(SettingsStore.KEY_CARD_INNER_SHADOW, cardInnerShadow)
        .putInt(SettingsStore.KEY_TEXT_COLOR, textColorMode)
        .putInt(SettingsStore.KEY_TEXT_COLOR_CUSTOM, textColorCustom)
        .putString(SettingsStore.KEY_PRESETS, encodePresets())
        .putString(SettingsStore.KEY_ACTIVE_PRESET, activePresetId)
        .putLong(SettingsStore.KEY_REVISION, System.currentTimeMillis())

    /** Any live edit is folded back into the active preset, so tweaks stick to that preset. */
    private fun syncActivePresetFromLive() {
        val index = presets.indexOfFirst { it.id == activePresetId }
        if (index < 0) return
        val updated = presets[index].copy(
            widthPortrait = widthPortrait,
            widthLandscape = widthLandscape,
            height = height,
            bottom = bottom,
            radius = radius,
            alpha = alpha,
            colorMode = colorMode,
            customColor = customColor,
        )
        if (updated != presets[index]) {
            presets = presets.toMutableList().also { it[index] = updated }
        }
    }

    fun commit() {
        syncActivePresetFromLive()
        preferences.edit().putAll().apply()
    }

    fun backup() {
        backupPreferences.edit().putAll().apply()
    }

    fun applyPreset(id: String) {
        val preset = presets.firstOrNull { it.id == id } ?: return
        activePresetId = id
        widthPortrait = preset.widthPortrait
        widthLandscape = preset.widthLandscape
        height = preset.height
        bottom = preset.bottom
        radius = preset.radius
        alpha = preset.alpha
        colorMode = preset.colorMode
        customColor = preset.customColor
        commit()
    }

    fun addPreset() {
        val preset = liveAsPreset(
            id = "preset-" + System.currentTimeMillis(),
            name = "预设 " + (presets.size + 1),
        )
        presets = presets + preset
        activePresetId = preset.id
        commit()
    }

    fun duplicatePreset(id: String) {
        val source = presets.firstOrNull { it.id == id } ?: return
        val copy = source.copy(
            id = "preset-" + System.currentTimeMillis(),
            name = source.name + " 副本",
        )
        val index = presets.indexOfFirst { it.id == id }
        presets = presets.toMutableList().also { it.add(index + 1, copy) }
        activePresetId = copy.id
        commit()
    }

    fun renamePreset(id: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        presets = presets.map { if (it.id == id) it.copy(name = trimmed) else it }
        commit()
    }

    fun deletePreset(id: String) {
        if (presets.size <= 1) return
        val remaining = presets.filterNot { it.id == id }
        presets = remaining
        if (activePresetId == id) {
            activePresetId = remaining.first().id
            val preset = remaining.first()
            widthPortrait = preset.widthPortrait
            widthLandscape = preset.widthLandscape
            height = preset.height
            bottom = preset.bottom
            radius = preset.radius
            alpha = preset.alpha
            colorMode = preset.colorMode
            customColor = preset.customColor
        }
        commit()
    }

    fun restoreBackup() {
        enabled = SettingsStore.getBoolean(backupPreferences, SettingsStore.KEY_ENABLED, enabled)
        immersive = SettingsStore.getBoolean(backupPreferences, SettingsStore.KEY_IMMERSIVE, immersive)
        removeScrim = SettingsStore.getBoolean(backupPreferences, SettingsStore.KEY_REMOVE_SCRIM, removeScrim)
        widthPortrait = SettingsStore.getFloat(backupPreferences, SettingsStore.KEY_WIDTH_PORTRAIT, widthPortrait)
        widthLandscape = SettingsStore.getFloat(backupPreferences, SettingsStore.KEY_WIDTH_LANDSCAPE, widthLandscape)
        height = SettingsStore.getFloat(backupPreferences, SettingsStore.KEY_HEIGHT, height)
        bottom = SettingsStore.getFloat(backupPreferences, SettingsStore.KEY_BOTTOM, bottom)
        radius = SettingsStore.getFloat(backupPreferences, SettingsStore.KEY_RADIUS, radius)
        alpha = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_ALPHA, alpha)
        colorMode = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_COLOR_MODE, colorMode)
        customColor = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_CUSTOM_COLOR, customColor)
        backgroundMode = SettingsStore.getString(backupPreferences, SettingsStore.KEY_BACKGROUND_MODE, backgroundMode)
        backgroundPath = SettingsStore.getString(backupPreferences, SettingsStore.KEY_BACKGROUND_PATH, backgroundPath)
        backgroundBlur = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_BACKGROUND_BLUR, backgroundBlur)
        accent = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_ACCENT, accent)
        glassBlur = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_GLASS_BLUR, glassBlur)
        glassRefraction = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_GLASS_REFRACTION, glassRefraction)
        glassHighlight = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_GLASS_HIGHLIGHT, glassHighlight)
        glassTint = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_GLASS_TINT, glassTint)
        glassNavbar = SettingsStore.getBoolean(backupPreferences, SettingsStore.KEY_GLASS_NAVBAR, glassNavbar)
        glassCards = SettingsStore.getBoolean(backupPreferences, SettingsStore.KEY_GLASS_CARDS, glassCards)
        cardGlassBlur = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_CARD_GLASS_BLUR, cardGlassBlur)
        cardGlassTint = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_CARD_GLASS_TINT, cardGlassTint)
        cardGlassColorMode = SettingsStore.getInt(
            backupPreferences,
            SettingsStore.KEY_CARD_GLASS_COLOR,
            cardGlassColorMode
        )
        cardGlassCustom = SettingsStore.getInt(
            backupPreferences,
            SettingsStore.KEY_CARD_GLASS_CUSTOM,
            cardGlassCustom
        )
        cardInnerShadow = SettingsStore.getBoolean(
            backupPreferences,
            SettingsStore.KEY_CARD_INNER_SHADOW,
            cardInnerShadow
        )
        textColorMode = SettingsStore.getInt(backupPreferences, SettingsStore.KEY_TEXT_COLOR, textColorMode)
        textColorCustom = SettingsStore.getInt(
            backupPreferences,
            SettingsStore.KEY_TEXT_COLOR_CUSTOM,
            textColorCustom
        )
        commit()
    }

    fun resetToIosPreset() {
        applyPreset(presets.firstOrNull { it.id == PRESET_IOS }?.id ?: return)
    }

    fun resetToSystem() {
        applyPreset(presets.firstOrNull { it.id == PRESET_COLOROS }?.id ?: return)
    }

    fun resetGlass() {
        accent = SettingsStore.DEFAULT_ACCENT
        glassBlur = SettingsStore.DEFAULT_GLASS_BLUR
        glassRefraction = SettingsStore.DEFAULT_GLASS_REFRACTION
        glassHighlight = SettingsStore.DEFAULT_GLASS_HIGHLIGHT
        glassTint = SettingsStore.DEFAULT_GLASS_TINT
        glassNavbar = SettingsStore.DEFAULT_GLASS_NAVBAR
        glassCards = SettingsStore.DEFAULT_GLASS_CARDS
        cardGlassBlur = SettingsStore.DEFAULT_CARD_GLASS_BLUR
        cardGlassTint = SettingsStore.DEFAULT_CARD_GLASS_TINT
        cardGlassColorMode = SettingsStore.DEFAULT_CARD_GLASS_COLOR
        cardGlassCustom = SettingsStore.DEFAULT_CARD_GLASS_CUSTOM
        cardInnerShadow = SettingsStore.DEFAULT_CARD_INNER_SHADOW
        textColorMode = SettingsStore.DEFAULT_TEXT_COLOR
        textColorCustom = SettingsStore.DEFAULT_TEXT_COLOR_CUSTOM
        backgroundMode = SettingsStore.DEFAULT_BACKGROUND_MODE
        backgroundBlur = SettingsStore.DEFAULT_BACKGROUND_BLUR
        commit()
    }
}

@Composable
fun rememberSettingsState(
    preferences: SharedPreferences,
    backupPreferences: SharedPreferences,
): SettingsState =
    remember(preferences, backupPreferences) {
        SettingsState(preferences, backupPreferences)
    }

/** Lets leaf composables such as [SettingsCard] read the live settings without threading them. */
val LocalSettingsState = staticCompositionLocalOf<SettingsState?> { null }
