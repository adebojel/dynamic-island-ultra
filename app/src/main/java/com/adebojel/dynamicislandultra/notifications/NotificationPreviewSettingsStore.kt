package com.adebojel.dynamicislandultra.notifications

import android.content.Context

object NotificationPreviewSettingsStore {
    private const val PREFS = "notification_privacy"

    fun load(context: Context): NotificationPreviewSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return NotificationPreviewSettings(
            enabled = prefs.getBoolean("enabled", true),
            hideContent = prefs.getBoolean("hide_content", true),
            allowlistOnly = prefs.getBoolean("allowlist_only", false),
            allowedPackages = prefs.getStringSet("allowed_packages", emptySet())?.toSet().orEmpty()
        )
    }

    fun save(context: Context, settings: NotificationPreviewSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", settings.enabled)
            .putBoolean("hide_content", settings.hideContent)
            .putBoolean("allowlist_only", settings.allowlistOnly)
            .putStringSet("allowed_packages", HashSet(settings.allowedPackages))
            .apply()
    }
}
