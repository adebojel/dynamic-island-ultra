package com.adebojel.dynamicislandultra.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

data class LocalReminder(val id: Long, val title: String, val triggerAt: Long)

object ReminderRules {
    const val MIN_MINUTES = 1
    const val MAX_MINUTES = 10_080
    const val MAX_PENDING = 30
    const val MAX_TITLE_LENGTH = 80
    fun isValidTitle(title: String) = title.trim().isNotEmpty() && title.trim().length <= MAX_TITLE_LENGTH
    fun isValidMinutes(minutes: Int) = minutes in MIN_MINUTES..MAX_MINUTES
}

object ReminderScheduler {
    private const val PREFS = "reminders"
    private const val KEY_ITEMS = "scheduled_items"
    const val ACTION_FIRE = "com.adebojel.dynamicislandultra.action.FIRE_REMINDER"
    const val EXTRA_ID = "reminder_id"

    fun pending(context: Context): List<LocalReminder> =
        read(context).filter { it.triggerAt > System.currentTimeMillis() }.sortedBy { it.triggerAt }

    fun schedule(context: Context, title: String, minutes: Int): LocalReminder {
        require(ReminderRules.isValidTitle(title)) { "Judul pengingat harus 1–80 karakter." }
        require(ReminderRules.isValidMinutes(minutes)) { "Durasi harus 1 menit sampai 7 hari." }
        val list = pending(context)
        require(list.size < ReminderRules.MAX_PENDING) { "Maksimal 30 pengingat aktif." }
        val id = System.currentTimeMillis().coerceAtLeast(1L)
        val reminder = LocalReminder(id, title.trim(), id + minutes * 60_000L)
        write(context, list + reminder)
        scheduleStored(context, reminder)
        return reminder
    }

    fun cancel(context: Context, id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntent(context, id)?.let { alarmManager.cancel(it); it.cancel() }
        write(context, read(context).filterNot { it.id == id })
    }

    fun consume(context: Context, id: Long): LocalReminder? {
        val list = read(context)
        val item = list.firstOrNull { it.id == id }
        write(context, list.filterNot { it.id == id })
        return item
    }

    fun rescheduleAll(context: Context) {
        read(context).filter { it.triggerAt > System.currentTimeMillis() }.forEach { scheduleStored(context, it) }
        write(context, pending(context))
    }

    private fun scheduleStored(context: Context, reminder: LocalReminder) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntent(context, reminder.id)?.let {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.triggerAt, it)
        }
    }

    private fun pendingIntent(context: Context, id: Long): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_FIRE
            data = Uri.parse("dynamic-island-ultra://reminder/$id")
            putExtra(EXTRA_ID, id)
        }
        return PendingIntent.getBroadcast(context, id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun read(context: Context): List<LocalReminder> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ITEMS, "[]")
        val array = runCatching { JSONArray(raw ?: "[]") }.getOrElse { JSONArray() }
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val id = item.optLong("id")
                val title = item.optString("title")
                val at = item.optLong("triggerAt")
                if (id > 0L && at > 0L && ReminderRules.isValidTitle(title)) add(LocalReminder(id, title, at))
            }
        }
    }

    private fun write(context: Context, reminders: List<LocalReminder>) {
        val array = JSONArray()
        reminders.forEach { array.put(JSONObject().put("id", it.id).put("title", it.title).put("triggerAt", it.triggerAt)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ITEMS, array.toString()).apply()
    }
}
