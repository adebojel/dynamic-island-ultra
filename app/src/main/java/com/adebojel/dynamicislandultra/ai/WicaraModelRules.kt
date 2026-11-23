package com.adebojel.dynamicislandultra.ai

internal object WicaraModelRules {
    const val MAX_MODEL_BYTES = 100_000_000L
    const val CONTEXT_TOKENS = 512
    const val MAX_OUTPUT_TOKENS = 96

    fun modelSizeAllowed(sizeBytes: Long): Boolean = sizeBytes in 1L..MAX_MODEL_BYTES
    fun hasGgufHeader(header: ByteArray): Boolean =
        header.size >= 4 && header[0] == 'G'.code.toByte() &&
            header[1] == 'G'.code.toByte() && header[2] == 'U'.code.toByte() &&
            header[3] == 'F'.code.toByte()

    fun sha256Matches(expected: String, actual: String): Boolean =
        expected.matches(Regex("^[a-fA-F0-9]{64}$")) &&
            expected.equals(actual, ignoreCase = true)
}
