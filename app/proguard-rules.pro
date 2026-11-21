# Keep Android component entry points.
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.service.notification.NotificationListenerService { *; }
