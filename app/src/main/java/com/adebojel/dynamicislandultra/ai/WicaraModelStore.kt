package com.adebojel.dynamicislandultra.ai

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

internal data class WicaraModelFile(
    val file: File, val revision: String, val sha256: String, val sizeBytes: Long
)

internal object WicaraModelStore {
    private const val MODEL_ASSET = "models/wicara-56m-chat-q8_0.gguf"
    private const val METADATA_ASSET = "wicara-model-metadata.json"
    private const val PRIVATE_FILE = "wicara-56m-chat-q8_0.gguf"

    fun prepare(context: Context): WicaraModelFile {
        val metadata = context.assets.open(METADATA_ASSET).bufferedReader().use {
            JSONObject(it.readText())
        }
        require(metadata.getString("filename") == PRIVATE_FILE) { "Nama file model tidak cocok." }
        val revision = metadata.getString("revision")
        require(revision.matches(Regex("^[0-9a-f]{40}$"))) { "Revision model tidak valid." }
        val expectedSize = metadata.getLong("sizeBytes")
        require(WicaraModelRules.modelSizeAllowed(expectedSize)) {
            "Ukuran model melampaui batas 100.000.000 byte."
        }
        val expectedSha = metadata.getString("sha256").lowercase()
        require(expectedSha.matches(Regex("^[0-9a-f]{64}$"))) { "Checksum model tidak valid." }

        val directory = File(context.noBackupFilesDir, "wicara-model").apply {
            if (!exists() && !mkdirs()) error("Penyimpanan privat model tidak dapat dibuat.")
        }
        val destination = File(directory, PRIVATE_FILE)
        if (destination.isFile && destination.length() == expectedSize &&
            WicaraModelRules.sha256Matches(expectedSha, sha256(destination))) {
            return WicaraModelFile(destination, revision, expectedSha, expectedSize)
        }

        destination.delete()
        val temporary = File(directory, "$PRIVATE_FILE.part")
        temporary.delete()
        val digest = MessageDigest.getInstance("SHA-256")
        var copied = 0L
        context.assets.open(MODEL_ASSET).use { input ->
            FileOutputStream(temporary).use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    copied += count
                    require(WicaraModelRules.modelSizeAllowed(copied)) {
                        "File model melewati batas 100.000.000 byte."
                    }
                    digest.update(buffer, 0, count)
                    output.write(buffer, 0, count)
                }
                output.fd.sync()
            }
        }
        val actualSha = digest.digest().joinToString("") { "%02x".format(it) }
        require(copied == expectedSize) { "Ukuran model berbeda dari metadata upstream." }
        require(WicaraModelRules.sha256Matches(expectedSha, actualSha)) {
            "SHA-256 model tidak cocok dengan artefak upstream."
        }
        val header = ByteArray(4)
        temporary.inputStream().use { input ->
            require(input.read(header) == 4 && WicaraModelRules.hasGgufHeader(header)) {
                "Header GGUF tidak valid."
            }
        }
        if (!temporary.renameTo(destination)) {
            temporary.copyTo(destination, overwrite = true)
            temporary.delete()
        }
        require(destination.isFile && destination.length() == expectedSize) {
            "Penyiapan model ke penyimpanan privat gagal."
        }
        return WicaraModelFile(destination, revision, actualSha, copied)
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
