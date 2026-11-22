package com.adebojel.dynamicislandultra.overlay

/** Idle dimming keeps the bubble visible and interactive; it is not an overlay hide/removal feature. */
object BubbleAutoDimConfig {
    const val IDLE_DELAY_MS = 8_000L
    const val MIN_VISIBLE_ALPHA = 0.18f

    fun shouldSchedule(enabled: Boolean, expanded: Boolean, bubbleAttached: Boolean): Boolean =
        enabled && !expanded && bubbleAttached

    fun shouldDim(enabled: Boolean, expanded: Boolean, bubbleAttached: Boolean): Boolean =
        shouldSchedule(enabled, expanded, bubbleAttached)

    fun targetAlpha(baseOpacity: Float): Float {
        val safeOpacity = baseOpacity.coerceIn(0.4f, 1f)
        return (safeOpacity * 0.28f).coerceAtLeast(MIN_VISIBLE_ALPHA)
    }
}
