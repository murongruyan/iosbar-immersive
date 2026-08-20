package com.iosbar.navhook;

import android.graphics.Insets;
import android.util.Log;
import android.view.WindowInsets;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

/**
 * Removes only the layout/navigation-bar inset from SystemUI's bar providers,
 * then adjusts the OEM handle geometry while preserving all gesture providers.
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
    private static final float HANDLE_HEIGHT_DP = 6.4f;
    private static final float HANDLE_BOTTOM_DP = 14.0f;
    private static final float HANDLE_WIDTH_DP = 180.0f;
    private static final int NAVIGATION_BARS = WindowInsets.Type.navigationBars();
    private static final Set<Object> geometryApplied =
            Collections.newSetFromMap(new WeakHashMap<>());
    private static volatile boolean installed;
    private static volatile boolean handleDiagnosticsInstalled;
    private static volatile boolean handleLifecycleInstalled;
    private static volatile boolean transitionsInstalled;
    private static volatile boolean landscapeLayoutLogged;
    private static volatile boolean logged;

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(Log.INFO, "loaded process=" + param.getProcessName());
    }

    @Override
    public void onPackageReady(PackageReadyParam param) {
        log(Log.INFO, "package-ready package=" + param.getPackageName()
                + " first=" + param.isFirstPackage());
        if (!param.isFirstPackage() || !SYSTEM_UI.equals(param.getPackageName())) {
            return;
        }
        try {
            install(param.getClassLoader());
        } catch (Throwable error) {
            log(Log.ERROR, "SystemUI hook initialization failed", error);
        }
    }

    private void install(ClassLoader loader) throws Throwable {
        if (installed) {
            return;
        }
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
                        normalizeLandscapeLayoutParams(result, readRotation(chain), chain.getThisObject());
                        normalizeProvidedInsets(result);
                        return result;
                    });
            count++;
        }
        if (count == 0) {
            throw new NoSuchMethodException(NAVIGATION_BAR + ".getBarLayoutParamsForRotation");
        }
        installNavigationTransitions(loader);
        installHandleDiagnostics(loader);
        installed = true;
        log(Log.INFO, "installed NavigationBar inset hook methods=" + count);
    }

    /**
     * The OEM landscape bar uses a full-screen buffer while transient bars are
     * shown. Keep the handle, but remove only the scrim selected by
     * MODE_SEMI_TRANSPARENT. This avoids replacing framework resources or the
     * navigation-mode overlay, both of which can invalidate resource mappings.
     */
    private void installNavigationTransitions(ClassLoader loader) {
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
                            if (clearSemiTransparent(result)) {
                                log(Log.INFO, "transient navigation scrim disabled");
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

    private void normalizeLandscapeLayoutParams(Object layoutParams, int rotation, Object owner) {
        if (layoutParams == null || (rotation != 1 && rotation != 3)) {
            return;
        }
        boolean widthChanged = setIntField(layoutParams, "width", -1);
        // Let the handle view determine its own height. A fixed landscape frame
        // leaves a large transparent navigation window around the visible bar.
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

    private void installHandleDiagnostics(ClassLoader loader) {
        if (handleDiagnosticsInstalled) {
            return;
        }
        try {
            Class<?> handle = Class.forName(OPLUS_HANDLE, false, loader);
            Method onDraw = handle.getDeclaredMethod("onDraw", android.graphics.Canvas.class);
            onDraw.setAccessible(true);
            hook(onDraw)
                    .setId("navigation.handle.geometry")
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object receiver = chain.getThisObject();
                        int viewHeight = readViewHeight(receiver);
                        int viewWidth = readViewWidth(receiver);
                        if (markGeometryApplied(receiver)) {
                            applyHandleGeometry(receiver);
                        }
                        if (!handleDiagnosticsInstalled) {
                            handleDiagnosticsInstalled = true;
                            log(Log.INFO, "OplusNavigationHandle geometry "
                                    + "height=" + readIntField(receiver, "mHeight")
                                    + " bottom=" + readIntField(receiver, "mHandleBottom")
                                    + " radius=" + readIntField(receiver, "mRadius")
                                    + " viewHeight=" + viewHeight
                                    + " viewWidth=" + viewWidth
                                    + " resources=" + readHandleResources(receiver));
                        }
                        return chain.proceed();
                    });
            installHandleLifecycleHooks(handle);
        } catch (Throwable error) {
            log(Log.WARN, "OplusNavigationHandle diagnostics unavailable", error);
        }
    }

    /**
     * The vendor view recalculates its dimensions after attachment, rotation, and orientation
     * changes. Re-apply only after those lifecycle methods return so the OEM draw path stays intact.
     */
    private void installHandleLifecycleHooks(Class<?> handle) {
        if (handleLifecycleInstalled) {
            return;
        }
        int count = 0;
        for (Method method : handle.getDeclaredMethods()) {
            String name = method.getName();
            if (!("onAttachedToWindow".equals(name)
                    || "onLayout".equals(name)
                    || "setVertical".equals(name))) {
                continue;
            }
            method.setAccessible(true);
            hook(method)
                    .setId("navigation.handle.geometry.lifecycle")
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object result = chain.proceed();
                        applyHandleGeometry(chain.getThisObject());
                        return result;
                    });
            count++;
        }
        handleLifecycleInstalled = true;
        log(Log.INFO, "installed OplusNavigationHandle lifecycle hooks=" + count);
    }

    private static boolean markGeometryApplied(Object receiver) {
        synchronized (geometryApplied) {
            return geometryApplied.add(receiver);
        }
    }

    private void applyHandleGeometry(Object receiver) {
        try {
            android.view.View view = (android.view.View) receiver;
            float density = view.getResources().getDisplayMetrics().density;
            int height = Math.max(1, Math.round(HANDLE_HEIGHT_DP * density));
            int bottom = Math.max(1, Math.round(HANDLE_BOTTOM_DP * density));
            int width = Math.max(1, Math.round(HANDLE_WIDTH_DP * density));
            int viewHeight = view.getHeight();
            if (viewHeight > 0) {
                height = Math.min(height, Math.max(1, viewHeight - bottom));
            }
            int radius = Math.max(1, height / 2);
            boolean changed = setIntField(receiver, "mHeight", height);
            changed |= setIntField(receiver, "mHandleBottom", bottom);
            changed |= setIntField(receiver, "mRadius", radius);
            changed |= setIntField(receiver, "mPortraitWidth", width);
            changed |= setIntField(receiver, "mLandscapeWidth", width);
            if (changed) {
                log(Log.INFO, "applied handle geometry width=" + width
                        + " height=" + height + " bottom=" + bottom + " radius=" + radius
                        + " density=" + density);
            }
        } catch (Throwable error) {
            log(Log.WARN, "portrait handle geometry apply failed", error);
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

    private static int readIntField(Object receiver, String name) {
        try {
            Field field = findField(receiver.getClass(), name);
            if (field == null) {
                return Integer.MIN_VALUE;
            }
            field.setAccessible(true);
            return field.getInt(receiver);
        } catch (Throwable ignored) {
            return Integer.MIN_VALUE;
        }
    }

    private static int readViewHeight(Object receiver) {
        try {
            Method method = findNoArgMethod(receiver.getClass(), "getHeight");
            if (method != null) {
                method.setAccessible(true);
                return ((Number) method.invoke(receiver)).intValue();
            }
        } catch (Throwable ignored) {
        }
        return Integer.MIN_VALUE;
    }

    private static int readViewWidth(Object receiver) {
        try {
            Method method = findNoArgMethod(receiver.getClass(), "getWidth");
            if (method != null) {
                method.setAccessible(true);
                return ((Number) method.invoke(receiver)).intValue();
            }
        } catch (Throwable ignored) {
        }
        return Integer.MIN_VALUE;
    }

    private static String readHandleResources(Object receiver) {
        try {
            android.view.View view = (android.view.View) receiver;
            android.content.res.Resources resources = view.getResources();
            String paths = "unknown";
            try {
                Method getApkPaths = resources.getAssets().getClass().getDeclaredMethod("getApkPaths");
                getApkPaths.setAccessible(true);
                Object value = getApkPaths.invoke(resources.getAssets());
                paths = value instanceof String[] ? Arrays.toString((String[]) value) : String.valueOf(value);
            } catch (Throwable ignored) {
            }
            return "height=" + resources.getDimension(0x7f0712ce)
                    + ",bottom=" + resources.getDimension(0x7f0712cb)
                    + ",radius=" + resources.getDimension(0x7f0712d0)
                    + ",width=" + resources.getDimension(0x7f0712d8)
                    + ",apkPaths=" + paths;
        } catch (Throwable error) {
            return "unavailable";
        }
    }

    private void normalizeProvidedInsets(Object layoutParams) throws Throwable {
        if (layoutParams == null) {
            return;
        }
        Field provided = findField(layoutParams.getClass(), "providedInsets");
        if (provided == null) {
            if (!logged) {
                logged = true;
                log(Log.WARN, "LayoutParams.providedInsets is unavailable");
            }
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
        if (changed > 0 && !logged) {
            logged = true;
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
            // A hidden framework shape change should fail closed for this provider.
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

    private void log(int priority, String message) {
        log(priority, TAG, message);
    }

    private void log(int priority, String message, Throwable error) {
        log(priority, TAG, message, error);
    }
}
