package com.adebojel.dynamicislandultra.floating

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val ToolNight = Color(0xFF0B1222)
private val ToolCyan = Color(0xFF43E8FF)

class SmartToolsActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val accelerometer by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private var acceleration by mutableStateOf<FloatArray?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        setContent { SmartToolsScreen(acceleration) }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            acceleration = event.values.copyOf()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

@Composable
private fun SmartToolsScreen(acceleration: FloatArray?) {
    var selected by remember { mutableStateOf("converter") }
    MaterialTheme(colorScheme = darkColorScheme(primary = ToolCyan, background = ToolNight, surface = Color(0xFF142039))) {
        Column(
            Modifier.fillMaxSize().background(ToolNight).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("SMART TOOLBOX", color = ToolCyan, fontSize = 20.sp)
            Text("Alat lokal tanpa akun, cloud, atau permintaan izin tambahan.", color = Color.LightGray, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("converter" to "Konversi", "counter" to "Penghitung", "stopwatch" to "Stopwatch", "timer" to "Timer", "level" to "Spirit Level").forEach { (key, label) ->
                    if (selected == key) Button(onClick = { selected = key }) { Text(label) }
                    else OutlinedButton(onClick = { selected = key }) { Text(label) }
                }
            }
            when (selected) {
                "converter" -> UnitConverterPanel()
                "counter" -> CounterPanel()
                "stopwatch" -> StopwatchPanel()
                "timer" -> TimerPanel()
                "level" -> LevelPanel(acceleration)
            }
            Text(
                "Catatan: stopwatch dan timer berjalan sebagai alat sesi dalam aplikasi. Keduanya bukan alarm sistem dan tidak menjamin notifikasi setelah aplikasi ditutup paksa.",
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
@Composable
private fun LevelPanel(acceleration: FloatArray?) {
    val angles = acceleration?.let { LevelSensorMath.angles(it) }
    var zeroPitch by remember { mutableFloatStateOf(0f) }
    var zeroRoll by remember { mutableFloatStateOf(0f) }
    Text("SPIRIT LEVEL • 2 AXIS", color = Color.White, fontSize = 16.sp)
    if (acceleration == null || angles == null) {
        Surface(color = Color(0xFF172A45), shape = RoundedCornerShape(16.dp)) {
            Text(
                "Accelerometer tidak tersedia atau belum mengirim pembacaan pada perangkat ini.",
                color = Color.LightGray, modifier = Modifier.padding(16.dp)
            )
        }
        return
    }
    val pitch = angles.first - zeroPitch
    val roll = angles.second - zeroRoll
    val status = when {
        kotlin.math.abs(pitch) <= 0.5f && kotlin.math.abs(roll) <= 0.5f -> "RATA"
        kotlin.math.abs(pitch) <= 2f && kotlin.math.abs(roll) <= 2f -> "HAMPIR RATA"
        else -> "MIRING"
    }
    Surface(color = Color(0xFF172A45), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(status, color = ToolCyan, fontSize = 20.sp)
            Canvas(Modifier.fillMaxWidth().height(190.dp)) {
                val radius = size.minDimension * 0.34f
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(Color(0xFF6A7A96), radius, center, style = Stroke(width = 3.dp.toPx()))
                drawLine(Color(0xFF6A7A96), Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), 2.dp.toPx())
                drawLine(Color(0xFF6A7A96), Offset(center.x, center.y - radius), Offset(center.x, center.y + radius), 2.dp.toPx())
                val maxAngle = 45f
                val dx = (roll.coerceIn(-maxAngle, maxAngle) / maxAngle) * radius
                val dy = (pitch.coerceIn(-maxAngle, maxAngle) / maxAngle) * radius
                drawCircle(ToolCyan, 11.dp.toPx(), Offset(center.x + dx, center.y + dy))
            }
            Text("Pitch: %.1f°".format(pitch), color = Color.White, fontSize = 17.sp)
            Text("Roll: %.1f°".format(roll), color = Color.White, fontSize = 17.sp)
            Text(
                "Pembacaan berasal dari accelerometer. Kalibrasi nol menyimpan posisi perangkat saat ini sebagai titik acuan lokal.",
                color = Color.LightGray, fontSize = 11.sp
            )
            OutlinedButton(
                onClick = { zeroPitch = angles.first; zeroRoll = angles.second },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Kalibrasi posisi saat ini sebagai 0°") }
            OutlinedButton(
                onClick = { zeroPitch = 0f; zeroRoll = 0f },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset kalibrasi") }
        }
    }
}

private fun UnitConverterPanel() {
    var category by remember { mutableStateOf(LocalUnitConverter.groups.first().key) }
    val group = LocalUnitConverter.group(category)
    var input by remember { mutableStateOf("1") }
    var from by remember(category) { mutableStateOf(group.units.first().id) }
    var to by remember(category) { mutableStateOf(group.units.getOrElse(1) { group.units.first() }.id) }
    val converted = input.replace(',', '.').toDoubleOrNull()?.let {
        LocalUnitConverter.convert(it, category, from, to)
    }

    Text("KONVERSI SATUAN", color = Color.White, fontSize = 16.sp)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UnitChoice("Kategori", LocalUnitConverter.groups.map { it.key to it.label }, category) { category = it }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nilai") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        UnitChoice("Dari", group.units.map { it.id to it.label }, from) { from = it }
        UnitChoice("Ke", group.units.map { it.id to it.label }, to) { to = it }
        Surface(color = Color(0xFF172A45), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("HASIL", color = ToolCyan, fontSize = 11.sp)
                Text(converted?.let { LocalUnitConverter.format(it) } ?: "Masukkan nilai yang valid", color = Color.White, fontSize = 24.sp)
            }
        }
    }
}

@Composable
private fun UnitChoice(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: options.first().second
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = Color.LightGray, fontSize = 11.sp)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedLabel)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option.second) }, onClick = {
                        onSelected(option.first)
                        expanded = false
                    })
                }
            }
        }
    }
}

