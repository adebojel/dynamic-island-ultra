package com.adebojel.dynamicislandultra.overlay

import android.app.Notification
import android.app.PendingIntent
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
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.ViewConfiguration
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
import com.adebojel.dynamicislandultra.ads.PremiumAccess
import com.adebojel.dynamicislandultra.floating.InstalledAppsActivity
import com.adebojel.dynamicislandultra.floating.OfflineNotesActivity
import com.adebojel.dynamicislandultra.floating.OfflineCalculatorActivity
import com.adebojel.dynamicislandultra.floating.FloatingWorkspaceActivity
import com.adebojel.dynamicislandultra.floating.QrScannerActivity
import com.adebojel.dynamicislandultra.floating.SensorKitActivity
import com.adebojel.dynamicislandultra.floating.SmartToolsActivity
import com.adebojel.dynamicislandultra.floating.DocumentPreviewActivity
import com.adebojel.dynamicislandultra.floating.FileManagerActivity
import com.adebojel.dynamicislandultra.system.DeviceControlActivity
import com.adebojel.dynamicislandultra.backup.BackupActivity
import com.adebojel.dynamicislandultra.reminders.ReminderActivity
import com.adebojel.dynamicislandultra.clipboard.ClipboardHistoryActivity
import com.adebojel.dynamicislandultra.notifications.NotificationPreviewSettingsActivity
import com.adebojel.dynamicislandultra.notifications.MediaControlsActivity
import com.adebojel.dynamicislandultra.notifications.ActiveMediaSession
import com.adebojel.dynamicislandultra.overlay.IslandInteractionSettingsActivity
import kotlin.math.abs

