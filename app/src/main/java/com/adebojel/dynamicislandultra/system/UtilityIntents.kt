package com.adebojel.dynamicislandultra.system

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings

/**
 * Intents to Android-owned surfaces and user-installed apps.
 * These shortcuts never attempt to silently change protected system settings.
 */
object UtilityIntents {
    fun create(key: String, packageName: String): Intent? {
        val intent = when (key) {
            "wifi" -> if (Build.VERSION.SDK_INT >= 29) {
                Intent(Settings.Panel.ACTION_WIFI)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            "internet" -> if (Build.VERSION.SDK_INT >= 29) {
                Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            } else {
                Intent(Settings.ACTION_WIRELESS_SETTINGS)
            }
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "sound" -> Intent(Settings.ACTION_SOUND_SETTINGS)
            "display" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "battery" -> Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            "accessibility" -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            "apps" -> Intent(Settings.ACTION_APPLICATION_SETTINGS)
            "app_info" -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:$packageName"))
            "app_notifications" -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            "notification_access" -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            "dialer" -> Intent(Intent.ACTION_DIAL)
            "camera" -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            "calculator" -> Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_CALCULATOR
            )
            "calendar" -> Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_CALENDAR
            )
            "music" -> Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_MUSIC
            )
            "browser" -> Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_BROWSER
            )
            "clock" -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            "timer" -> Intent(AlarmClock.ACTION_SET_TIMER)
                .putExtra(AlarmClock.EXTRA_LENGTH, 300)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            "files" -> Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
            "system_settings" -> Intent(Settings.ACTION_SETTINGS)
            else -> return null
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
