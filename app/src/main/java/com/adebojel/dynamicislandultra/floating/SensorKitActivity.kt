package com.adebojel.dynamicislandultra.floating

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class SensorKitActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var sensors: List<Sensor>
    private val readings = mutableStateMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val types = listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_LIGHT, Sensor.TYPE_PROXIMITY, Sensor.TYPE_PRESSURE)
        sensors = types.mapNotNull { sensorManager.getDefaultSensor(it) }
        sensors.forEach { readings[it.type] = "Menunggu data sensor…" }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                Column(Modifier.fillMaxSize().background(Color(0xFF0B1222)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DEVICE SENSOR KIT", color = Color(0xFF43E8FF), fontSize = 18.sp)
                    Text("Pembacaan berlangsung hanya ketika layar ini terbuka. Data tidak direkam atau dikirim.",
                        color = Color.LightGray, fontSize = 11.sp)
                    if (sensors.isEmpty()) Text("Sensor yang didukung tidak ditemukan.", color = Color.White)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        items(sensors, key = { it.type }) { sensor ->
                            Surface(color = Color(0xFF18243A), shape = MaterialTheme.shapes.medium) {
                                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(sensor.name, color = Color.White, fontSize = 14.sp)
                                    Text(sensor.vendor, color = Color.LightGray, fontSize = 10.sp)
                                    Text(readings[sensor.type] ?: "Belum tersedia", color = Color(0xFF43E8FF), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensors.forEach { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val unit = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> "m/s²"
            Sensor.TYPE_GYROSCOPE -> "rad/s"
            Sensor.TYPE_MAGNETIC_FIELD -> "µT"
            Sensor.TYPE_LIGHT -> "lux"
            Sensor.TYPE_PROXIMITY -> "cm"
            Sensor.TYPE_PRESSURE -> "hPa"
            else -> ""
        }
        readings[event.sensor.type] = event.values.joinToString(" • ") {
            String.format(Locale.ROOT, "%.2f", it)
        } + " $unit"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
