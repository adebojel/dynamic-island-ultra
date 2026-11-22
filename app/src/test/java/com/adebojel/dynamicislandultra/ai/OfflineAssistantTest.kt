package com.adebojel.dynamicislandultra.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineAssistantTest {
    private val assistant = OfflineAssistant()

    @Test
    fun arithmeticHonoursPrecedenceAndParentheses() {
        assertEquals("Hasil: 25", assistant.respond("3 * (4 + 5) - 2").text)
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
    fun browserCommandRoutesToDefaultBrowserWithoutNetworkInference() {
        assertEquals(OfflineAction.OpenBrowser, assistant.respond("Buka DuckDuckGo").action)
    }

    @Test
    fun updateCommandRoutesToOfficialUpdateCenter() {
        assertEquals(OfflineAction.OpenUpdateCenter, assistant.respond("cek update").action)
    }

    @Test
    fun updateCommandUsesDistributionNeutralWording() {
        assertEquals(
            "Membuka pemeriksa pembaruan resmi aplikasi.",
            assistant.respond("cek update").text
        )
    }

    @Test
    fun diagnosticsAreRunLocally() {
        assertEquals(OfflineAction.RunDiagnostics, assistant.respond("cek kesehatan aplikasi").action)
    }

    @Test
    fun qrScannerCommandIsRecognised() {
        assertEquals(OfflineAction.OpenQrScanner, assistant.respond("buka pemindai QR").action)
    }

    @Test
    fun sensorKitCommandIsRecognised() {
        assertEquals(OfflineAction.OpenSensorKit, assistant.respond("buka sensor").action)
    }

    @Test
    fun mediaControlsCommandIsRecognised() {
        assertEquals(OfflineAction.OpenMediaControls, assistant.respond("kontrol musik").action)
    }

    @Test
    fun notificationPrivacyCommandIsRecognised() {
        assertEquals(OfflineAction.OpenNotificationPrivacy, assistant.respond("filter notifikasi").action)
    }

    @Test
    fun islandSettingsCommandIsRecognised() {
        assertEquals(OfflineAction.OpenIslandSettings, assistant.respond("pengaturan island").action)
    }

    @Test
    fun unknownCommandDoesNotCreateExternalAction() {
        assertNull(assistant.respond("buatkan lagu").action)
    }
}
