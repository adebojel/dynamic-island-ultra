package com.adebojel.dynamicislandultra.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class BubbleGestureConfigTest {
    @Test
    fun tapDefaultsToOpeningTheBubbleMenu() {
        assertEquals("menu", BubbleGestureConfig.defaultAction("tap"))
        assertEquals("menu", BubbleGestureConfig.resolve("tap", null))
    }

    @Test
    fun doubleTapDefaultsToOfflineAssistant() {
        assertEquals("assistant", BubbleGestureConfig.resolve("double_tap", null))
    }

    @Test
    fun swipeIsDisabledByDefaultSoDraggingRemainsAvailable() {
        listOf("swipe_up", "swipe_down", "swipe_left", "swipe_right").forEach {
            assertEquals("none", BubbleGestureConfig.resolve(it, null))
        }
    }

    @Test
    fun invalidStoredActionFallsBackToSafeDefault() {
        assertEquals("menu", BubbleGestureConfig.resolve("tap", "launch_arbitrary_command"))
        assertEquals("none", BubbleGestureConfig.resolve("swipe_up", "launch_arbitrary_command"))
    }

    @Test
    fun tapCannotBeMappedToNoAction() {
        assertEquals("menu", BubbleGestureConfig.resolve("tap", "none"))
    }

    @Test
    fun configuredSwipeActionIsAccepted() {
        assertEquals("media", BubbleGestureConfig.resolve("swipe_up", "media"))
    }
}
