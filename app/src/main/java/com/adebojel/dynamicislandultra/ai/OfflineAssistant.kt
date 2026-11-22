package com.adebojel.dynamicislandultra.ai

import java.text.Normalizer
import java.util.Locale

data class OfflineAssistantReply(
    val text: String,
    val action: OfflineAction? = null
)

sealed interface OfflineAction {
    data object RunDiagnostics : OfflineAction
    data object OpenBrowser : OfflineAction
    data object OpenUpdateCenter : OfflineAction
    data object ShowBattery : OfflineAction
    data object OpenWifiSettings : OfflineAction
    data object OpenBluetoothSettings : OfflineAction
    data object OpenAccessibilitySettings : OfflineAction
    data object OpenInstalledApps : OfflineAction
    data object OpenCalculator : OfflineAction
    data object OpenTimer : OfflineAction
    data object OpenFiles : OfflineAction
    data object OpenDisplaySettings : OfflineAction
    data object OpenSoundSettings : OfflineAction
    data object OpenQrScanner : OfflineAction
    data object OpenSensorKit : OfflineAction
    data object OpenMediaControls : OfflineAction
    data object OpenDocumentPreview : OfflineAction
    data object OpenNotificationPrivacy : OfflineAction
    data object OpenIslandSettings : OfflineAction
}

/**
 * A deterministic, on-device utility assistant. No network, API keys, telemetry,
 * external inference endpoint, or remote LLM is used.
 *
 * This is an offline command assistant, not a generative LLM. The modular
 * interface can later accept a separately installed local model runtime.
 */
class OfflineAssistant {
    fun respond(input: String, batteryPercent: Int? = null): OfflineAssistantReply {
        val query = normalize(input)
        if (query.isBlank()) return OfflineAssistantReply("Ketik perintah atau pilih salah satu alat offline.")

        return when {
            hasAny(query, "diagnostik", "diagnostic", "periksa sistem", "cek kesehatan", "status aplikasi", "cek aplikasi") ->
                OfflineAssistantReply("Menjalankan pemeriksaan lokal fitur dan izin perangkat.", OfflineAction.RunDiagnostics)
            hasAny(query, "browser", "duckduckgo", "internet", "youtube", "buka web", "buka situs") ->
                OfflineAssistantReply("Membuka DuckDuckGo Browser. Browser membutuhkan koneksi internet.", OfflineAction.OpenBrowser)
            hasAny(query, "update", "pembaruan", "versi terbaru") ->
                OfflineAssistantReply("Membuka pemeriksa pembaruan resmi aplikasi.", OfflineAction.OpenUpdateCenter)
            hasAny(query, "baterai", "battery", "daya") ->
                OfflineAssistantReply(
                    batteryPercent?.let { "Baterai perangkat saat ini $it%." }
                        ?: "Data baterai belum tersedia dari sistem.",
                    OfflineAction.ShowBattery
                )
            hasAny(query, "wifi", "wi-fi", "wi fi") ->
                OfflineAssistantReply("Membuka pengaturan Wi-Fi Android.", OfflineAction.OpenWifiSettings)
            hasAny(query, "bluetooth", "bt") ->
                OfflineAssistantReply("Membuka pengaturan Bluetooth Android.", OfflineAction.OpenBluetoothSettings)
            hasAny(query, "aksesibilitas", "accessibility", "talkback") ->
                OfflineAssistantReply("Membuka pengaturan aksesibilitas Android.", OfflineAction.OpenAccessibilitySettings)
            hasAny(query, "aplikasi terpasang", "daftar aplikasi", "kumpulan aplikasi", "installed apps") ->
                OfflineAssistantReply("Menampilkan daftar aplikasi yang dapat dibuka.", OfflineAction.OpenInstalledApps)
            hasAny(query, "kalkulator", "calculator", "hitung") ->
                OfflineAssistantReply("Membuka kalkulator perangkat.", OfflineAction.OpenCalculator)
            hasAny(query, "timer", "penghitung waktu", "alarm") ->
                OfflineAssistantReply("Membuka timer atau alarm perangkat.", OfflineAction.OpenTimer)
            hasAny(query, "file", "berkas", "dokumen") ->
                OfflineAssistantReply("Membuka pemilih berkas Android.", OfflineAction.OpenFiles)
            hasAny(query, "layar", "display", "kecerahan") ->
                OfflineAssistantReply("Membuka pengaturan tampilan.", OfflineAction.OpenDisplaySettings)
            hasAny(query, "suara", "volume", "audio") ->
                OfflineAssistantReply("Membuka pengaturan suara.", OfflineAction.OpenSoundSettings)
            hasAny(query, "qr", "barcode", "kode batang", "pindai kode") ->
                OfflineAssistantReply("Membuka pemindai QR dan barcode.", OfflineAction.OpenQrScanner)
            hasAny(query, "sensor", "accelerometer", "gyroscope", "kompas") ->
                OfflineAssistantReply("Membuka sensor perangkat yang tersedia.", OfflineAction.OpenSensorKit)
            hasAny(query, "media", "musik", "lagu", "putar musik", "jeda musik") ->
                OfflineAssistantReply("Membuka kontrol media dari pemutar yang didukung.", OfflineAction.OpenMediaControls)
            hasAny(query, "preview dokumen", "pratinjau dokumen", "lihat pdf", "pratinjau gambar") ->
                OfflineAssistantReply("Membuka pemilih gambar dan PDF.", OfflineAction.OpenDocumentPreview)
            hasAny(query, "privasi notifikasi", "filter notifikasi", "sembunyikan isi notifikasi") ->
                OfflineAssistantReply("Membuka pengaturan privasi notifikasi.", OfflineAction.OpenNotificationPrivacy)
            hasAny(query, "pengaturan island", "atur island", "haptic island", "snap island") ->
                OfflineAssistantReply("Membuka pengaturan interaksi island.", OfflineAction.OpenIslandSettings)
            query.matches(Regex("^[0-9+\\-*/().% ]+$")) ->
                OfflineAssistantReply(calculate(query))
            hasAny(query, "bantuan", "help", "apa yang bisa", "perintah") ->
                OfflineAssistantReply(
                    "Saya dapat membuka Wi-Fi, Bluetooth, aksesibilitas, aplikasi terpasang, kalkulator, timer, file, tampilan, suara, pemindai QR, sensor, kontrol media, pratinjau dokumen, privasi notifikasi dan pengaturan island. Saya juga dapat menghitung ekspresi matematika sederhana."
                )
            else ->
                OfflineAssistantReply(
                    "Perintah belum dikenali. Coba: baterai, buka Wi-Fi, daftar aplikasi, kalkulator, timer, atau bantuan."
                )
        }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFKC).lowercase(Locale.ROOT).trim()

