package com.iosbar.navhook.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Single source of truth for the module identity shown in the UI and used by the updater. */
object ModuleInfo {
    const val HOMEPAGE = "https://github.com/murongruyan/iosbar-immersive"
    const val UPDATE_JSON_URL =
        "https://raw.githubusercontent.com/murongruyan/iosbar-immersive/master/update.json"
    const val XPosed_API = "LSPosed API 102"
    const val USER_AGENT = "iosbar-navhook"

    /**
     * 版本号从 PackageManager 读，不再硬编码：这里曾写死 0.6.0/10，发到 0.6.1 之后关于页仍显示旧版本，
     * 「检查更新」也会把新版本误判成"已是最新"。
     */
    fun versionName(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "?"

    fun versionCode(context: Context): Long = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }.getOrNull() ?: 0L
}

/** KernelSU style update manifest, hosted at the repo root as update.json. */
data class UpdateInfo(
    val version: String,
    val versionCode: Int,
    val zipUrl: String,
    val changelog: String,
    val homepage: String,
)

/** Blocking fetch, call it from Dispatchers.IO. Throws on transport or parse failure. */
internal fun fetchUpdateInfo(timeoutMillis: Int = 8000): UpdateInfo {
    val connection = (URL(ModuleInfo.UPDATE_JSON_URL).openConnection() as HttpURLConnection).apply {
        connectTimeout = timeoutMillis
        readTimeout = timeoutMillis
        requestMethod = "GET"
        instanceFollowRedirects = true
        setRequestProperty("Accept", "application/json")
        setRequestProperty("User-Agent", ModuleInfo.USER_AGENT)
    }
    return try {
        val code = connection.responseCode
        if (code !in 200..299) throw IllegalStateException("HTTP " + code)
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(body)
        UpdateInfo(
            version = json.optString("version", "?"),
            versionCode = json.optInt("versionCode", 0),
            zipUrl = json.optString("zipUrl", ""),
            changelog = json.optString("changelog", ""),
            homepage = json.optString("homepage", ModuleInfo.HOMEPAGE),
        )
    } finally {
        connection.disconnect()
    }
}

internal suspend fun checkForUpdate(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
    runCatching { fetchUpdateInfo() }
}

internal fun openUrl(context: Context, url: String) {
    if (url.isBlank()) return
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
