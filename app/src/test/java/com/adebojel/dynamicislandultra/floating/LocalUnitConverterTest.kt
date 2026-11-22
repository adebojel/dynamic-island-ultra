package com.adebojel.dynamicislandultra.floating

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalUnitConverterTest {
    @Test fun convertsLengthAcrossMetricUnits() {
        assertEquals(125.0, LocalUnitConverter.convert(1.25, "length", "m", "cm")!!, 0.000001)
    }

    @Test fun convertsMassAndVolume() {
        assertEquals(1000.0, LocalUnitConverter.convert(1.0, "mass", "kg", "g")!!, 0.000001)
        assertEquals(1.0, LocalUnitConverter.convert(1000.0, "volume", "ml", "l")!!, 0.000001)
    }

    @Test fun convertsTemperatureThroughCelsius() {
        assertEquals(0.0, LocalUnitConverter.convert(32.0, "temperature", "f", "c")!!, 0.000001)
        assertEquals(273.15, LocalUnitConverter.convert(0.0, "temperature", "c", "k")!!, 0.000001)
    }

    @Test fun rejectsUnknownCategoriesAndUnits() {
        assertNull(LocalUnitConverter.convert(1.0, "distance", "m", "cm"))
        assertNull(LocalUnitConverter.convert(1.0, "length", "m", "kg"))
    }
}