    private fun hasAny(value: String, vararg needles: String): Boolean =
        needles.any { value.contains(it) }

    private fun calculate(expression: String): String {
        return runCatching {
            val parser = ExpressionParser(expression)
            val result = parser.parse()
            require(result.isFinite()) { "Hasil tidak valid" }
            "Hasil: " + if (result % 1.0 == 0.0) result.toLong().toString() else "%.6f".format(Locale.ROOT, result).trimEnd('0').trimEnd('.')
        }.getOrElse { "Ekspresi tidak valid. Contoh: 125 * (4 + 3)." }
    }
}

private class ExpressionParser(private val source: String) {
    private var index = 0

    fun parse(): Double {
        val value = parseExpression()
        skipSpace()
        require(index == source.length) { "Karakter tersisa" }
        return value
    }

    private fun parseExpression(): Double {
        var value = parseTerm()
        while (true) {
            skipSpace()
            value = when {
                consume('+') -> value + parseTerm()
                consume('-') -> value - parseTerm()
                else -> return value
            }
        }
    }

    private fun parseTerm(): Double {
        var value = parseFactor()
        while (true) {
            skipSpace()
            value = when {
                consume('*') -> value * parseFactor()
                consume('/') -> value / parseFactor()
                consume('%') -> value % parseFactor()
                else -> return value
            }
        }
    }

    private fun parseFactor(): Double {
        skipSpace()
        if (consume('+')) return parseFactor()
        if (consume('-')) return -parseFactor()
        if (consume('(')) {
            val value = parseExpression()
            skipSpace()
            require(consume(')')) { "Kurung tidak lengkap" }
            return value
        }
        val start = index
        while (index < source.length && (source[index].isDigit() || source[index] == '.')) index++
        require(index > start) { "Angka diharapkan" }
        return source.substring(start, index).toDouble()
    }

    private fun consume(char: Char): Boolean {
        if (index < source.length && source[index] == char) {
            index++
            return true
        }
        return false
    }

    private fun skipSpace() {
        while (index < source.length && source[index].isWhitespace()) index++
    }
}
