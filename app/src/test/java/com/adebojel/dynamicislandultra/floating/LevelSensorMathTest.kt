package com.adebojel.dynamicislandultra.floating

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LevelSensorMathTest {
    @Test fun flatSampleIsZeroDegrees() {
        val angles = LevelSensorMath.angles(floatArrayOf(0f, 0f, 9.81f))!!
        assertEquals(0f, angles.first, 0.001f)
        assertEquals(0f, angles.second, 0.001f)
    }

    @Test fun positiveXAxisTiltProducesNegativePitch() {
        val angles = LevelSensorMath.angles(floatArrayOf(1f, 0f, 1f))!!
        assertEquals(-45f, angles.first, 0.01f)
    }

    @Test fun rejectsInvalidSamples() {
        assertNull(LevelSensorMath.angles(floatArrayOf(1f, 2f)))
        assertNull(LevelSensorMath.angles(floatArrayOf(1f, Float.NaN, 0f)))
    }
}
