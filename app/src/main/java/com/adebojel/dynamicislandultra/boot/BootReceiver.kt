package com.adebojel.dynamicislandultra.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.adebojel.dynamicislandultra.overlay.FloatingIslandService
import com.adebojel.dynamicislandultra.reminders.ReminderScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        ReminderScheduler.rescheduleAll(context)
        val enabled = context.getSharedPreferences("island", Context.MODE_PRIVATE)
            .getBoolean("enabled", false)
        if (enabled && Settings.canDrawOverlays(context)) {
            runCatching {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, FloatingIslandService::class.java)
                )
            }
        }
    }
}
