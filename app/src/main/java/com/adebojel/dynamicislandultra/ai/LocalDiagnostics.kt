package com.adebojel.dynamicislandultra.ai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.webkit.WebView
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Read-only app/device health checks. It never changes system settings,
 * disables services or sends device information over the network.
 */
object LocalDiagnostics {
    fun createReport(context: Context): String {
        val packageName = context.packageName
        val overlay = Settings.canDrawOverlays(context)
        val listenerSetting = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()
        val notificationAccess = listenerSetting.split(':').any {
            it.contains(packageName, ignoreCase = true)
        }
        val notificationPermission = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        val battery = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryPercent = battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val freeBytes = context.cacheDir.usableSpace
        val webViewAvailable = runCatching {
            WebView.getCurrentWebViewPackage() != null
        }.getOrDefault(false)
        val version = context.packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
        val lines = listOf(
            "Pemeriksaan lokal Dynamic Island Ultra",
            "Versi aplikasi: $version",
            "Android API: ${Build.VERSION.SDK_INT} (minimum aplikasi 26)",
            "Overlay: ${state(overlay)}",
            "Akses notifikasi: ${state(notificationAccess)}",
            "Izin notifikasi: ${state(notificationPermission)}",
            "Komponen WebView: ${state(webViewAvailable)}",
            "Baterai: ${if (batteryPercent >= 0) "$batteryPercent%" else "tidak tersedia"}",
            "Ruang cache tersedia: ${formatBytes(freeBytes)}",
            "",
            "Pemeriksaan ini hanya membaca status. Izin dapat diaktifkan dari Pengaturan Android."
        )
        return lines.joinToString("\n")
    }

    private fun state(ok: Boolean) = if (ok) "Siap" else "Belum aktif / tidak tersedia"

    private fun formatBytes(bytes: Long): String {
        val mb = bytes.coerceAtLeast(0L) / (1024.0 * 1024.0)
        return String.format(java.util.Locale.ROOT, "%.1f MiB", mb)
    }
}
