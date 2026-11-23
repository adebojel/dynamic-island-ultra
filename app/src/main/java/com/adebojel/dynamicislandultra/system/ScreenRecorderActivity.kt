package com.adebojel.dynamicislandultra.system

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val RecorderNight = Color(0xFF0B1222)
private val RecorderCyan = Color(0xFF43E8FF)

class ScreenRecorderActivity : ComponentActivity() {
    private var captureResultCode: Int = 0
    private var captureData: Intent? = null
    private var status by mutableStateOf("Perekaman dimulai hanya setelah konfirmasi Screen Capture Android.")
    private val projectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val captureRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            captureResultCode = result.resultCode
            captureData = data
            val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
            saveRecording.launch("dynamic-island-recording-$stamp.mp4")
        } else {
            status = "Izin perekaman layar dibatalkan."
        }
    }

    private val saveRecording = registerForActivityResult(
        ActivityResultContracts.CreateDocument("video/mp4")
    ) { uri ->
        if (uri == null) {
            status = "Lokasi penyimpanan tidak dipilih."
            captureData = null
            return@registerForActivityResult
        }
        startRecording(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = RecorderCyan, background = RecorderNight, surface = Color(0xFF17243A))) {
                Column(
                    Modifier.fillMaxSize().background(RecorderNight).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("SCREEN RECORDER", color = RecorderCyan, fontSize = 20.sp)
                    Text(
                        "Merekam layar ke file MP4 setelah Anda menyetujui dialog Screen Capture Android dan memilih lokasi file. Modul ini tidak merekam suara atau mikrofon.",
                        color = Color.LightGray, fontSize = 12.sp
                    )
                    Button(
                        onClick = { captureRequest.launch(projectionManager.createScreenCaptureIntent()) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Mulai rekam layar") }
                    Surface(color = Color(0xFF17243A), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(status, color = Color.White, modifier = Modifier.padding(14.dp), fontSize = 12.sp)
                    }
                    Text(
                        "Saat aktif, layanan foreground menampilkan notifikasi dengan tombol Stop. Android dapat mengakhiri perekaman kapan saja. Rekam hanya konten yang Anda berhak rekam; aplikasi tidak menyembunyikan indikator perekaman sistem.",
                        color = Color(0xFF9BA8C1), fontSize = 10.sp
                    )
                }
            }
        }
    }

    private fun startRecording(uri: Uri) {
        val data = captureData ?: run {
            status = "Token izin Screen Capture tidak tersedia."
            return
        }
        val intent = Intent(this, ScreenRecordingService::class.java).apply {
            action = ScreenRecordingService.ACTION_START
            putExtra(ScreenRecordingService.EXTRA_RESULT_CODE, captureResultCode)
            putExtra(ScreenRecordingService.EXTRA_RESULT_DATA, data)
            putExtra(ScreenRecordingService.EXTRA_OUTPUT_URI, uri.toString())
        }
        runCatching {
            ContextCompat.startForegroundService(this, intent)
        }.onSuccess {
            status = "Permintaan perekaman dikirim. Gunakan notifikasi untuk menghentikan rekaman."
            Toast.makeText(this, "Screen Recorder dimulai.", Toast.LENGTH_SHORT).show()
        }.onFailure {
            status = "Perekaman tidak dapat dimulai: \${it.message ?: "kesalahan sistem"}"
        }
        captureData = null
    }
}
