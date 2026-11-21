package com.adebojel.dynamicislandultra.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.os.Build
import com.adebojel.dynamicislandultra.overlay.FloatingIslandService

class IslandNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val extras = notification.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return
        val preview = listOf(title, text).filter { it.isNotBlank() }.joinToString(": ").take(70)
        sendBroadcast(Intent(FloatingIslandService.ACTION_NOTIFICATION_PREVIEW).apply {
            setPackage(packageName)
            putExtra("preview", preview)
        })
    }
}
