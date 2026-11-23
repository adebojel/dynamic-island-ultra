package com.adebojel.dynamicislandultra.overlay

data class BubbleFavoriteOption(
    val key: String,
    val label: String,
    val shortLabel: String
)

/**
 * Safe allowlist for the four bubble quick-favorite slots.
 * Every action opens an app-owned activity; it never launches shell or privileged system commands.
 */
object BubbleFavoriteConfig {
    const val SLOT_COUNT = 4

    val options = listOf(
        BubbleFavoriteOption("assistant", "Offline assistant", "AI"),
        BubbleFavoriteOption("notes", "Offline notes", "NOTES"),
        BubbleFavoriteOption("calculator", "Calculator", "CALC"),
        BubbleFavoriteOption("apps", "Installed apps", "APPS"),
        BubbleFavoriteOption("media", "Media controls", "MEDIA"),
        BubbleFavoriteOption("qr", "QR / barcode scanner", "QR"),
        BubbleFavoriteOption("documents", "Image / PDF preview", "FILES"),
        BubbleFavoriteOption("sensors", "Sensor kit", "SENSOR"),
        BubbleFavoriteOption("workspace", "Floating workspace", "PIP"),
        BubbleFavoriteOption("settings", "Island settings", "SET"),
        BubbleFavoriteOption("tools", "Smart Toolbox", "TOOLS"),
        BubbleFavoriteOption("mini_notes", "Mini floating notes", "MNOTE"),
        BubbleFavoriteOption("mini_calculator", "Mini floating calculator", "MCALC"),
        BubbleFavoriteOption("mini_timer", "Mini floating timer", "MTIME")
    )

    private val defaults = listOf("assistant", "notes", "calculator", "apps")

    fun defaultFor(slot: Int): String =
        defaults.getOrElse(slot) { defaults.first() }

    fun resolve(slot: Int, configured: String?): String {
        return options.firstOrNull { it.key == configured }?.key ?: defaultFor(slot)
    }

    fun optionFor(key: String): BubbleFavoriteOption =
        options.firstOrNull { it.key == key } ?: options.first()
}
