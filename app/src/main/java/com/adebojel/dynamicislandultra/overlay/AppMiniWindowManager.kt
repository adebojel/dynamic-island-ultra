package com.adebojel.dynamicislandultra.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max
import kotlin.math.min

internal enum class MiniWindowKind(val title: String) {
    NOTES("Floating Notes"),
    CALCULATOR("Mini Calculator"),
    TIMER("Mini Timer")
}

internal object MiniWindowMath {
    fun calculate(left: Double, operator: String, right: Double): Double? {
        if (!left.isFinite() || !right.isFinite()) return null
        val result = when (operator) {
            "+" -> left + right
            "−", "-" -> left - right
            "×", "*" -> left * right
            "÷", "/" -> if (right == 0.0) return null else left / right
            else -> return null
        }
        return result.takeIf { it.isFinite() }
    }
}

/** App-owned TYPE_APPLICATION_OVERLAY mini-windows; never attempts to embed third-party apps. */
internal class AppMiniWindowManager(
    private val context: Context,
    private val windowManager: WindowManager
) {
    private class Entry(
        val id: Int,
        val root: LinearLayout,
        val body: LinearLayout,
        val params: WindowManager.LayoutParams,
        val fullWidth: Int,
        val fullHeight: Int,
        var minimized: Boolean = false,
        var large: Boolean = false,
        var timer: CountDownTimer? = null
    )

    private val windows = linkedMapOf<Int, Entry>()
    private var nextId = 1
    private val density get() = context.resources.displayMetrics.density

    fun open(kind: MiniWindowKind) {
        if (windows.size >= MAX_WINDOWS) {
            Toast.makeText(context, "Maksimal $MAX_WINDOWS jendela mini terbuka.", Toast.LENGTH_SHORT).show()
            return
        }
        val id = nextId++
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.rgb(13, 20, 36), Color.rgb(67, 208, 255), 20, 2)
            elevation = dp(14).toFloat()
        }
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(5), dp(5), dp(5))
            background = rounded(Color.rgb(22, 34, 55), Color.rgb(55, 83, 122), 17, 1)
        }
        val title = TextView(context).apply {
            text = kind.title
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
        }
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        val width = min(dp(300), context.resources.displayMetrics.widthPixels - dp(24))
        val height = dp(270)
        val params = WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(12 + (windows.size * 18))
            y = dp(105 + (windows.size * 30))
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }
        root.addView(header, LinearLayout.LayoutParams(-1, dp(42)))
        root.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
        val entry = Entry(id, root, body, params, width, height)
        title.layoutParams = LinearLayout.LayoutParams(0, -1, 1f)
        header.addView(title)
        header.addView(actionButton("−", "Minimize or restore") {
            entry.minimized = !entry.minimized
            body.visibility = if (entry.minimized) View.GONE else View.VISIBLE
            params.height = if (entry.minimized) dp(46) else if (entry.large) dp(420) else entry.fullHeight
            update(entry)
        })
        header.addView(actionButton("↕", "Resize window") {
            entry.large = !entry.large
            if (!entry.minimized) params.height = if (entry.large) dp(420) else entry.fullHeight
            params.width = if (entry.large) min(dp(360), context.resources.displayMetrics.widthPixels - dp(16)) else entry.fullWidth
            update(entry)
        })
        header.addView(actionButton("×", "Close window") { close(id) })
        attachDrag(title, entry)
        buildTool(kind, entry)
        windows[id] = entry
        runCatching { windowManager.addView(root, params) }
            .onFailure {
                windows.remove(id)
                entry.timer?.cancel()
                Toast.makeText(context, "Tidak dapat membuka jendela overlay: ${it.message ?: "izin overlay"}", Toast.LENGTH_LONG).show()
            }
    }

    fun closeAll() {
        windows.keys.toList().forEach(::close)
    }

    private fun buildTool(kind: MiniWindowKind, entry: Entry) {
        when (kind) {
            MiniWindowKind.NOTES -> buildNotes(entry)
            MiniWindowKind.CALCULATOR -> buildCalculator(entry)
            MiniWindowKind.TIMER -> buildTimer(entry)
        }
    }

    private fun buildNotes(entry: Entry) {
        val prefs = context.getSharedPreferences("mini_windows", Context.MODE_PRIVATE)
        val editor = EditText(context).apply {
            setText(prefs.getString("notes", "").orEmpty())
            hint = "Tulis catatan singkat…"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            textSize = 13f
            minLines = 5
            maxLines = 9
            gravity = Gravity.TOP
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = rounded(Color.rgb(24, 34, 53), Color.rgb(63, 93, 136), 12, 1)
            imeOptions = EditorInfo.IME_ACTION_NONE
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    prefs.edit().putString("notes", s?.toString().orEmpty()).apply()
                }
                override fun afterTextChanged(s: Editable?) = Unit
            })
        }
        entry.body.addView(editor, LinearLayout.LayoutParams(-1, -1))
    }

    private fun buildCalculator(entry: Entry) {
        val fields = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val left = numberField("0")
        val right = numberField("0")
        val operator = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listOf("+", "−", "×", "÷"))
        }
        fields.addView(left, LinearLayout.LayoutParams(0, dp(48), 1f))
        fields.addView(operator, LinearLayout.LayoutParams(dp(52), dp(48)))
        fields.addView(right, LinearLayout.LayoutParams(0, dp(48), 1f))
        val result = toolLabel("Hasil akan muncul di sini", 14f)
        val calculate = toolButton("Hitung") {
            val a = left.text.toString().replace(',', '.').toDoubleOrNull()
            val b = right.text.toString().replace(',', '.').toDoubleOrNull()
            val value = if (a != null && b != null) MiniWindowMath.calculate(a, operator.selectedItem.toString(), b) else null
            result.text = value?.let { "Hasil: ${"%.8f".format(it).trimEnd('0').trimEnd('.')}" }
                ?: "Input tidak valid atau pembagian nol."
        }
        entry.body.addView(fields)
        entry.body.addView(result, LinearLayout.LayoutParams(-1, dp(50)))
        entry.body.addView(calculate, LinearLayout.LayoutParams(-1, dp(42)))
    }

    private fun buildTimer(entry: Entry) {
        val minutes = numberField("5")
        val display = toolLabel("Siap", 16f)
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(minutes, LinearLayout.LayoutParams(0, dp(48), 1f))
        row.addView(toolLabel("menit", 12f), LinearLayout.LayoutParams(dp(55), dp(48)))
        val start = toolButton("Mulai countdown") {
            val value = minutes.text.toString().toIntOrNull()
            if (value == null || value !in 1..240) {
                display.text = "Masukkan 1–240 menit."
            } else {
                entry.timer?.cancel()
                entry.timer = object : CountDownTimer(value * 60_000L, 1_000L) {
                    override fun onTick(millisUntilFinished: Long) {
                        val total = millisUntilFinished / 1_000L
                        display.text = "Sisa %02d:%02d".format(total / 60, total % 60)
                    }
                    override fun onFinish() {
                        display.text = "WAKTU HABIS"
                        Toast.makeText(context, "Timer mini selesai.", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            }
        }
        val stop = toolButton("Batalkan timer") {
            entry.timer?.cancel()
            entry.timer = null
            display.text = "Timer dibatalkan."
        }
        entry.body.addView(row)
        entry.body.addView(display, LinearLayout.LayoutParams(-1, dp(54)))
        entry.body.addView(start, LinearLayout.LayoutParams(-1, dp(42)))
        entry.body.addView(stop, LinearLayout.LayoutParams(-1, dp(42)))
    }

    private fun attachDrag(handle: View, entry: Entry) {
        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        handle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = entry.params.x
                    startY = entry.params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    entry.params.x = max(0, startX + (event.rawX - downX).toInt())
                    entry.params.y = max(0, startY + (event.rawY - downY).toInt())
                    update(entry)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> true
                else -> false
            }
        }
    }

    private fun update(entry: Entry) {
        runCatching { windowManager.updateViewLayout(entry.root, entry.params) }
    }

    private fun close(id: Int) {
        val entry = windows.remove(id) ?: return
        entry.timer?.cancel()
        runCatching { windowManager.removeView(entry.root) }
    }

    private fun actionButton(text: String, description: String, action: () -> Unit) =
        TextView(context).apply {
            this.text = text
            contentDescription = description
            gravity = Gravity.CENTER
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(dp(7), 0, dp(7), 0)
            setOnClickListener { action() }
        }

    private fun numberField(value: String) = EditText(context).apply {
        setText(value)
        setTextColor(Color.WHITE)
        setHintTextColor(Color.LTGRAY)
        textSize = 14f
        inputType = android.text.InputType.TYPE_CLASS_NUMBER or
            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
            android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
        setPadding(dp(6), dp(4), dp(6), dp(4))
        background = rounded(Color.rgb(24, 34, 53), Color.rgb(63, 93, 136), 9, 1)
    }

    private fun toolLabel(text: String, size: Float) = TextView(context).apply {
        this.text = text
        textSize = size
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun toolButton(text: String, action: () -> Unit) =
        Button(context).apply { this.text = text; textSize = 11f; setOnClickListener { action() } }

    private fun rounded(fill: Int, stroke: Int, radiusDp: Int, strokeDp: Int) =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radiusDp).toFloat()
            setStroke(dp(strokeDp), stroke)
        }

    private fun dp(value: Int): Int = (value * density).toInt()

    companion object { private const val MAX_WINDOWS = 4 }
}