class FloatingIslandService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var miniWindows: AppMiniWindowManager
    private var bubble: TextView? = null
    private var panel: View? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private var expanded = false
    private var latestText = "DYNAMIC ISLAND"
    private var batteryText = ""
    private var pulseAnimator: ObjectAnimator? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val autoDimTask = Runnable {
        val preferences = getSharedPreferences("island", MODE_PRIVATE)
        if (!BubbleAutoDimConfig.shouldDim(preferences.getBoolean("auto_dim_enabled", false), expanded, bubble != null)) {
            return@Runnable
        }
        pulseAnimator?.cancel()
        pulseAnimator = null
        val opacity = preferences.getFloat("bubble_opacity", 1f).coerceIn(0.4f, 1f)
        bubble?.animate()?.cancel()
        bubble?.animate()?.alpha(BubbleAutoDimConfig.targetAlpha(opacity))?.setDuration(240L)?.start()
    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            latestText = intent?.getStringExtra("preview")?.take(54) ?: latestText
            updateBubbleText()
        }
    }

    private val activityCardReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ActiveMediaSession.ACTION_CHANGED -> {
                    val title = intent.getStringExtra("title").orEmpty()
                    val artist = intent.getStringExtra("artist").orEmpty()
                    latestText = when {
                        title.isNotBlank() -> "♫ $title".take(38)
                        artist.isNotBlank() -> "♫ $artist".take(38)
                        else -> latestText
                    }
                    updateBubbleText()
                }
                ACTION_CALL_ACTIVITY -> {
                    latestText = intent.getStringExtra("preview")?.take(38) ?: "Panggilan"
                    updateBubbleText()
                }
            }
        }
    }

    private val appearanceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            applyPremiumAppearance()
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
        miniWindows = AppMiniWindowManager(this, windowManager)
        registerAppReceiver(notificationReceiver, IntentFilter(ACTION_NOTIFICATION_PREVIEW))
        registerAppReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        registerAppReceiver(appearanceReceiver, IntentFilter(ACTION_APPEARANCE_CHANGED))
        registerAppReceiver(activityCardReceiver, IntentFilter().apply {
            addAction(ActiveMediaSession.ACTION_CHANGED)
            addAction(ACTION_CALL_ACTIVITY)
        })
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
            contentDescription = "Dynamic Island floating control. Tap, double-tap, long-press, or swipe according to gesture settings; drag to move."
            setOnClickListener { performBubbleGesture("tap") }
            setOnTouchListener(DragTouchListener())
        }
        windowManager.addView(bubble, bubbleParams)
        updateBubbleText()
        applyPremiumAppearance()
    }

    private fun updateBubbleText() {
        val status = if (batteryText.isNotBlank()) "$batteryText  •  " else ""
        bubble?.text = "●  $status${latestText.take(38)}"
    }

    private fun applyPremiumAppearance(scheduleDim: Boolean = true) {
        val view = bubble ?: return
        val preferences = getSharedPreferences("island", MODE_PRIVATE)
        val accent = preferences.getInt("custom_accent_color", PremiumAccess.accentColor(this))
        val scale = PremiumAccess.islandScale(this)
        val bubbleScale = preferences.getFloat("bubble_size", 1f).coerceIn(0.8f, 1.4f)
        val opacity = preferences.getFloat("bubble_opacity", 1f).coerceIn(0.4f, 1f)
        view.background = rounded(Color.rgb(10, 13, 24), accent, 48f, 2)
        view.textSize = 11f * scale * bubbleScale
        val horizontal = (22 * scale * bubbleScale).toInt()
        val vertical = (13 * scale * bubbleScale).toInt()
        view.setPadding(horizontal, vertical, horizontal, vertical)

        pulseAnimator?.cancel()
        pulseAnimator = null
        val (lowAlpha, duration) = when (PremiumAccess.pulseStyle(this)) {
            "off" -> { view.alpha = opacity; if (scheduleDim) scheduleAutoDim(); return }
            "soft" -> 0.94f to 2300L
            "fast" -> 0.76f to 900L
            else -> 0.86f to 1450L
        }
        pulseAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, lowAlpha * opacity, opacity).apply {
            this.duration = duration
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
        if (scheduleDim) scheduleAutoDim()
    }

    private fun scheduleAutoDim() {
        mainHandler.removeCallbacks(autoDimTask)
        val preferences = getSharedPreferences("island", MODE_PRIVATE)
        if (BubbleAutoDimConfig.shouldSchedule(preferences.getBoolean("auto_dim_enabled", false), expanded, bubble != null)) {
            mainHandler.postDelayed(autoDimTask, BubbleAutoDimConfig.IDLE_DELAY_MS)
        }
    }

    private fun revealBubbleForInteraction() {
        mainHandler.removeCallbacks(autoDimTask)
        bubble?.animate()?.cancel()
        applyPremiumAppearance(scheduleDim = false)
    }


    private fun performBubbleGesture(gesture: String) {
        val preferences = getSharedPreferences("island", MODE_PRIVATE)
        val action = BubbleGestureConfig.resolve(
            gesture,
            preferences.getString("gesture_$gesture", null)
        )
        when (action) {
            "menu" -> togglePanel()
            "assistant" -> launchBubbleActivity(OfflineAssistantActivity::class.java)
            "media" -> launchBubbleActivity(MediaControlsActivity::class.java)
            "notifications" -> launchBubbleActivity(NotificationPreviewSettingsActivity::class.java)
            "workspace" -> launchBubbleActivity(FloatingWorkspaceActivity::class.java)
            "settings" -> launchBubbleActivity(IslandInteractionSettingsActivity::class.java)
            "tools" -> launchBubbleActivity(SmartToolsActivity::class.java)
            "none" -> Unit
        }
    }

    private fun launchBubbleActivity(activity: Class<*>) {
        runCatching {
            startActivity(Intent(this, activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure {
            Toast.makeText(this, "Fitur tidak tersedia pada perangkat ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePanel() {
        if (expanded) {
            closePanel()
            return
        }
        mainHandler.removeCallbacks(autoDimTask)

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
        addFavoriteActions(content)
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
            "System settings" to "system_settings",
            "Global navigation controls" to "device_controls"
        ))
        addSection(content, "06  MY FLOATING APPS", listOf(
            "Installed app collection" to "installed_apps",
            "Floating workspace (PiP)" to "workspace",
            "Offline notes" to "notes",
            "Offline calculator" to "local_calculator",
            "File manager (folder pilihan)" to "file_manager",
            "Mini floating notes" to "mini_notes",
            "Mini floating calculator" to "mini_calculator",
            "Mini floating timer" to "mini_timer"
        ))
        addSection(content, "07  OFFLINE INTELLIGENCE", listOf(
            "Offline utility assistant" to "offline_assistant",
            "Notification privacy" to "notification_privacy",
            "Island interaction settings" to "island_settings"
        ))
        addSection(content, "08  MEDIA & DEVICE TOOLS", listOf(
            "Media session controls" to "media_controls",
            "QR / barcode scanner" to "qr_scanner",
            "Image / PDF preview" to "document_preview",
            "Live sensor kit" to "sensor_kit"
        ))
        addSection(content, "09  SMART TOOLBOX", listOf(
            "Converter, counter, stopwatch & timer" to "smart_tools",
            "Unit converter" to "smart_tools",
            "Pomodoro 25 menit" to "smart_tools"
        ))
        addSection(content, "10  BACKUP & DATA", listOf(
            "Encrypted backup & restore" to "backup_manager",
            "Persistent reminders" to "reminders",
            "Clipboard history" to "clipboard_history"
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
        val heightFactor = if (PremiumAccess.compactPanel(this)) .64f else .76f
        val height = (metrics.heightPixels * heightFactor).toInt()
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


    private fun addFavoriteActions(root: LinearLayout) {
        root.addView(label("QUICK FAVORITES", 9f, true).apply {
            setTextColor(Color.rgb(88, 220, 255))
            setPadding(0, 12, 0, 7)
        })
        val preferences = getSharedPreferences("island", MODE_PRIVATE)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        repeat(BubbleFavoriteConfig.SLOT_COUNT) { slot ->
            val key = BubbleFavoriteConfig.resolve(
                slot,
                preferences.getString("favorite_${slot + 1}", null)
            )
            val option = BubbleFavoriteConfig.optionFor(key)
            val item = TextView(this).apply {
                text = option.shortLabel
                textSize = 9f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                minHeight = (46 * resources.displayMetrics.density).toInt()
                background = rounded(
                    Color.rgb(27, 39, 65),
                    Color.rgb(70, 103, 155),
                    13f,
                    1
                )
                contentDescription = option.label
                setOnClickListener {
                    closePanel()
                    launchFavoriteAction(option.key, option.label)
                }
            }
            row.addView(item, LinearLayout.LayoutParams(0, -2, 1f).apply {
                marginEnd = (5 * resources.displayMetrics.density).toInt()
            })
        }
        root.addView(row)
    }

    private fun launchFavoriteAction(key: String, label: String) {
        val miniKind = when (key) {
            "mini_notes" -> MiniWindowKind.NOTES
            "mini_calculator" -> MiniWindowKind.CALCULATOR
            "mini_timer" -> MiniWindowKind.TIMER
            else -> null
        }
        if (miniKind != null) {
            if (::miniWindows.isInitialized) miniWindows.open(miniKind)
            else Toast.makeText(this, "Floating Window Manager belum siap.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = when (key) {
            "assistant" -> Intent(this, OfflineAssistantActivity::class.java)
            "notes" -> Intent(this, OfflineNotesActivity::class.java)
            "calculator" -> Intent(this, OfflineCalculatorActivity::class.java)
            "apps" -> Intent(this, InstalledAppsActivity::class.java)
            "media" -> Intent(this, MediaControlsActivity::class.java)
            "qr" -> Intent(this, QrScannerActivity::class.java)
            "documents" -> Intent(this, DocumentPreviewActivity::class.java)
            "sensors" -> Intent(this, SensorKitActivity::class.java)
            "workspace" -> Intent(this, FloatingWorkspaceActivity::class.java)
            "settings" -> Intent(this, IslandInteractionSettingsActivity::class.java)
            "tools" -> Intent(this, SmartToolsActivity::class.java)
            else -> null
        }
        if (intent == null) {
            Toast.makeText(this, "Favorite action is unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure {
            Toast.makeText(this, "$label is not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun closePanel() {
        panel?.let { runCatching { windowManager.removeView(it) } }
        panel = null
        expanded = false
        scheduleAutoDim()
    }

    private fun addSection(
        root: LinearLayout,
        title: String,
        actions: List<Pair<String, String>>
    ) {
        val actionItems = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        val header = label("▸  $title", 10f, true).apply {
            setTextColor(Color.rgb(88, 220, 255))
            setPadding(13, 14, 10, 12)
            background = rounded(
                Color.rgb(19, 29, 49),
                Color.rgb(42, 63, 94),
                13f,
                1
            )
            isClickable = true
            isFocusable = true
            contentDescription = "$title, kategori tertutup. Ketuk untuk membuka."
            setOnClickListener {
                val isOpening = actionItems.visibility != View.VISIBLE
                actionItems.visibility = if (isOpening) View.VISIBLE else View.GONE
                text = (if (isOpening) "▾  " else "▸  ") + title
                contentDescription = "$title, kategori ${if (isOpening) "terbuka" else "tertutup"}. Ketuk untuk ${if (isOpening) "menutup" else "membuka"}."
            }
        }
        root.addView(header, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = (10 * resources.displayMetrics.density).toInt()
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
                    val miniKind = when (key) {
                        "mini_notes" -> MiniWindowKind.NOTES
                        "mini_calculator" -> MiniWindowKind.CALCULATOR
                        "mini_timer" -> MiniWindowKind.TIMER
                        else -> null
                    }
                    if (miniKind != null) {
                        closePanel()
                        miniWindows.open(miniKind)
                        return@setOnClickListener
                    }
                    val internalIntent = when (key) {
                        "installed_apps" -> Intent(this@FloatingIslandService, InstalledAppsActivity::class.java)
                        "workspace" -> Intent(this@FloatingIslandService, FloatingWorkspaceActivity::class.java)
                        "notes" -> Intent(this@FloatingIslandService, OfflineNotesActivity::class.java)
                        "local_calculator" -> Intent(this@FloatingIslandService, OfflineCalculatorActivity::class.java)
                        "offline_assistant" -> Intent(this@FloatingIslandService, OfflineAssistantActivity::class.java)
                        "notification_privacy" -> Intent(this@FloatingIslandService, NotificationPreviewSettingsActivity::class.java)
                        "island_settings" -> Intent(this@FloatingIslandService, IslandInteractionSettingsActivity::class.java)
                        "media_controls" -> Intent(this@FloatingIslandService, MediaControlsActivity::class.java)
                        "qr_scanner" -> Intent(this@FloatingIslandService, QrScannerActivity::class.java)
                        "document_preview" -> Intent(this@FloatingIslandService, DocumentPreviewActivity::class.java)
                        "sensor_kit" -> Intent(this@FloatingIslandService, SensorKitActivity::class.java)
                        "smart_tools" -> Intent(this@FloatingIslandService, SmartToolsActivity::class.java)
                        "file_manager" -> Intent(this@FloatingIslandService, FileManagerActivity::class.java)
                        "device_controls" -> Intent(this@FloatingIslandService, DeviceControlActivity::class.java)
                        "backup_manager" -> Intent(this@FloatingIslandService, BackupActivity::class.java)
                        "reminders" -> Intent(this@FloatingIslandService, ReminderActivity::class.java)
                        "clipboard_history" -> Intent(this@FloatingIslandService, ClipboardHistoryActivity::class.java)
                        else -> null
                    }
                    val intent = internalIntent ?: UtilityIntents.create(key, packageName)
                    if (intent == null) {
                        Toast.makeText(this@FloatingIslandService, "Tool unavailable", Toast.LENGTH_SHORT).show()
                    } else {
                        closePanel()
                        runCatching {
                            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }.onFailure {
                            Toast.makeText(
                                this@FloatingIslandService,
                                "$name is not available on this device",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            actionItems.addView(item, LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = (6 * resources.displayMetrics.density).toInt()
            })
        }
        root.addView(actionItems, LinearLayout.LayoutParams(-1, -2))
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
        private var longPressTriggered = false
        private var swipeGesture: String? = null
        private var activeView: View? = null
        private var lastTapAt = 0L
        private var pendingTap: Runnable? = null

        private val longPressTask = Runnable {
            if (!dragged && activeView != null) {
                longPressTriggered = true
                val hapticsEnabled = getSharedPreferences("island", MODE_PRIVATE)
                    .getBoolean("haptic_enabled", false)
                if (hapticsEnabled) {
                    activeView?.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                }
                performBubbleGesture("long_press")
            }
        }

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    revealBubbleForInteraction()
                    downX = event.rawX
                    downY = event.rawY
                    startX = bubbleParams.x
                    startY = bubbleParams.y
                    dragged = false
                    longPressTriggered = false
                    swipeGesture = null
                    activeView = view
                    mainHandler.removeCallbacks(longPressTask)
                    mainHandler.postDelayed(
                        longPressTask,
                        ViewConfiguration.getLongPressTimeout().toLong()
                    )
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downX).toInt()
                    val dy = (event.rawY - downY).toInt()
                    val configuration = ViewConfiguration.get(this@FloatingIslandService)
                    if (abs(dx) + abs(dy) > configuration.scaledTouchSlop) {
                        dragged = true
                        mainHandler.removeCallbacks(longPressTask)
                    }
                    if (dragged) {
                        swipeGesture = detectConfiguredSwipe(dx, dy)
                        if (swipeGesture == null) {
                            bubbleParams.x = startX + dx
                            bubbleParams.y = startY + dy
                            runCatching { windowManager.updateViewLayout(bubble, bubbleParams) }
                        }
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    mainHandler.removeCallbacks(longPressTask)
                    val gesture = swipeGesture
                    if (gesture != null) {
                        bubbleParams.x = startX
                        bubbleParams.y = startY
                        runCatching { windowManager.updateViewLayout(bubble, bubbleParams) }
                        performBubbleGesture(gesture)
                    } else if (dragged) {
                        val preferences = getSharedPreferences("island", MODE_PRIVATE)
                        if (preferences.getBoolean("edge_docking", true)) {
                            val width = resources.displayMetrics.widthPixels
                            val center = width / 2
                            val inset = (12 * resources.displayMetrics.density).toInt()
                            bubbleParams.x = if (event.rawX < width / 2f) {
                                -center + view.width / 2 + inset
                            } else {
                                center - view.width / 2 - inset
                            }
                            runCatching { windowManager.updateViewLayout(bubble, bubbleParams) }
                        }
                        if (preferences.getBoolean("haptic_enabled", false)) {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                        }
                        preferences.edit().putInt("bubble_x", bubbleParams.x)
                            .putInt("bubble_y", bubbleParams.y).apply()
                    } else if (!longPressTriggered) {
                        dispatchTap(view)
                    }
                    activeView = null
                    scheduleAutoDim()
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    mainHandler.removeCallbacks(longPressTask)
                    pendingTap?.let { mainHandler.removeCallbacks(it) }
                    pendingTap = null
                    lastTapAt = 0L
                    if (dragged) {
                        bubbleParams.x = startX
                        bubbleParams.y = startY
                        runCatching { windowManager.updateViewLayout(bubble, bubbleParams) }
                    }
                    activeView = null
                    scheduleAutoDim()
                    return true
                }
            }
            return false
        }

        private fun dispatchTap(view: View) {
            val now = SystemClock.uptimeMillis()
            val doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout().toLong()
            val previousTap = pendingTap
            if (previousTap != null && now - lastTapAt <= doubleTapTimeout) {
                mainHandler.removeCallbacks(previousTap)
                pendingTap = null
                lastTapAt = 0L
                performBubbleGesture("double_tap")
                return
            }
            if (previousTap != null) {
                mainHandler.removeCallbacks(previousTap)
                previousTap.run()
            }
            lastTapAt = now
            val task = Runnable {
                pendingTap = null
                lastTapAt = 0L
                view.performClick()
            }
            pendingTap = task
            mainHandler.postDelayed(task, doubleTapTimeout)
        }

        private fun detectConfiguredSwipe(dx: Int, dy: Int): String? {
            val threshold = (42 * resources.displayMetrics.density).toInt()
            val candidate = when {
                abs(dx) >= threshold && abs(dx) > abs(dy) * 1.25f ->
                    if (dx > 0) "swipe_right" else "swipe_left"
                abs(dy) >= threshold && abs(dy) > abs(dx) * 1.25f ->
                    if (dy > 0) "swipe_down" else "swipe_up"
                else -> null
            } ?: return null
            val preferences = getSharedPreferences("island", MODE_PRIVATE)
            val action = BubbleGestureConfig.resolve(
                candidate,
                preferences.getString("gesture_$candidate", null)
            )
            return candidate.takeIf { action != "none" }
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

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, FloatingIslandService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getForegroundService(
            this,
            4101,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, "island_service")
            .setSmallIcon(R.drawable.ic_stat_island)
            .setContentTitle("Dynamic Island Ultra is active")
            .setContentText("Floating island and assistive bubble are running")
            .addAction(R.drawable.ic_stat_island, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(notificationReceiver) }
        runCatching { unregisterReceiver(batteryReceiver) }
        runCatching { unregisterReceiver(appearanceReceiver) }
        runCatching { unregisterReceiver(activityCardReceiver) }
        mainHandler.removeCallbacksAndMessages(null)
        pulseAnimator?.cancel()
        pulseAnimator = null
        bubble?.let { runCatching { windowManager.removeView(it) } }
        panel?.let { runCatching { windowManager.removeView(it) } }
        if (::miniWindows.isInitialized) miniWindows.closeAll()
        bubble = null
        panel = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_NOTIFICATION_PREVIEW =
            "com.adebojel.dynamicislandultra.NOTIFICATION_PREVIEW"
        const val ACTION_STOP = "com.adebojel.dynamicislandultra.STOP_ISLAND"
        const val ACTION_APPEARANCE_CHANGED = "com.adebojel.dynamicislandultra.PREMIUM_APPEARANCE_CHANGED"
        const val ACTION_CALL_ACTIVITY = "com.adebojel.dynamicislandultra.CALL_ACTIVITY"
    }
}
