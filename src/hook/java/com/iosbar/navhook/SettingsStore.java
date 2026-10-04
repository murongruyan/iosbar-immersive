package com.iosbar.navhook;

import android.content.Context;
import android.content.SharedPreferences;

/** Shared preference contract used by both the LSPosed hook and the settings UI. */
public final class SettingsStore {
    public static final String PREFS_NAME = "settings";
    public static final String BACKUP_PREFS_NAME = "settings_backup";

    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_IMMERSIVE = "immersive";
    public static final String KEY_REMOVE_SCRIM = "remove_scrim";
    public static final String KEY_WIDTH_PORTRAIT = "width_portrait";
    public static final String KEY_WIDTH_LANDSCAPE = "width_landscape";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_BOTTOM = "bottom";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_ALPHA = "alpha";
    public static final String KEY_COLOR_MODE = "color_mode";
    public static final String KEY_CUSTOM_COLOR = "custom_color";
    public static final String KEY_REVISION = "revision";
    public static final String KEY_BACKGROUND_MODE = "background_mode";
    public static final String KEY_BACKGROUND_PATH = "background_path";
    public static final String KEY_BACKGROUND_BLUR = "background_blur";
    public static final String KEY_ACCENT = "accent";
    public static final String KEY_GLASS_BLUR = "glass_blur";
    public static final String KEY_GLASS_REFRACTION = "glass_refraction";
    public static final String KEY_GLASS_HIGHLIGHT = "glass_highlight";
    public static final String KEY_GLASS_TINT = "glass_tint";
    public static final String KEY_GLASS_NAVBAR = "glass_navbar";
    public static final String KEY_PRESETS = "presets";
    public static final String KEY_ACTIVE_PRESET = "active_preset";
    /** Liquid glass on the settings cards, independent of the bottom bar glass. */
    public static final String KEY_GLASS_CARDS = "glass_cards";
    public static final String KEY_CARD_GLASS_BLUR = "card_glass_blur";
    public static final String KEY_CARD_GLASS_TINT = "card_glass_tint";
    /** 0 = auto (contrast against the wallpaper), 1 = white, 2 = black, 3 = custom. */
    public static final String KEY_CARD_GLASS_COLOR = "card_glass_color";
    public static final String KEY_CARD_GLASS_CUSTOM = "card_glass_custom";
    /**
     * Optional inner shadow on the glass cards. Off by default: it is the one part of the
     * AndroidLiquidGlass recipe that costs a whole extra offscreen layer per card.
     */
    public static final String KEY_CARD_INNER_SHADOW = "card_inner_shadow";
    /** Bar / label colour overrides. -1 means "follow the resolved bar colour". */
    public static final String KEY_TEXT_COLOR = "text_color";
    public static final String KEY_TEXT_COLOR_CUSTOM = "text_color_custom";

    public static final boolean DEFAULT_ENABLED = true;
    public static final boolean DEFAULT_IMMERSIVE = true;
    public static final boolean DEFAULT_REMOVE_SCRIM = true;
    public static final float DEFAULT_WIDTH_PORTRAIT = 180f;
    public static final float DEFAULT_WIDTH_LANDSCAPE = 200f;
    public static final float DEFAULT_HEIGHT = 6.4f;
    public static final float DEFAULT_BOTTOM = 14f;
    public static final float DEFAULT_RADIUS = -1f;
    public static final int DEFAULT_ALPHA = 100;
    public static final int DEFAULT_COLOR_MODE = BarConfig.COLOR_AUTO;
    public static final int DEFAULT_CUSTOM_COLOR = 0xFFFFFFFF;
    public static final String DEFAULT_BACKGROUND_MODE = "gradient";
    public static final String DEFAULT_BACKGROUND_PATH = "";
    public static final int DEFAULT_BACKGROUND_BLUR = 18;
    public static final int DEFAULT_ACCENT = 0;
    public static final int DEFAULT_GLASS_BLUR = 20;
    public static final int DEFAULT_GLASS_REFRACTION = 22;
    public static final int DEFAULT_GLASS_HIGHLIGHT = 90;
    public static final int DEFAULT_GLASS_TINT = 46;
    public static final boolean DEFAULT_GLASS_NAVBAR = true;
    public static final boolean DEFAULT_GLASS_CARDS = true;
    public static final int DEFAULT_CARD_GLASS_BLUR = 18;
    public static final int DEFAULT_CARD_GLASS_TINT = 62;
    public static final int DEFAULT_CARD_GLASS_COLOR = 0;
    public static final int DEFAULT_CARD_GLASS_CUSTOM = 0xFF1C1C1E;
    public static final boolean DEFAULT_CARD_INNER_SHADOW = false;
    /** 0 = follow bar colour, 1 = white, 2 = black, 3 = custom. */
    public static final int DEFAULT_TEXT_COLOR = 0;
    public static final int DEFAULT_TEXT_COLOR_CUSTOM = 0xFFFFFFFF;

    @SuppressWarnings("deprecation")
    public static SharedPreferences get(Context context) {
        try {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
        } catch (SecurityException ignored) {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void touch(SharedPreferences preferences) {
        preferences.edit().putLong(KEY_REVISION, System.currentTimeMillis()).apply();
    }

    public static float getFloat(SharedPreferences preferences, String key, float fallback) {
        try {
            return preferences.getFloat(key, fallback);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static int getInt(SharedPreferences preferences, String key, int fallback) {
        try {
            return preferences.getInt(key, fallback);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static long getLong(SharedPreferences preferences, String key, long fallback) {
        try {
            return preferences.getLong(key, fallback);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static boolean getBoolean(SharedPreferences preferences, String key, boolean fallback) {
        try {
            return preferences.getBoolean(key, fallback);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    public static String getString(SharedPreferences preferences, String key, String fallback) {
        try {
            return preferences.getString(key, fallback);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private SettingsStore() {
    }
}


