package com.adebojel.dynamicislandultra.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BubbleAutoDimConfigTest {
    @Test fun disabledByDefault() { assertFalse(BubbleAutoDimConfig.shouldSchedule(false, false, true)) }
    @Test fun menuIsNeverDimmed() { assertFalse(BubbleAutoDimConfig.shouldDim(true, true, true)) }
    @Test fun detachedBubbleIsNotScheduled() { assertFalse(BubbleAutoDimConfig.shouldDim(true, false, false)) }
    @Test fun enabledIdleBubbleIsDimmedAfterEightSeconds() {
        assertTrue(BubbleAutoDimConfig.shouldDim(true, false, true))
        assertEquals(8_000L, BubbleAutoDimConfig.IDLE_DELAY_MS)
    }
    @Test fun dimmedBubbleRemainsVisible() {
        assertEquals(0.18f, BubbleAutoDimConfig.targetAlpha(0.4f), 0.001f)
        assertEquals(0.28f, BubbleAutoDimConfig.targetAlpha(1f), 0.001f)
    }
}
