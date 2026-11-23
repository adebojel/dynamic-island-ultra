package com.adebojel.dynamicislandultra.ai

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WicaraAndroidIntegrationTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun packagedModelIsProvisionedAndVerifiedInPrivateStorage() {
        val model = WicaraModelStore.prepare(context)
        assertTrue(model.file.isFile)
        assertTrue(model.file.parentFile?.canonicalPath?.startsWith(
            context.noBackupFilesDir.canonicalPath
        ) == true)
        assertTrue(WicaraModelRules.modelSizeAllowed(model.sizeBytes))
        assertTrue(model.revision.matches(Regex("^[0-9a-f]{40}$")))
        assertTrue(model.sha256.matches(Regex("^[0-9a-f]{64}$")))
    }

    @Test
    fun bundledWicaraRunsLocallyAndCanUnloadAndReload() = runBlocking {
        // This workflow deliberately uses an ARM64 emulator. Never silently skip on that runner.
        assumeTrue("ARM64 native runtime must be available on the configured emulator",
            WicaraEngine.isSupported())
        val engine = WicaraEngine(context)
        try {
            engine.load()
            val answer = engine.generate("Balas dengan satu kata saja: Halo.")
            assertTrue("Native model must produce a response", answer.isNotBlank())
            assertFalse("Native generation must not return an internal error",
                answer.startsWith("ERROR:"))
            assertFalse("Test must not pass on the empty-output fallback",
                answer.startsWith("Wicara belum menghasilkan jawaban."))

            engine.unload()
            engine.load()
            val secondAnswer = engine.generate("Sebutkan satu warna.")
            assertTrue("Model must be usable after unload and reload", secondAnswer.isNotBlank())
            assertFalse(secondAnswer.startsWith("ERROR:"))
        } finally {
            engine.unload()
        }
    }

    @Test
    fun deterministicArithmeticStaysOutsideTheLanguageModel() {
        val assistant = OfflineAssistant()
        val arithmetic = assistant.respond("2 + 2")
        assertFalse(arithmetic.shouldAskWicara)
        assertTrue(arithmetic.text.contains("4"))

        val conversation = assistant.respond("Berikan ide sederhana untuk hari ini")
        assertTrue(conversation.shouldAskWicara)
        assertTrue(conversation.action == null)
    }
}
