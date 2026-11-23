package com.adebojel.dynamicislandultra.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Serializes model load, inference and release; no network path exists in this class. */
internal class WicaraEngine(context: Context) {
    private val appContext = context.applicationContext
    private val operationMutex = Mutex()
    @Volatile private var loaded = false

    suspend fun load() = operationMutex.withLock {
        withContext(Dispatchers.IO) {
            check(isSupported()) {
                "Wicara membutuhkan Android ARM64. Fitur lain tetap tersedia."
            }
            if (!loaded) {
                val model = WicaraModelStore.prepare(appContext)
                check(WicaraNative.nativeLoadModel(
                    model.file.absolutePath, WicaraModelRules.CONTEXT_TOKENS, 2
                )) {
                    "Runtime Wicara gagal memuat model lokal. Tidak ada fallback AI cloud."
                }
                loaded = true
            }
        }
    }

    suspend fun generate(userInput: String): String = operationMutex.withLock {
        withContext(Dispatchers.IO) {
            check(loaded) { "Aktifkan Wicara terlebih dahulu." }
            val response = WicaraNative.nativeGenerate(
                WicaraPromptBuilder.build(userInput), WicaraModelRules.MAX_OUTPUT_TOKENS
            )
            check(!response.startsWith("ERROR:")) { response.removePrefix("ERROR:").trim() }
            response.trim().ifBlank { "Wicara belum menghasilkan jawaban. Coba pertanyaan lebih singkat." }
        }
    }

    fun cancel() {
        if (WicaraNative.isNativeLoaded()) WicaraNative.nativeCancel()
    }

    suspend fun unload() {
        cancel()
        operationMutex.withLock {
            withContext(Dispatchers.IO) {
                if (WicaraNative.isNativeLoaded()) WicaraNative.nativeUnload()
                loaded = false
            }
        }
    }

    companion object {
        fun isSupported(): Boolean = WicaraNative.isSupported()
    }
}