@Composable
private fun CounterPanel() {
    var count by remember { mutableLongStateOf(0L) }
    Text("PENGHITUNG", color = Color.White, fontSize = 16.sp)
    Surface(color = Color(0xFF172A45), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Text(count.toString(), color = Color.White, fontSize = 44.sp, modifier = Modifier.fillMaxWidth().padding(24.dp))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { count-- }, modifier = Modifier.weight(1f)) { Text("− 1") }
        Button(onClick = { count++ }, modifier = Modifier.weight(1f)) { Text("+ 1") }
        OutlinedButton(onClick = { count = 0L }, modifier = Modifier.weight(1f)) { Text("Reset") }
    }
}

@Composable
private fun StopwatchPanel() {
    var running by remember { mutableStateOf(false) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var accumulated by remember { mutableLongStateOf(0L) }
    var elapsed by remember { mutableLongStateOf(0L) }
    var lastLapAt by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(running) {
        while (running) {
            elapsed = accumulated + (SystemClock.elapsedRealtime() - startedAt)
            delay(50)
        }
    }
    Text("STOPWATCH", color = Color.White, fontSize = 16.sp)
    Text(formatDuration(elapsed), color = ToolCyan, fontSize = 32.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            if (!running) {
                startedAt = SystemClock.elapsedRealtime()
                lastLapAt = accumulated
                running = true
            } else {
                accumulated += SystemClock.elapsedRealtime() - startedAt
                elapsed = accumulated
                running = false
            }
        }) { Text(if (running) "Jeda" else "Mulai") }
        OutlinedButton(
            onClick = {
                val total = accumulated + (if (running) SystemClock.elapsedRealtime() - startedAt else 0L)
                val lap = (total - lastLapAt).coerceAtLeast(0L)
                laps.add(0, lap)
                lastLapAt = total
            },
            enabled = running
        ) { Text("Putaran") }
        OutlinedButton(onClick = {
            running = false
            startedAt = 0L
            accumulated = 0L
            elapsed = 0L
            lastLapAt = 0L
            laps.clear()
        }) { Text("Reset") }
    }
    if (laps.isNotEmpty()) {
        Text("PUTARAN", color = ToolCyan, fontSize = 12.sp)
        laps.forEachIndexed { index, lap ->
            Text("Putaran ${laps.size - index}: ${formatDuration(lap)}", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TimerPanel() {
    var secondsInput by remember { mutableStateOf("300") }
    var remaining by remember { mutableIntStateOf(300) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1_000)
            remaining = (remaining - 1).coerceAtLeast(0)
        }
        if (remaining == 0) running = false
    }
    Text("COUNTDOWN TIMER", color = Color.White, fontSize = 16.sp)
    OutlinedTextField(
        value = secondsInput,
        onValueChange = { secondsInput = it.filter(Char::isDigit).take(6) },
        label = { Text("Durasi dalam detik") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
    Text(formatDuration(remaining * 1_000L), color = ToolCyan, fontSize = 32.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            if (running) running = false
            else {
                if (remaining <= 0) remaining = secondsInput.toIntOrNull()?.coerceIn(1, 86_400) ?: 300
                running = remaining > 0
            }
        }) { Text(if (running) "Jeda" else "Mulai") }
        OutlinedButton(onClick = {
            running = false
            remaining = secondsInput.toIntOrNull()?.coerceIn(1, 86_400) ?: 300
        }) { Text("Reset") }
        OutlinedButton(onClick = {
            secondsInput = "1500"
            remaining = 1500
            running = false
        }) { Text("Pomodoro 25m") }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val safe = milliseconds.coerceAtLeast(0L)
    val seconds = safe / 1_000L
    val hours = seconds / 3_600
    val minutes = (seconds % 3_600) / 60
    val remainder = seconds % 60
    val centiseconds = (safe % 1_000) / 10
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, remainder)
    else "%02d:%02d.%02d".format(minutes, remainder, centiseconds)
}
