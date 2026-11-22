package com.adebojel.dynamicislandultra.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationPreviewPolicyTest {
    private val sample = NotificationPreviewCandidate(
        packageName = "com.example.chat",
        title = "Pesan baru",
        text = "Isi pesan rahasia"
    )

    @Test
    fun hidesContentByDefault() {
        assertEquals("Notifikasi dari Chat", NotificationPreviewPolicy.createPreview(
            sample, NotificationPreviewSettings(), "com.adebojel.dynamicislandultra", "Chat"
        ))
    }

    @Test
    fun explicitSettingCanShowContent() {
        assertEquals("Pesan baru: Isi pesan rahasia", NotificationPreviewPolicy.createPreview(
            sample, NotificationPreviewSettings(hideContent = false),
            "com.adebojel.dynamicislandultra", "Chat"
        ))
    }

    @Test
    fun globalDisableDropsNotifications() {
        assertNull(NotificationPreviewPolicy.createPreview(
            sample, NotificationPreviewSettings(enabled = false),
            "com.adebojel.dynamicislandultra", "Chat"
        ))
    }

    @Test
    fun allowlistRejectsUnselectedApps() {
        assertNull(NotificationPreviewPolicy.createPreview(
            sample,
            NotificationPreviewSettings(allowlistOnly = true, allowedPackages = setOf("com.example.mail")),
            "com.adebojel.dynamicislandultra", "Chat"
        ))
    }

    @Test
    fun allowlistPermitsSelectedApp() {
        assertEquals("Notifikasi dari Chat", NotificationPreviewPolicy.createPreview(
            sample,
            NotificationPreviewSettings(allowlistOnly = true, allowedPackages = setOf("com.example.chat")),
            "com.adebojel.dynamicislandultra", "Chat"
        ))
    }

    @Test
    fun secretOngoingGroupAndOwnNotificationsAreSuppressed() {
        listOf(
            sample.copy(isSecretVisibility = true),
            sample.copy(isOngoing = true),
            sample.copy(isGroupSummary = true),
            sample.copy(packageName = "com.adebojel.dynamicislandultra")
        ).forEach { candidate ->
            assertNull(NotificationPreviewPolicy.createPreview(
                candidate, NotificationPreviewSettings(hideContent = false),
                "com.adebojel.dynamicislandultra", "Chat"
            ))
        }
    }
}
