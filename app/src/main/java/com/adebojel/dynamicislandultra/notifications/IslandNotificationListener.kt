package com.adebojel.dynamicislandultra.notifications

import android.app.Notification
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.adebojel.dynamicislandultra.overlay.FloatingIslandService

class IslandNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val posted = sbn ?: return
        val notification = posted.notification ?: return
        val extras = notification.extras ?: return
        val settings = NotificationPreviewSettingsStore.load(this)

        val token = if (Build.VERSION.SDK_INT >= 33) {
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token
        }
        if (token != null) ActiveMediaSession.attach(this, token)

        if (settings.enabled &&
            notification.category == Notification.CATEGORY_CALL &&
            notification.visibility != Notification.VISIBILITY_SECRET
        ) {
            val label = runCatching {
                val info = packageManager.getApplicationInfo(posted.packageName, 0)
                packageManager.getApplicationLabel(info).toString()
            }.getOrDefault("Aplikasi telepon")
            val callState = if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) {
                "Panggilan berlangsung"
            } else {
                "Panggilan masuk"
            }
            sendBroadcast(Intent(FloatingIslandService.ACTION_CALL_ACTIVITY).setPackage(packageName).apply {
                putExtra("preview", "$callState • $label")
            })
            return
        }

        val candidate = NotificationPreviewCandidate(
            packageName = posted.packageName.orEmpty(),
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
            isSecretVisibility = notification.visibility == Notification.VISIBILITY_SECRET,
            isOngoing = notification.flags and Notification.FLAG_ONGOING_EVENT != 0,
            isGroupSummary = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        )
        val label = runCatching {
            val info = packageManager.getApplicationInfo(candidate.packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(candidate.packageName)
        val preview = NotificationPreviewPolicy.createPreview(
            candidate, settings, packageName, label
        ) ?: return
        sendBroadcast(Intent(FloatingIslandService.ACTION_NOTIFICATION_PREVIEW).setPackage(packageName).apply {
            putExtra("preview", preview)
        })
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let { ActiveMediaSession.clearIfPackageMatches(it.packageName) }
    }

    override fun onDestroy() {
        ActiveMediaSession.release()
        super.onDestroy()
    }
}
