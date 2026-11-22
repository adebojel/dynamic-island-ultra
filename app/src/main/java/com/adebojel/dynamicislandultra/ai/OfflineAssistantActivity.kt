package com.adebojel.dynamicislandultra.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.adebojel.dynamicislandultra.browser.DuckDuckGoBrowserActivity
import com.adebojel.dynamicislandultra.update.AppUpdateActivity
import com.adebojel.dynamicislandultra.floating.InstalledAppsActivity
import com.adebojel.dynamicislandultra.floating.OfflineCalculatorActivity
import com.adebojel.dynamicislandultra.floating.OfflineNotesActivity
import com.adebojel.dynamicislandultra.floating.FloatingWorkspaceActivity
import com.adebojel.dynamicislandultra.floating.QrScannerActivity
import com.adebojel.dynamicislandultra.floating.SensorKitActivity
import com.adebojel.dynamicislandultra.floating.DocumentPreviewActivity
import com.adebojel.dynamicislandultra.notifications.MediaControlsActivity
import com.adebojel.dynamicislandultra.notifications.NotificationPreviewSettingsActivity
import com.adebojel.dynamicislandultra.overlay.IslandInteractionSettingsActivity
import com.adebojel.dynamicislandultra.system.UtilityIntents
import java.util.Locale

