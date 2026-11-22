package com.adebojel.dynamicislandultra.notifications

data class NotificationPreviewCandidate(
    val packageName: String,
    val title: String,
    val text: String,
    val isSecretVisibility: Boolean = false,
    val isOngoing: Boolean = false,
    val isGroupSummary: Boolean = false
)

data class NotificationPreviewSettings(
    val enabled: Boolean = true,
    val hideContent: Boolean = true,
    val allowlistOnly: Boolean = false,
    val allowedPackages: Set<String> = emptySet()
)

object NotificationPreviewPolicy {
    private const val MAX_LENGTH = 70

    fun createPreview(
        candidate: NotificationPreviewCandidate,
        settings: NotificationPreviewSettings,
        ownPackageName: String,
        applicationLabel: String
    ): String? {
        if (!settings.enabled) return null
        if (candidate.packageName.isBlank() || candidate.packageName == ownPackageName) return null
        if (candidate.isSecretVisibility || candidate.isOngoing || candidate.isGroupSummary) return null
        if (settings.allowlistOnly && candidate.packageName !in settings.allowedPackages) return null
        val title = normalize(candidate.title)
        val body = normalize(candidate.text)
        if (title.isBlank() && body.isBlank()) return null
        if (settings.hideContent) {
            val label = normalize(applicationLabel).ifBlank { candidate.packageName }
            return "Notifikasi dari $label".take(MAX_LENGTH)
        }
        return listOf(title, body).filter { it.isNotBlank() }
            .joinToString(": ").take(MAX_LENGTH).ifBlank { null }
    }

    private fun normalize(value: String): String =
        value.filter { it == '\n' || it == '\t' || it >= ' ' }
            .replace(Regex("\\s+"), " ")
            .trim()
}
