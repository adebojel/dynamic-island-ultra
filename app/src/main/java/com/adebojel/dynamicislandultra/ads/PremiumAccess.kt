package com.adebojel.dynamicislandultra.ads

import android.content.Context
import android.graphics.Color

object PremiumFeatureCatalog {
    data class Feature(val id: String, val title: String, val description: String)

    val features = listOf(
        Feature("rgb_theme", "Advanced RGB Themes", "Aksen Cyan, Violet, Emerald, dan Amber"),
        Feature("island_fx", "Island Pulse FX", "Animasi Soft, Normal, Fast, atau Off"),
        Feature("island_size", "Island Size", "Ukuran pill Small, Normal, atau Large"),
        Feature("panel_layout", "Panel Layout", "Tata letak panel Compact atau Comfortable")
    )
}

object PremiumAccess {
    const val PREFS = "premium_access"
    const val KEY_REWARDED_UNTIL = "rewarded_until"
    const val KEY_PLAY_ACTIVE = "play_subscription_active"
    const val KEY_ACCENT = "premium_accent"
    const val KEY_PULSE = "premium_pulse"
    const val KEY_SCALE = "premium_scale"
    const val KEY_COMPACT_PANEL = "premium_compact_panel"

    fun grantRewardedAccess(context: Context, durationHours: Int = 24) {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val expiry = maxOf(now, preferences.getLong(KEY_REWARDED_UNTIL, 0L)) +
            durationHours * 60L * 60L * 1000L
        preferences.edit().putLong(KEY_REWARDED_UNTIL, expiry).apply()
    }

    fun rewardedExpiry(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_REWARDED_UNTIL, 0L)

    fun hasPremiumAccess(context: Context): Boolean {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return preferences.getBoolean(KEY_PLAY_ACTIVE, false) ||
            preferences.getLong(KEY_REWARDED_UNTIL, 0L) > System.currentTimeMillis()
    }

    fun setPlaySubscriptionActive(context: Context, active: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_PLAY_ACTIVE, active).apply()
    }

    fun accentColor(context: Context): Int {
        if (!hasPremiumAccess(context)) return Color.rgb(67, 232, 255)
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_ACCENT, Color.rgb(67, 232, 255))
        return when (stored) {
            Color.rgb(179, 107, 255) -> Color.rgb(179, 107, 255)
            Color.rgb(65, 220, 145) -> Color.rgb(65, 220, 145)
            Color.rgb(255, 200, 87) -> Color.rgb(255, 200, 87)
            else -> Color.rgb(67, 232, 255)
        }
    }

    fun saveAccent(context: Context, color: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_ACCENT, color).apply()
    }

    fun pulseStyle(context: Context): String {
        if (!hasPremiumAccess(context)) return "standard"
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PULSE, "standard") ?: "standard"
    }

    fun savePulseStyle(context: Context, style: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PULSE, style).apply()
    }

    fun islandScale(context: Context): Float {
        if (!hasPremiumAccess(context)) return 1f
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY_SCALE, 1f).coerceIn(0.85f, 1.2f)
    }

    fun saveIslandScale(context: Context, scale: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_SCALE, scale.coerceIn(0.85f, 1.2f)).apply()
    }

    fun compactPanel(context: Context): Boolean {
        return hasPremiumAccess(context) &&
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_COMPACT_PANEL, false)
    }

    fun saveCompactPanel(context: Context, compact: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_COMPACT_PANEL, compact).apply()
    }
}
