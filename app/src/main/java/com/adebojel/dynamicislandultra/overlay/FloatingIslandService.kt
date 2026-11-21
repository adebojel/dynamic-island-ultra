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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.core.app.NotificationCompat
import com.adebojel.dynamicislandultra.R
import com.adebojel.dynamicislandultra.system.UtilityIntents
import com.adebojel.dynamicislandultra.ai.OfflineAssistantActivity
import com.adebojel.dynamicislandultra.floating.InstalledAppsActivity
import com.adebojel.dynamicislandultra.floating.OfflineNotesActivity
import com.adebojel.dynamicislandultra.floating.OfflineCalculatorActivity
import com.adebojel.dynamicislandultra.floating.FloatingWorkspaceActivity
import kotlin.math.abs

class FloatingIslandService : Service() {
    private lateinit var windowManager: WindowManager
    private var bubble: TextView? = null
    private var panel: View? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private var expanded = false
    private var latestText = "DYNAMIC ISLAND"
    private var batteryText = ""
    private var pulseAnimator: ObjectAnimator? = null

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            latestText = intent?.getStringExtra("preview")?.take(54) ?: latestText
            updateBubbleText()
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return
            val level = intent.getIntExtra("level", -1)
            val scale = intent.getIntExtra("scale", 100).coerceAtLeast(1)
            val status = intent.getIntExtra("status", -1)
            if (level >= 0) {
                val percent = (level * 100 / scale).coerceIn(0, 100)
                val charging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL
                batteryText = (if (charging) "⚡ " else "🔋 ") + percent + "%"
                updateBubbleText()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(4100, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        registerAppReceiver(notificationReceiver, IntentFilter(ACTION_NOTIFICATION_PREVIEW))
        registerAppReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun registerAppReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(receiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            getSharedPreferences("island", MODE_PRIVATE).edit()
                .putBoolean("enabled", false).apply()
            stopSelf()
            return START_NOT_STICKY
        }
        if (!android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        showBubble()
        return START_STICKY
    }

    private fun showBubble() {
        if (bubble != null) return
        val prefs = getSharedPreferences("island", MODE_PRIVATE)
        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = prefs.getInt("bubble_x", 0)
            y = prefs.getInt("bubble_y", (28 * resources.displayMetrics.density).toInt())
        }

        bubble = TextView(this).apply {
            text = "●  DYNAMIC ISLAND"
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(22, 13, 22, 13)
            background = rounded(Color.rgb(10, 13, 24), Color.rgb(67, 232, 255), 48f, 2)
            contentDescription = "Dynamic Island floating control. Tap to open tools; drag to move."
            setOnClickListener { togglePanel() }
            setOnTouchListener(DragTouchListener())
        }
        windowManager.addView(bubble, bubbleParams)
        updateBubbleText()
        pulseAnimator = ObjectAnimator.ofFloat(bubble, View.ALPHA, 0.86f, 1f).apply {
            duration = 1450
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun updateBubbleText() {
        val status = if (batteryText.isNotBlank()) "$batteryText  •  " else ""
        bubble?.text = "●  $status${latestText.take(38)}"
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
            setPadding(18, 16, 18, 14)
            background = rounded(Color.rgb(12, 17, 31), Color.rgb(67, 208, 255), 27f, 2)
        }
        root.addView(label("DYNAMIC ISLAND ULTRA", 16f, true))
        root.addView(label("ASSISTIVE CONTROL CENTER", 9f, false).apply {
            setTextColor(Color.rgb(153, 172, 206))
            setPadding(0, 3, 0, 10)
        })

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        addSection(content, "01  QUICK CONNECTIVITY", listOf(
            "Wi-Fi panel" to "wifi",
            "Internet connectivity" to "internet",
            "Bluetooth settings" to "bluetooth",
            "Sound & volume" to "sound"
        ))
        addSection(content, "02  COMMUNICATION", listOf(
            "Phone dialer" to "dialer",
            "App notifications" to "app_notifications",
            "Notification access" to "notification_access"
        ))
        addSection(content, "03  MEDIA & CAPTURE", listOf(
            "Music player" to "music",
            "Camera" to "camera",
            "Web browser" to "browser"
        ))
        addSection(content, "04  PRODUCTIVITY", listOf(
            "Calculator" to "calculator",
            "Calendar" to "calendar",
            "Clock & alarms" to "clock",
            "Start 5-minute timer" to "timer",
            "System file picker" to "files"
        ))
        addSection(content, "05  DEVICE & ACCESSIBILITY", listOf(
            "Display settings" to "display",
            "Battery settings" to "battery",
            "Accessibility settings" to "accessibility",
            "Application settings" to "apps",
            "This app's settings" to "app_info",
            "System settings" to "system_settings"
        ))
        addSection(content, "06  MY FLOATING APPS", listOf(
            "Installed app collection" to "installed_apps",
            "Floating workspace (PiP)" to "workspace",
            "Offline notes" to "notes",
            "Offline calculator" to "local_calculator"
        ))
        addSection(content, "07  OFFLINE INTELLIGENCE", listOf(
            "Offline utility assistant" to "offline_assistant"
        ))

        val scroll = ScrollView(this).apply {
            isFillViewport = false
            addView(content)
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val close = label("CLOSE PANEL  ×", 11f, true).apply {
            gravity = Gravity.CENTER
            setPadding(10, 15, 10, 2)
            setTextColor(Color.rgb(67, 232, 255))
            setOnClickListener { togglePanel() }
        }
        root.addView(close)

        val metrics = resources.displayMetrics
        val width = minOf((metrics.widthPixels * .91f).toInt(), (440 * metrics.density).toInt())
        val height = (metrics.heightPixels * .76f).toInt()
        val params = WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (82 * metrics.density).toInt()
        }
        panel = root
        windowManager.addView(root, params)
        expanded = true
    }

    private fun addSection(
        root: LinearLayout,
        title: String,
        actions: List<Pair<String, String>>
    ) {
        root.addView(label(title, 9f, true).apply {
            setTextColor(Color.rgb(88, 220, 255))
            setPadding(0, 16, 0, 7)
        })
        actions.forEach { (name, key) ->
            val item = label("›   $name", 13f, false).apply {
                setPadding(13, 12, 12, 12)
                background = rounded(
                    Color.rgb(24, 32, 51),
                    Color.rgb(48, 69, 103),
                    14f,
                    1
                )
                contentDescription = name
                setOnClickListener {
                    val internalIntent = when (key) {
                        "installed_apps" -> Intent(this@FloatingIslandService, InstalledAppsActivity::class.java)
                        "workspace" -> Intent(this@FloatingIslandService, FloatingWorkspaceActivity::class.java)
                        "notes" -> Intent(this@FloatingIslandService, OfflineNotesActivity::class.java)
                        "local_calculator" -> Intent(this@FloatingIslandService, OfflineCalculatorActivity::class.java)
                        "offline_assistant" -> Intent(this@FloatingIslandService, OfflineAssistantActivity::class.java)
                        else -> null
                    }
                    val intent = internalIntent ?: UtilityIntents.create(key, packageName)
                    if (intent == null) {
                        Toast.makeText(this@FloatingIslandService, "Tool unavailable", Toast.LENGTH_SHORT).show()
                    } else {
                        runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure {
                            Toast.makeText(
                                this@FloatingIslandService,
                                "$name is not available on this device",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            root.addView(item, LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = (6 * resources.displayMetrics.density).toInt()
            })
        }
    }

    private fun label(value: String, size: Float, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.WHITE)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun rounded(color: Int, stroke: Int, radius: Float, width: Int) =
        GradientDrawable().apply {
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

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = bubbleParams.x
                    startY = bubbleParams.y
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
                    if (dragged) {
                        getSharedPreferences("island", MODE_PRIVATE).edit()
                            .putInt("bubble_x", bubbleParams.x)
                            .putInt("bubble_y", bubbleParams.y)
                            .apply()
                    } else {
                        view.performClick()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "island_service",
                "Dynamic Island",
                NotificationManager.IMPORTANCE_LOW
            )
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
        runCatching { unregisterReceiver(batteryReceiver) }
        pulseAnimator?.cancel()
        pulseAnimator = null
        bubble?.let { runCatching { windowManager.removeView(it) } }
        panel?.let { runCatching { windowManager.removeView(it) } }
        bubble = null
        panel = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_NOTIFICATION_PREVIEW =
            "com.adebojel.dynamicislandultra.NOTIFICATION_PREVIEW"
        const val ACTION_STOP = "com.adebojel.dynamicislandultra.STOP_ISLAND"
    }
}
