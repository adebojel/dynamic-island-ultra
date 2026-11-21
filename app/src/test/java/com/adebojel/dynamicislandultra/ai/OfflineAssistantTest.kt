package com.adebojel.dynamicislandultra.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineAssistantTest {
    private val assistant = OfflineAssistant()

    @Test
    fun arithmeticHonoursPrecedenceAndParentheses() {
        assertEquals("Hasil: 31", assistant.respond("3 * (4 + 5) - 2").text)
    }

    @Test
    fun arithmeticHandlesDivisionAndUnaryMinus() {
        assertEquals("Hasil: -4", assistant.respond("-(8 / 2)").text)
    }

    @Test
    fun calculatorRejectsNonFiniteDivision() {
        assertEquals(
            "Ekspresi tidak valid. Contoh: 125 * (4 + 3).",
            assistant.respond("7 / 0").text
        )
    }

    @Test
    fun wifiCommandProducesLocalSystemAction() {
        assertEquals(OfflineAction.OpenWifiSettings, assistant.respond("Buka Wi-Fi").action)
    }

    @Test
    fun installedAppsCommandIsRecognised() {
        assertEquals(OfflineAction.OpenInstalledApps, assistant.respond("daftar aplikasi").action)
    }

    @Test
    fun unknownCommandDoesNotCreateExternalAction() {
        assertNull(assistant.respond("buatkan lagu").action)
    }
}
