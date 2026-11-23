package com.adebojel.dynamicislandultra.ai

internal object WicaraPromptBuilder {
    const val MAX_USER_CHARS = 1_200

    // Wicara uses explicit role/end markers; do not substitute a generic chat wrapper.
    fun build(userInput: String): String {
        val safeInput = userInput.replace('\u0000', ' ').trim().take(MAX_USER_CHARS)
        return buildString {
            append("<|system|>\n")
            append("Kamu adalah Wicara, asisten Bahasa Indonesia yang ramah dan ringkas. ")
            append("Jawab berdasarkan informasi yang tersedia dan jangan mengarang fakta. ")
            append("Hitungan, kontrol Android, izin, diagnostik, dan status perangkat selalu ditangani ")
            append("oleh alat deterministik aplikasi; jangan mengaku telah menjalankan tindakan yang belum dilakukan. ")
            append("Akui keterbatasan jika tidak yakin.")
            append("<|end|>\n<|user|>\n")
            append(safeInput)
            append("<|end|>\n<|assistant|>\n")
        }
    }
}