class OfflineAssistantActivity : ComponentActivity() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechOutput: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechOutput = TextToSpeech(this) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) speechOutput?.language = Locale("id", "ID")
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                var input by remember { mutableStateOf("") }
                var history by remember {
                    mutableStateOf(listOf("DIALOG OFFLINE • Perintah lokal diproses di perangkat."))
                }
                var voiceStatus by remember {
                    mutableStateOf("Suara offline memerlukan Android 12+ dan layanan/model lokal yang tersedia.")
                }
                val engine = remember { OfflineAssistant() }
                val requestMic = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) listenOnDevice(
                        onText = { phrase -> input = phrase; voiceStatus = "Teks dikenali. Periksa lalu jalankan." },
                        onStatus = { voiceStatus = it }
                    ) else voiceStatus = "Izin mikrofon ditolak. Perintah teks tetap tersedia."
                }

                fun execute(command: String) {
                    if (command.isBlank()) return
                    val reply = engine.respond(command)
                    history = history + "Anda: $command" + "Ultra: ${reply.text}"
                    input = ""
                    when (reply.action) {
                        OfflineAction.OpenBrowser -> startActivity(Intent(this@OfflineAssistantActivity, DuckDuckGoBrowserActivity::class.java))
                        OfflineAction.OpenUpdateCenter -> startActivity(Intent(this@OfflineAssistantActivity, AppUpdateActivity::class.java))
                        OfflineAction.RunDiagnostics -> history = history + "Ultra: ${LocalDiagnostics.createReport(this@OfflineAssistantActivity)}"
                        OfflineAction.OpenInstalledApps -> startActivity(Intent(this@OfflineAssistantActivity, InstalledAppsActivity::class.java))
                        OfflineAction.OpenCalculator -> startActivity(Intent(this@OfflineAssistantActivity, OfflineCalculatorActivity::class.java))
                        OfflineAction.OpenFiles -> startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"))
                        OfflineAction.OpenWifiSettings -> openUtility("wifi")
                        OfflineAction.OpenBluetoothSettings -> openUtility("bluetooth")
                        OfflineAction.OpenAccessibilitySettings -> openUtility("accessibility")
                        OfflineAction.OpenTimer -> openUtility("timer")
                        OfflineAction.OpenDisplaySettings -> openUtility("display")
                        OfflineAction.OpenSoundSettings -> openUtility("sound")
                        OfflineAction.OpenQrScanner -> startActivity(Intent(this@OfflineAssistantActivity, QrScannerActivity::class.java))
                        OfflineAction.OpenSensorKit -> startActivity(Intent(this@OfflineAssistantActivity, SensorKitActivity::class.java))
                        OfflineAction.OpenMediaControls -> startActivity(Intent(this@OfflineAssistantActivity, MediaControlsActivity::class.java))
                        OfflineAction.OpenDocumentPreview -> startActivity(Intent(this@OfflineAssistantActivity, DocumentPreviewActivity::class.java))
                        OfflineAction.OpenNotificationPrivacy -> startActivity(Intent(this@OfflineAssistantActivity, NotificationPreviewSettingsActivity::class.java))
                        OfflineAction.OpenIslandSettings -> startActivity(Intent(this@OfflineAssistantActivity, IslandInteractionSettingsActivity::class.java))
                        OfflineAction.ShowBattery -> {
                            val battery = getSystemService(BATTERY_SERVICE) as android.os.BatteryManager
                            val level = battery.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
                            history = history + "Ultra: Baterai perangkat ${if (level >= 0) "$level%" else "belum tersedia"}."
                        }
                        null -> Unit
                    }
                }

                Column(
                    Modifier.fillMaxSize().background(Color(0xFF0B1222)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("BUILT-IN OFFLINE ASSISTANT", color = Color(0xFF43E8FF), fontSize = 16.sp)
                    Text(
                        "Asisten utilitas lokal, bukan LLM generatif. Voice input hanya ditawarkan ketika recognizer on-device tersedia.",
                        color = Color.LightGray, fontSize = 11.sp
                    )
                    LazyColumn(
                        Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { line ->
                            Surface(color = Color(0xFF18243A), shape = RoundedCornerShape(12.dp)) {
                                Text(line, color = Color.White, modifier = Modifier.fillMaxWidth().padding(12.dp))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.fillMaxWidth().semantics {
                            contentDescription = "Masukkan perintah untuk asisten lokal"
                        },
                        label = { Text("Perintah lokal / offline") }
                    )
                    Text(voiceStatus, color = Color.LightGray, fontSize = 10.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT < 31) {
                                    voiceStatus = "Pengenalan suara offline memerlukan Android 12+. Gunakan perintah teks."
                                } else if (!SpeechRecognizer.isOnDeviceRecognitionAvailable(this@OfflineAssistantActivity)) {
                                    voiceStatus = "Recognizer lokal tidak tersedia atau model bahasa belum terpasang."
                                } else if (ContextCompat.checkSelfPermission(this@OfflineAssistantActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    listenOnDevice(
                                        onText = { phrase -> input = phrase; voiceStatus = "Teks dikenali. Periksa lalu jalankan." },
                                        onStatus = { voiceStatus = it }
                                    )
                                } else {
                                    requestMic.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.weight(1f).semantics {
                                contentDescription = "Mulai pengenalan suara offline"
                            }
                        ) { Text("Suara offline") }
                        TextButton(
                            onClick = {
                                val answer = history.lastOrNull { it.startsWith("Ultra:") }?.removePrefix("Ultra:")?.trim()
                                if (answer.isNullOrBlank()) voiceStatus = "Belum ada jawaban untuk dibacakan."
                                else speak(answer)
                            },
                            modifier = Modifier.weight(1f).semantics {
                                contentDescription = "Bacakan jawaban terakhir"
                            }
                        ) { Text("Bacakan jawaban") }
                    }
                    Button(
                        onClick = { execute(input) },
                        modifier = Modifier.fillMaxWidth().semantics {
                            contentDescription = "Jalankan perintah asisten"
                        }
                    ) { Text("Jalankan") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { startActivity(Intent(this@OfflineAssistantActivity, FloatingWorkspaceActivity::class.java)) }) { Text("Workspace") }
                        TextButton(onClick = { startActivity(Intent(this@OfflineAssistantActivity, OfflineNotesActivity::class.java)) }) { Text("Catatan") }
                    }
                }
            }
        }
    }

    private fun listenOnDevice(onText: (String) -> Unit, onStatus: (String) -> Unit) {
        if (Build.VERSION.SDK_INT < 31 || !SpeechRecognizer.isOnDeviceRecognitionAvailable(this)) {
            onStatus("Pengenalan suara offline tidak tersedia pada perangkat ini.")
            return
        }
        releaseRecognizer()
        val created = runCatching { SpeechRecognizer.createOnDeviceSpeechRecognizer(this) }
            .getOrElse {
                onStatus("Recognizer on-device gagal dibuat.")
                return
            }
        speechRecognizer = created
        created.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { onStatus("Mendengarkan…") }
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { onStatus("Memproses ucapan di perangkat…") }
            override fun onError(error: Int) {
                onStatus("Pengenalan gagal (kode $error). Perintah teks tetap tersedia.")
                releaseRecognizer()
            }
            override fun onResults(results: Bundle?) {
                val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (phrase.isNullOrBlank()) onStatus("Tidak ada ucapan yang dikenali.") else onText(phrase)
                releaseRecognizer()
            }
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val request = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        runCatching { created.startListening(request) }.onFailure {
            onStatus("Mikrofon tidak dapat dimulai. Periksa izin perangkat.")
            releaseRecognizer()
        }
    }

    private fun releaseRecognizer() {
        speechRecognizer?.let {
            runCatching { it.cancel() }
            runCatching { it.destroy() }
        }
        speechRecognizer = null
    }

    private fun speak(value: String) {
        val engine = speechOutput
        if (!ttsReady || engine == null) {
            Toast.makeText(this, "Mesin suara perangkat belum siap.", Toast.LENGTH_SHORT).show()
            return
        }
        engine.speak(value.take(900), TextToSpeech.QUEUE_FLUSH, null, "offline-assistant-reply")
    }

    private fun openUtility(key: String) {
        val intent = UtilityIntents.create(key, packageName)
        if (intent == null) {
            Toast.makeText(this, "Fitur tidak tersedia pada perangkat ini", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching { startActivity(intent) }
            .onFailure { Toast.makeText(this, "Tidak dapat membuka alat tersebut", Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroy() {
        releaseRecognizer()
        speechOutput?.stop()
        speechOutput?.shutdown()
        speechOutput = null
        super.onDestroy()
    }
}
