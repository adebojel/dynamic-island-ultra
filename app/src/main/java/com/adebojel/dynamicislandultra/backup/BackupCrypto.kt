package com.adebojel.dynamicislandultra.backup

import java.io.ByteArrayOutputStream
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Versioned authenticated encryption for local backup files. */
internal object BackupCrypto {
    const val MIN_PASSWORD_LENGTH = 8
    private const val ITERATIONS = 180_000
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private val MAGIC = byteArrayOf(0x44, 0x49, 0x55, 0x42, 0x01)

    fun encrypt(plain: ByteArray, password: CharArray): ByteArray {
        require(password.size >= MIN_PASSWORD_LENGTH) { "Password too short." }
        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val iv = ByteArray(IV_BYTES).also(SecureRandom()::nextBytes)
        val header = MAGIC + salt + iv
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(header)
        val encrypted = cipher.doFinal(plain)
        return ByteArrayOutputStream(header.size + encrypted.size).apply {
            write(header)
            write(encrypted)
        }.toByteArray()
    }

    @Throws(GeneralSecurityException::class)
    fun decrypt(input: ByteArray, password: CharArray): ByteArray {
        require(password.size >= MIN_PASSWORD_LENGTH) { "Password too short." }
        val headerLength = MAGIC.size + SALT_BYTES + IV_BYTES
        require(input.size > headerLength + 16) { "Backup file is too short." }
        require(input.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) { "Unknown backup format." }
        val saltStart = MAGIC.size
        val ivStart = saltStart + SALT_BYTES
        val header = input.copyOfRange(0, headerLength)
        val salt = input.copyOfRange(saltStart, ivStart)
        val iv = input.copyOfRange(ivStart, headerLength)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(header)
        return cipher.doFinal(input.copyOfRange(headerLength, input.size))
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
            try {
                SecretKeySpec(bytes, "AES")
            } finally {
                bytes.fill(0)
            }
        } finally {
            spec.clearPassword()
        }
    }
}
