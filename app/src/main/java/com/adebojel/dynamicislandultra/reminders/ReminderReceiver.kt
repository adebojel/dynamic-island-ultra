package com.adebojel.dynamicislandultra.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adebojel.dynamicislandultra.MainActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ReminderScheduler.ACTION_FIRE) return
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_ID, 0L)
        if (id <= 0L) return
        val reminder = ReminderScheduler.consume(context, id) ?: return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(
                CHANNEL_ID, "Pengingat", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Pengingat lokal yang dijadwalkan pengguna." })
        }
        val openApp = PendingIntent.getActivity(context, id.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(reminder.title)
            .setContentText("Pengingat terjadwal Anda.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        runCatching {
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
            }
        }
    }
    companion object { private const val CHANNEL_ID = "local_reminders" }
}
