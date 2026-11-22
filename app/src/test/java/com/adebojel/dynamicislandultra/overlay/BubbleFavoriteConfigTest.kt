package com.adebojel.dynamicislandultra.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BubbleFavoriteConfigTest {
    @Test
    fun defaultSlotsExposeUsefulOfflineToolsAndAppLauncher() {
        assertEquals("assistant", BubbleFavoriteConfig.resolve(0, null))
        assertEquals("notes", BubbleFavoriteConfig.resolve(1, null))
        assertEquals("calculator", BubbleFavoriteConfig.resolve(2, null))
        assertEquals("apps", BubbleFavoriteConfig.resolve(3, null))
    }

    @Test
    fun configuredFavoriteMustBeInTheAllowlist() {
        assertEquals("media", BubbleFavoriteConfig.resolve(0, "media"))
        assertEquals("assistant", BubbleFavoriteConfig.resolve(0, "launch_shell"))
    }

    @Test
    fun outOfRangeSlotsFallBackToFirstDefault() {
        assertEquals("assistant", BubbleFavoriteConfig.resolve(99, null))
    }

    @Test
    fun everyFavoriteHasAnAccessibleLabelAndShortLabel() {
        BubbleFavoriteConfig.options.forEach {
            assertTrue(it.label.isNotBlank())
            assertTrue(it.shortLabel.isNotBlank())
        }
    }

    @Test
    fun configuredOptionResolvesToTheSameCatalogEntry() {
        val selected = BubbleFavoriteConfig.optionFor("sensors")
        assertEquals("Sensor kit", selected.label)
        assertEquals("SENSOR", selected.shortLabel)
    }
}
