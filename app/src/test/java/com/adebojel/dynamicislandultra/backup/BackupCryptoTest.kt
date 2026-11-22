package com.adebojel.dynamicislandultra.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.GeneralSecurityException

class BackupCryptoTest {
    @Test fun encryptDecryptRoundTrip() {
        val plain = "local notes and settings".toByteArray()
        val encrypted = BackupCrypto.encrypt(plain, "strong-passphrase".toCharArray())
        assertTrue(!encrypted.contentEquals(plain))
        assertArrayEquals(plain, BackupCrypto.decrypt(encrypted, "strong-passphrase".toCharArray()))
    }

    @Test(expected = GeneralSecurityException::class)
    fun rejectsWrongPassword() {
        val encrypted = BackupCrypto.encrypt("secret".toByteArray(), "strong-passphrase".toCharArray())
        BackupCrypto.decrypt(encrypted, "different-password".toCharArray())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsShortPassword() {
        BackupCrypto.encrypt("data".toByteArray(), "short".toCharArray())
    }
}
