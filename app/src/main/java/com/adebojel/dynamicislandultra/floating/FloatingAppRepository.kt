package com.adebojel.dynamicislandultra.floating

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.app.ActivityOptions
import android.graphics.Rect

data class LaunchableApp(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

/** Uses the narrow launcher intent query declared in the manifest; no QUERY_ALL_PACKAGES. */
class FloatingAppRepository(private val context: Context) {
    fun loadLaunchableApps(): List<LaunchableApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val entries = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return entries.mapNotNull { entry ->
            val activity = entry.activityInfo ?: return@mapNotNull null
            if (activity.packageName == context.packageName) return@mapNotNull null
            runCatching {
                LaunchableApp(
                    label = entry.loadLabel(pm).toString(),
                    packageName = activity.packageName,
                    icon = entry.loadIcon(pm)
                )
            }.getOrNull()
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    /**
     * Requests Android's native freeform launch bounds. The OS/OEM may ignore them
     * when desktop/freeform multi-window is unavailable; this never embeds another app.
     */
    fun launchInFreeform(packageName: String): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        val metrics = context.resources.displayMetrics
        val left = (metrics.widthPixels * 0.08f).toInt()
        val top = (metrics.heightPixels * 0.12f).toInt()
        val right = (metrics.widthPixels * 0.88f).toInt()
        val bottom = (metrics.heightPixels * 0.72f).toInt()
        val bounds = Rect(left, top, right.coerceAtLeast(left + 1), bottom.coerceAtLeast(top + 1))
        return runCatching {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(
                launch,
                ActivityOptions.makeBasic().setLaunchBounds(bounds).toBundle()
            )
            true
        }.getOrDefault(false)
    }

    fun launch(packageName: String): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        return runCatching {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
            true
        }.getOrDefault(false)
    }
}
