package com.iosbar.navhook;

import android.content.SharedPreferences;
import android.util.DisplayMetrics;

/** Immutable snapshot of user settings consumed by the SystemUI hook. */
public final class BarConfig {
    public static final int COLOR_AUTO = 0;
    public static final int COLOR_WHITE = 1;
    public static final int COLOR_BLACK = 2;
    public static final int COLOR_CUSTOM = 3;

    public final boolean enabled;
    public final boolean immersive;
    public final boolean removeScrim;
    public final float widthPortraitDp;
    public final float widthLandscapeDp;
    public final float heightDp;
    public final float bottomDp;
    public final float radiusDp;
    public final int alphaPercent;
    public final int colorMode;
    public final int customColor;

    private BarConfig(
            boolean enabled,
            boolean immersive,
            boolean removeScrim,
            float widthPortraitDp,
            float widthLandscapeDp,
            float heightDp,
            float bottomDp,
            float radiusDp,
            int alphaPercent,
            int colorMode,
            int customColor) {
        this.enabled = enabled;
        this.immersive = immersive;
        this.removeScrim = removeScrim;
        this.widthPortraitDp = widthPortraitDp;
        this.widthLandscapeDp = widthLandscapeDp;
        this.heightDp = heightDp;
        this.bottomDp = bottomDp;
        this.radiusDp = radiusDp;
        this.alphaPercent = alphaPercent;
        this.colorMode = colorMode;
        this.customColor = customColor;
    }

    public static BarConfig defaults() {
        return new BarConfig(
                SettingsStore.DEFAULT_ENABLED,
                SettingsStore.DEFAULT_IMMERSIVE,
                SettingsStore.DEFAULT_REMOVE_SCRIM,
                SettingsStore.DEFAULT_WIDTH_PORTRAIT,
                SettingsStore.DEFAULT_WIDTH_LANDSCAPE,
                SettingsStore.DEFAULT_HEIGHT,
                SettingsStore.DEFAULT_BOTTOM,
                SettingsStore.DEFAULT_RADIUS,
                SettingsStore.DEFAULT_ALPHA,
                SettingsStore.DEFAULT_COLOR_MODE,
                SettingsStore.DEFAULT_CUSTOM_COLOR);
    }

    public static BarConfig from(SharedPreferences preferences) {
        if (preferences == null) {
            return defaults();
        }
        return new BarConfig(
                SettingsStore.getBoolean(preferences, SettingsStore.KEY_ENABLED,
                        SettingsStore.DEFAULT_ENABLED),
                SettingsStore.getBoolean(preferences, SettingsStore.KEY_IMMERSIVE,
                        SettingsStore.DEFAULT_IMMERSIVE),
                SettingsStore.getBoolean(preferences, SettingsStore.KEY_REMOVE_SCRIM,
                        SettingsStore.DEFAULT_REMOVE_SCRIM),
                SettingsStore.getFloat(preferences, SettingsStore.KEY_WIDTH_PORTRAIT,
                        SettingsStore.DEFAULT_WIDTH_PORTRAIT),
                SettingsStore.getFloat(preferences, SettingsStore.KEY_WIDTH_LANDSCAPE,
                        SettingsStore.DEFAULT_WIDTH_LANDSCAPE),
                SettingsStore.getFloat(preferences, SettingsStore.KEY_HEIGHT,
                        SettingsStore.DEFAULT_HEIGHT),
                SettingsStore.getFloat(preferences, SettingsStore.KEY_BOTTOM,
                        SettingsStore.DEFAULT_BOTTOM),
                SettingsStore.getFloat(preferences, SettingsStore.KEY_RADIUS,
                        SettingsStore.DEFAULT_RADIUS),
                SettingsStore.getInt(preferences, SettingsStore.KEY_ALPHA,
                        SettingsStore.DEFAULT_ALPHA),
                SettingsStore.getInt(preferences, SettingsStore.KEY_COLOR_MODE,
                        SettingsStore.DEFAULT_COLOR_MODE),
                SettingsStore.getInt(preferences, SettingsStore.KEY_CUSTOM_COLOR,
                        SettingsStore.DEFAULT_CUSTOM_COLOR));
    }

    public boolean hasGeometryOverride() {
        return widthPortraitDp > 0f
                || widthLandscapeDp > 0f
                || heightDp > 0f
                || bottomDp >= 0f
                || radiusDp >= -1f;
    }

    public boolean hasAppearanceOverride() {
        return alphaPercent < 100 || colorMode != COLOR_AUTO;
    }

    public boolean drawCustom() {
        return enabled && (hasGeometryOverride() || hasAppearanceOverride());
    }

    public int alpha() {
        return Math.max(0, Math.min(255, Math.round(alphaPercent * 255f / 100f)));
    }

    public static int dpToPx(float dp, DisplayMetrics metrics) {
        float density = metrics == null ? 1f : metrics.density;
        return Math.max(1, Math.round(dp * density));
    }
}
