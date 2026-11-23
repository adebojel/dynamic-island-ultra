package com.adebojel.dynamicislandultra.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class EqualizerPolicyTest {
    @Test fun preampIsClampedToSafeUiRange() {
        assertEquals(EqualizerPolicy.MIN_PREAMP_MB, EqualizerPolicy.clampPreamp(-9999))
        assertEquals(EqualizerPolicy.MAX_PREAMP_MB, EqualizerPolicy.clampPreamp(9999))
        assertEquals(250, EqualizerPolicy.clampPreamp(250))
    }

    @Test fun presetsAreStableAndIncludeFlatAndCustom() {
        assertEquals(true, EqualizerPolicy.presets.contains("Flat"))
        assertEquals(true, EqualizerPolicy.presets.contains("Custom"))
    }

    @Test fun flatPresetDoesNotBoostAnyBand() {
        assertEquals(0, EqualizerPolicy.presetGainMb("Flat", 100))
        assertEquals(0, EqualizerPolicy.presetGainMb("Flat", 10000))
    }

    @Test fun bassPresetBoostsLowFrequenciesMoreThanHighFrequencies() {
        val low = EqualizerPolicy.presetGainMb("Bass Boost", 80)
        val high = EqualizerPolicy.presetGainMb("Bass Boost", 10000)
        assertEquals(true, low > high)
    }

    @Test fun vocalPresetBoostsPresenceRegion() {
        assertEquals(true, EqualizerPolicy.presetGainMb("Vocal", 1500) >
            EqualizerPolicy.presetGainMb("Vocal", 80))
    }
}
