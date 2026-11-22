package com.adebojel.dynamicislandultra.overlay

data class BubbleGestureOption(
    val key: String,
    val label: String
)

/**
 * Shared allowlist and defaults for user-configurable floating bubble gestures.
 * This is deliberately a small local action router; it cannot execute privileged
 * Android operations or arbitrary commands.
 */
object BubbleGestureConfig {
    val options = listOf(
        BubbleGestureOption("none", "Tidak ada tindakan"),
        BubbleGestureOption("menu", "Buka menu bubble"),
        BubbleGestureOption("assistant", "Buka asisten offline"),
        BubbleGestureOption("media", "Kontrol media"),
        BubbleGestureOption("notifications", "Pengaturan notifikasi"),
        BubbleGestureOption("workspace", "Floating workspace"),
        BubbleGestureOption("settings", "Pengaturan bubble")
    )

    private val defaults = mapOf(
        "tap" to "menu",
        "double_tap" to "assistant",
        "long_press" to "menu",
        "swipe_up" to "none",
        "swipe_down" to "none",
        "swipe_left" to "none",
        "swipe_right" to "none"
    )

    fun defaultAction(gesture: String): String = defaults[gesture] ?: "none"

    fun optionsFor(gesture: String): List<BubbleGestureOption> =
        if (gesture == "tap") options.filterNot { it.key == "none" } else options

    fun resolve(gesture: String, configured: String?): String {
        val allowed = optionsFor(gesture).any { it.key == configured }
        return if (allowed) requireNotNull(configured) else defaultAction(gesture)
    }

    fun labelFor(action: String): String =
        options.firstOrNull { it.key == action }?.label ?: "Tidak ada tindakan"
}
