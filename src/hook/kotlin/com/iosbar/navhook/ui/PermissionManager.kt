package com.iosbar.navhook.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Storage access needed before the custom background picker can read anything.
 *
 * Android 11+ has no usable runtime storage permission for an arbitrary folder, so the only way
 * to read a gallery or file-manager image is MANAGE_EXTERNAL_STORAGE (所有文件访问); older systems
 * use the media read permissions. Ported from 慕容调度's PermissionManager.
 */
object PermissionManager {

    /** True when we can already read an arbitrary user selected image. */
    fun hasImageAccess(context: Context): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Environment.isExternalStorageManager()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES,
        ) == PackageManager.PERMISSION_GRANTED
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
        else -> true
    }

    /** The setting page the user has to be sent to, or null when nothing has to be granted. */
    fun allFilesAccessIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:" + context.packageName),
        )
    }

    /** Runtime permission to request, or null when the system has none for this API level. */
    fun runtimePermission(): String? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> null
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_IMAGES
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> Manifest.permission.READ_EXTERNAL_STORAGE
        else -> null
    }

    /**
     * Scope permitting us to read whatever the user picked, for as long as we need it. Falls back
     * to the media permissions on systems that predate MANAGE_EXTERNAL_STORAGE.
     */
    fun permissionSummary(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        "所有文件访问（MANAGE_EXTERNAL_STORAGE）"
    } else {
        "读取图片（READ_EXTERNAL_STORAGE）"
    }

    fun openAllFilesAccess(activity: Activity, launcher: (Intent) -> Unit): Boolean {
        val intent = allFilesAccessIntent(activity) ?: return false
        return runCatching {
            launcher(intent)
            true
        }.getOrDefault(false)
    }
}
