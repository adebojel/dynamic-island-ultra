package com.adebojel.dynamicislandultra.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.adebojel.dynamicislandultra.R
import kotlin.math.abs

class FloatingIslandService : Service() {
    private lateinit var windowManager: WindowManager
    private var bubble: TextView? = null
    private var panel: View? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private var expanded = false
    private var latestText = "DYNAMIC ISLAND"
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            latestText = intent?.getStringExtra("preview")?.take(70) ?: latestText
            bubble?.text = "●  " + latestText
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(4100, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val filter = IntentFilter(ACTION_NOTIFICATION_PREVIEW)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(notificationReceiver, filter, RECEIVER_NOT_EXPORTED)
        else @Suppress("DEPRECATION") registerReceiver(notificationReceiver, filter)
        showBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun showBubble() {
        if (bubble != null) return
        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (28 * resources.displayMetrics.density).toInt()
        }
        bubble = TextView(this).apply {
            text = "●  DYNAMIC ISLAND"
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(24, 14, 24, 14)
            background = rounded(Color.rgb(10, 13, 24), Color.rgb(67, 232, 255), 48f, 2)
            setOnClickListener { togglePanel() }
            setOnTouchListener(DragTouchListener())
        }
        windowManager.addView(bubble, bubbleParams)
    }

    private fun togglePanel() {
        if (expanded) {
            panel?.let { runCatching { windowManager.removeView(it) } }
            panel = null
            expanded = false
            return
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            background = rounded(Color.rgb(15, 20, 36), Color.rgb(145, 86, 255), 26f, 2)
        }
        val heading = label("DYNAMIC ISLAND ULTRA", 15f, true)
        root.addView(heading)
        root.addView(label("ASSISTIVE CONTROL CENTER", 9f, false))
        addSection(root, "QUICK TOOLS", listOf(
            "Wi-Fi settings" to android.provider.Settings.Panel.ACTION_WIFI,
            "Bluetooth settings" to android.provider.Settings.ACTION_BLUETOOTH_SETTINGS,
            "Internet settings" to android.provider.Settings.Panel.ACTION_INTERNET_CONNECTIVITY,
            "Sound settings" to android.provider.Settings.ACTION_SOUND_SETTINGS
        ))
        addSection(root, "DEVICE & UTILITIES", listOf(
            "Display settings" to android.provider.Settings.ACTION_DISPLAY_SETTINGS,
            "Battery settings" to android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS,
            "Accessibility settings" to android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS,
            "App settings" to android.provider.Settings.ACTION_APPLICATION_SETTINGS
        ))
        val close = label("CLOSE", 12f, true).apply {
            gravity = Gravity.CENTER
            setPadding(12, 18, 12, 8)
            setOnClickListener { togglePanel() }
        }
        root.addView(close)
        val params = WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels * .88f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (85 * resources.displayMetrics.density).toInt()
        }
        panel = root
        windowManager.addView(root, params)
        expanded = true
    }

    private fun addSection(root: LinearLayout, title: String, actions: List<Pair<String, String>>) {
        root.addView(label(title, 10f, true).apply { setPadding(0, 18, 0, 8) })
        actions.forEach { (name, action) ->
            val item = label("›  $name", 13f, false).apply {
                setPadding(12, 12, 12, 12)
                background = rounded(Color.rgb(28, 36, 56), Color.rgb(54, 70, 100), 14f, 1)
                setOnClickListener {
                    runCatching {
                        val i = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)
                    }
                }
            }
            root.addView(item, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 7 })
        }
    }

    private fun label(value: String, size: Float, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.WHITE)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun rounded(color: Int, stroke: Int, radius: Float, width: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius * resources.displayMetrics.density
        setStroke(width, stroke)
    }

    private inner class DragTouchListener : View.OnTouchListener {
        private var downX = 0f
        private var downY = 0f
        private var startX = 0
        private var startY = 0
        private var dragged = false
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY
                    startX = bubbleParams.x; startY = bubbleParams.y
                    dragged = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downX).toInt()
                    val dy = (event.rawY - downY).toInt()
                    if (abs(dx) + abs(dy) > 12) dragged = true
                    bubbleParams.x = startX + dx
                    bubbleParams.y = startY + dy
                    runCatching { windowManager.updateViewLayout(bubble, bubbleParams) }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (!dragged) v.performClick()
                    return true
                }
            }
            return false
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("island_service", "Dynamic Island", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, "island_service")
            .setSmallIcon(R.drawable.ic_stat_island)
            .setContentTitle("Dynamic Island Ultra is active")
            .setContentText("Floating island and assistive bubble are running")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    override fun onDestroy() {
        runCatching { unregisterReceiver(notificationReceiver) }
        bubble?.let { runCatching { windowManager.removeView(it) } }
        panel?.let { runCatching { windowManager.removeView(it) } }
        bubble = null; panel = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_NOTIFICATION_PREVIEW = "com.adebojel.dynamicislandultra.NOTIFICATION_PREVIEW"
        const val ACTION_STOP = "com.adebojel.dynamicislandultra.STOP_ISLAND"
    }
}
