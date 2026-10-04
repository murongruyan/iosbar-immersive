package com.iosbar.navhook;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

/**
 * ColorOS 16/17 SystemUI hook.
 *
 * <p>ColorOS 17 made the OEM handle geometry fields final. This implementation deliberately
 * avoids mutating those fields: it intercepts the handle draw pass and renders the same
 * rounded rectangle with the user's dimensions and color. Width is applied through the
 * vendor getGestureWidthRes(String) method so the navigation bar layout remains correct.</p>
 */
public final class IosBarHook extends XposedModule {
    private static final String TAG = "IosBarHook";
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String NAVIGATION_BAR =
            "com.android.systemui.navigationbar.views.NavigationBar";
    private static final String OPLUS_HANDLE =
            "com.oplus.systemui.navigationbar.gesture.sidegesture.OplusNavigationHandle";
    private static final String NAVIGATION_TRANSITIONS =
            "com.android.systemui.navigationbar.views.NavigationBarTransitions";
    private static final String PORTRAIT_WIDTH_RES =
            "navigation_gesture_view_width";
    private static final String LANDSCAPE_WIDTH_RES =
            "navigation_gesture_view_landscape_width";
    private static final int NAVIGATION_BARS = WindowInsets.Type.navigationBars();

    private static final Set<View> handleViews =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<View, Boolean>()));
    private static volatile boolean installed;
    private static volatile boolean transitionsInstalled;
    private static volatile boolean handleHooksInstalled;
    private static volatile boolean diagnosticsLogged;
    private static volatile boolean insetLogLogged;
    private static volatile boolean scrimLogLogged;
    private static volatile boolean landscapeLayoutLogged;

    private volatile SharedPreferences remotePreferences;
    private volatile BarConfig config = BarConfig.defaults();
    private volatile long configRevision = Long.MIN_VALUE;
    private volatile boolean remotePrefsWarned;

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(Log.INFO, "loaded process=" + param.getProcessName());
        ensureRemotePreferences();
        reloadConfig(true);
    }

    @Override
    public void onPackageReady(PackageReadyParam param) {
        log(Log.INFO, "package-ready package=" + param.getPackageName()
                + " first=" + param.isFirstPackage());
        if (!param.isFirstPackage() || !SYSTEM_UI.equals(param.getPackageName())) {
            return;
        }
        try {
            reloadConfig(true);
            install(param.getClassLoader());
        } catch (Throwable error) {
            log(Log.ERROR, "SystemUI hook initialization failed", error);
        }
    }

    private void install(ClassLoader loader) throws Throwable {
        if (installed) {
            return;
        }
        installNavigationBarHook(loader);
        installNavigationTransitionsHook(loader);
        installOplusHandleHooks(loader);
        installed = true;
        log(Log.INFO, "installed SystemUI hook set for ColorOS 16/17");
    }

    private void installNavigationBarHook(ClassLoader loader) throws Throwable {
        Class<?> owner = Class.forName(NAVIGATION_BAR, false, loader);
        int count = 0;
        for (Method method : owner.getDeclaredMethods()) {
            if (!"getBarLayoutParamsForRotation".equals(method.getName())
                    || method.getParameterCount() != 2
                    || !"android.view.WindowManager$LayoutParams".equals(
                    method.getReturnType().getName())) {
                continue;
            }
            method.setAccessible(true);
            hook(method)
                    .setId("navigation.insets.zero")
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object result = chain.proceed();
                        BarConfig snapshot = config;
                        if (snapshot.enabled) {
                            normalizeLandscapeLayoutParams(result, readRotation(chain));
                            if (snapshot.immersive) {
                                normalizeProvidedInsets(result);
                            }
                        }
                        return result;
                    });
            count++;
        }
        if (count == 0) {
            throw new NoSuchMethodException(NAVIGATION_BAR + ".getBarLayoutParamsForRotation");
        }
        log(Log.INFO, "installed NavigationBar inset hook methods=" + count);
    }

    /**
     * The OEM landscape bar uses a full-screen buffer while transient bars are shown. Keep the
     * handle, but remove only the scrim selected by MODE_SEMI_TRANSPARENT.
     */
    private void installNavigationTransitionsHook(ClassLoader loader) {
        if (transitionsInstalled) {
            return;
        }
        try {
            Class<?> owner = Class.forName(NAVIGATION_TRANSITIONS, false, loader);
            int count = 0;
            for (Method method : owner.getDeclaredMethods()) {
                if (!"getBarBackground".equals(method.getName())
                        || method.getParameterCount() != 3
                        || !android.graphics.drawable.Drawable.class.isAssignableFrom(
                        method.getReturnType())) {
                    continue;
                }
                method.setAccessible(true);
                hook(method)
                        .setId("navigation.transient.scrim")
                        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                        .intercept(chain -> {
                            Object result = chain.proceed();
                            BarConfig snapshot = config;
                            if (snapshot.enabled && snapshot.removeScrim && clearSemiTransparent(result)) {
                                if (!scrimLogLogged) {
                                    scrimLogLogged = true;
                                    log(Log.INFO, "transient navigation scrim disabled");
                                }
                            }
                            return result;
                        });
                count++;
            }
            if (count > 0) {
                transitionsInstalled = true;
                log(Log.INFO, "installed NavigationBarTransitions background hook methods=" + count);
            } else {
                log(Log.WARN, "NavigationBarTransitions.getBarBackground not found");
            }
        } catch (Throwable error) {
            log(Log.WARN, "NavigationBarTransitions hook unavailable", error);
        }
    }

    private void installOplusHandleHooks(ClassLoader loader) {
        if (handleHooksInstalled) {
            return;
        }
        try {
            Class<?> handle = Class.forName(OPLUS_HANDLE, false, loader);
            installHandleConstructorHook(handle);
            installHandleWidthHook(handle);
            installHandleDrawHook(handle);
            installHandleAttachHook(handle);
            handleHooksInstalled = true;
            log(Log.INFO, "installed OplusNavigationHandle hooks");
        } catch (Throwable error) {
            log(Log.WARN, "OplusNavigationHandle hook unavailable", error);
        }
    }

    private void installHandleConstructorHook(Class<?> handle) throws Throwable {
        Constructor<?> constructor = handle.getDeclaredConstructor(Context.class, android.util.AttributeSet.class);
        constructor.setAccessible(true);
        hook(constructor)
                .setId("navigation.handle.constructor")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object result = chain.proceed();
                    if (result instanceof View) {
                        registerHandle((View) result);
                    }
                    return result;
                });
    }

    private void installHandleWidthHook(Class<?> handle) throws Throwable {
        Method widthMethod = handle.getDeclaredMethod("getGestureWidthRes", String.class);
        widthMethod.setAccessible(true);
        hook(widthMethod)
                .setId("navigation.handle.width")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object receiver = chain.getThisObject();
                    Object argument = chain.getArg(0);
                    BarConfig snapshot = config;
                    if (snapshot.enabled && receiver instanceof View && argument instanceof String) {
                        String resourceName = (String) argument;
                        float widthDp = LANDSCAPE_WIDTH_RES.equals(resourceName)
                                ? snapshot.widthLandscapeDp
                                : snapshot.widthPortraitDp;
                        if (widthDp > 0f) {
                            View view = (View) receiver;
                            return BarConfig.dpToPx(widthDp, view.getResources().getDisplayMetrics());
                        }
                    }
                    return chain.proceed();
                });
    }

    private void installHandleDrawHook(Class<?> handle) throws Throwable {
        Method onDraw = handle.getDeclaredMethod("onDraw", Canvas.class);
        onDraw.setAccessible(true);
        hook(onDraw)
                .setId("navigation.handle.draw")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object receiver = chain.getThisObject();
                    Object canvasArgument = chain.getArg(0);
                    if (!(receiver instanceof View) || !(canvasArgument instanceof Canvas)) {
                        return chain.proceed();
                    }
                    View view = (View) receiver;
                    registerHandle(view);
                    BarConfig snapshot = config;
                    if (!snapshot.enabled) {
                        return chain.proceed();
                    }
                    if (drawCustomHandle(view, (Canvas) canvasArgument, snapshot)) {
                        return null;
                    }
                    return chain.proceed();
                });
    }

    private void installHandleAttachHook(Class<?> handle) throws Throwable {
        Method attached = handle.getDeclaredMethod("onAttachedToWindow");
        attached.setAccessible(true);
        hook(attached)
                .setId("navigation.handle.attach")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object result = chain.proceed();
                    if (chain.getThisObject() instanceof View) {
                        registerHandle((View) chain.getThisObject());
                    }
                    return result;
                });
    }

    private boolean drawCustomHandle(View view, Canvas canvas, BarConfig snapshot) {
        try {
            Paint paint = readPaint(view);
            if (paint == null) {
                return false;
            }
            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();
            if (viewWidth <= 0 || viewHeight <= 0) {
                return false;
            }

            boolean landscape = view.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE;
            float density = view.getResources().getDisplayMetrics().density;
            float height = snapshot.heightDp > 0f
                    ? snapshot.heightDp * density
                    : readIntField(view, "mHeight", Math.round(4f * density));
            float bottom = snapshot.bottomDp >= 0f
                    ? snapshot.bottomDp * density
                    : readIntField(view, "mHandleBottom", Math.round(7f * density));
            float radius = snapshot.radiusDp >= 0f
                    ? snapshot.radiusDp * density
                    : Math.max(1f, height / 2f);

            float extraHeight = readFloatField(view, "mAdditionalHeightForAnimation", 0f)
                    * readFloatField(view, "longPressAnimProgress", 0f);
            float currentHeight = Math.max(1f, height + extraHeight);
            if (bottom + currentHeight > viewHeight) {
                bottom = Math.max(0f, viewHeight - currentHeight);
            }
            float bottomY = viewHeight - bottom;
            float topY = bottomY - currentHeight;
            float radiusScale = currentHeight / Math.max(1f, height);
            float cornerRadius = Math.max(1f, radius * radiusScale);

            float widthDp = landscape ? snapshot.widthLandscapeDp : snapshot.widthPortraitDp;
            float requestedWidth = widthDp > 0f ? widthDp * density : viewWidth;
            float handleWidth = Math.max(1f, Math.min(viewWidth, requestedWidth));
            float left = (viewWidth - handleWidth) / 2f;
            float right = left + handleWidth;

            int originalColor = paint.getColor();
            paint.setColor(resolveColor(snapshot, originalColor));
            canvas.save();
            try {
                canvas.clipRect(left, topY, right, bottomY);
                canvas.drawRoundRect(left, topY, right, bottomY, cornerRadius, cornerRadius, paint);
            } finally {
                paint.setColor(originalColor);
                canvas.restore();
            }

            if (!diagnosticsLogged) {
                diagnosticsLogged = true;
                log(Log.INFO, "custom handle draw width=" + handleWidth
                        + " height=" + currentHeight
                        + " bottom=" + bottom
                        + " radius=" + cornerRadius
                        + " alpha=" + snapshot.alphaPercent
                        + " colorMode=" + snapshot.colorMode);
            }
            return true;
        } catch (Throwable error) {
            log(Log.WARN, "custom handle draw failed", error);
            return false;
        }
    }

    private static int resolveColor(BarConfig snapshot, int originalColor) {
        int baseColor;
        switch (snapshot.colorMode) {
            case BarConfig.COLOR_WHITE:
                baseColor = Color.WHITE;
                break;
            case BarConfig.COLOR_BLACK:
                baseColor = Color.BLACK;
                break;
            case BarConfig.COLOR_CUSTOM:
                baseColor = snapshot.customColor;
                break;
            case BarConfig.COLOR_AUTO:
            default:
                baseColor = originalColor;
                break;
        }
        int baseAlpha = Color.alpha(baseColor);
        int finalAlpha = Math.round(snapshot.alpha() * (baseAlpha / 255f));
        return Color.argb(finalAlpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
    }

    private void registerHandle(View view) {
        handleViews.add(view);
    }

    private void normalizeLandscapeLayoutParams(Object layoutParams, int rotation) {
        if (layoutParams == null || (rotation != 1 && rotation != 3)) {
            return;
        }
        boolean widthChanged = setIntField(layoutParams, "width", -1);
        boolean heightChanged = setIntField(layoutParams, "height", -2);
        boolean gravityChanged = setIntField(layoutParams, "gravity",
                android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
        if ((widthChanged || heightChanged || gravityChanged) && !landscapeLayoutLogged) {
            landscapeLayoutLogged = true;
            log(Log.INFO, "landscape NavigationBar window normalized to bottom-center"
                    + " width=-1 height=wrap_content rotation=" + rotation);
        }
    }

    private static int readRotation(XposedInterface.Chain chain) {
        try {
            Object value = chain.getArg(0);
            return value instanceof Number ? ((Number) value).intValue() : -1;
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static boolean clearSemiTransparent(Object drawable) {
        if (drawable == null) {
            return false;
        }
        try {
            Field field = findField(drawable.getClass(), "mSemiTransparent");
            if (field == null || field.getType() != Integer.TYPE) {
                return false;
            }
            field.setAccessible(true);
            int before = field.getInt(drawable);
            if (before == 0) {
                return false;
            }
            field.setInt(drawable, 0);
            return field.getInt(drawable) == 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Paint readPaint(Object receiver) {
        try {
            Field field = findField(receiver.getClass(), "mPaint");
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object value = field.get(receiver);
            return value instanceof Paint ? (Paint) value : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int readIntField(Object receiver, String name, int fallback) {
        try {
            Field field = findField(receiver.getClass(), name);
            if (field == null) {
                return fallback;
            }
            field.setAccessible(true);
            return field.getInt(receiver);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static float readFloatField(Object receiver, String name, float fallback) {
        try {
            Field field = findField(receiver.getClass(), name);
            if (field == null) {
                return fallback;
            }
            field.setAccessible(true);
            return field.getFloat(receiver);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static boolean setIntField(Object receiver, String name, int value) {
        try {
            Field field = findField(receiver.getClass(), name);
            if (field == null) {
                return false;
            }
            field.setAccessible(true);
            if (field.getInt(receiver) == value) {
                return false;
            }
            field.setInt(receiver, value);
            return field.getInt(receiver) == value;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void normalizeProvidedInsets(Object layoutParams) throws Throwable {
        if (layoutParams == null) {
            return;
        }
        Field provided = findField(layoutParams.getClass(), "providedInsets");
        if (provided == null) {
            return;
        }
        provided.setAccessible(true);
        Object providers = provided.get(layoutParams);
        if (providers == null || !providers.getClass().isArray()) {
            return;
        }
        int changed = 0;
        int length = Array.getLength(providers);
        for (int i = 0; i < length; i++) {
            Object provider = Array.get(providers, i);
            if (provider == null || !isNavigationProvider(provider)) {
                continue;
            }
            Method setter = findInsetsSetter(provider.getClass());
            if (setter == null) {
                continue;
            }
            setter.setAccessible(true);
            setter.invoke(provider, Insets.of(0, 0, 0, 0));
            changed++;
        }
        if (changed > 0 && !insetLogLogged) {
            insetLogLogged = true;
            log(Log.INFO, "navigationBars provider inset set to zero; gesture providers preserved");
        }
    }

    private static boolean isNavigationProvider(Object provider) {
        try {
            Method getType = findNoArgMethod(provider.getClass(), "getType");
            if (getType != null) {
                getType.setAccessible(true);
                Object value = getType.invoke(provider);
                return value instanceof Number
                        && ((((Number) value).intValue() & NAVIGATION_BARS) != 0);
            }
            Field type = findField(provider.getClass(), "mType");
            if (type != null) {
                type.setAccessible(true);
                Object value = type.get(provider);
                return value instanceof Number
                        && ((((Number) value).intValue() & NAVIGATION_BARS) != 0);
            }
        } catch (Throwable ignored) {
            // Fail closed for this provider if the hidden framework shape changed.
        }
        return false;
    }

    private static Method findInsetsSetter(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if ("setInsetsSize".equals(method.getName())
                        && method.getParameterCount() == 1
                        && Insets.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    return method;
                }
            }
        }
        return null;
    }

    private static Method findNoArgMethod(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (name.equals(method.getName()) && method.getParameterCount() == 0) {
                    return method;
                }
            }
        }
        return null;
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Continue through the hidden framework hierarchy.
            }
        }
        return null;
    }

    private void ensureRemotePreferences() {
        try {
            remotePreferences = getRemotePreferences(SettingsStore.PREFS_NAME);
        } catch (Throwable error) {
            if (!remotePrefsWarned) {
                remotePrefsWarned = true;
                log(Log.WARN, "remote preferences unavailable; using defaults", error);
            }
        }
    }



    private void reloadConfig(boolean force) {
        ensureRemotePreferences();
        try {
            SharedPreferences preferences = remotePreferences;
            if (preferences == null) {
                if (force) {
                    config = BarConfig.defaults();
                    configRevision = Long.MIN_VALUE;
                }
                return;
            }
            long revision = SettingsStore.getLong(preferences, SettingsStore.KEY_REVISION, -1L);
            BarConfig oldConfig = config;
            BarConfig newConfig = BarConfig.from(preferences);
            config = newConfig;
            configRevision = revision;
            if (force || oldConfig.enabled != newConfig.enabled
                    || oldConfig.immersive != newConfig.immersive
                    || oldConfig.removeScrim != newConfig.removeScrim
                    || oldConfig.heightDp != newConfig.heightDp
                    || oldConfig.bottomDp != newConfig.bottomDp
                    || oldConfig.radiusDp != newConfig.radiusDp
                    || oldConfig.widthPortraitDp != newConfig.widthPortraitDp
                    || oldConfig.widthLandscapeDp != newConfig.widthLandscapeDp
                    || oldConfig.alphaPercent != newConfig.alphaPercent
                    || oldConfig.colorMode != newConfig.colorMode
                    || oldConfig.customColor != newConfig.customColor) {
                log(Log.INFO, "config revision=" + revision
                        + " enabled=" + newConfig.enabled
                        + " immersive=" + newConfig.immersive
                        + " removeScrim=" + newConfig.removeScrim
                        + " width=" + newConfig.widthPortraitDp + "/" + newConfig.widthLandscapeDp
                        + " height=" + newConfig.heightDp
                        + " bottom=" + newConfig.bottomDp
                        + " radius=" + newConfig.radiusDp
                        + " alpha=" + newConfig.alphaPercent
                        + " colorMode=" + newConfig.colorMode);
            }
        } catch (Throwable error) {
            log(Log.WARN, "failed to reload settings", error);
        }
    }



    private void log(int priority, String message) {
        log(priority, TAG, message);
    }

    private void log(int priority, String message, Throwable error) {
        log(priority, TAG, message, error);
    }
}
