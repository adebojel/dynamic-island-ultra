package com.adebojel.dynamicislandultra.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MediaControlsActivity : ComponentActivity() {
    private var currentMedia by mutableStateOf(ActiveMediaSession.snapshot())
    private var eqSnapshot by mutableStateOf(
        EqualizerSnapshot(false, false, emptyList(), -1500, 1500, 0, "Flat", "Equalizer belum diaktifkan.")
    )
    private var audioRouteSummary by mutableStateOf("")
    private lateinit var equalizer: SystemAudioEqualizer

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            currentMedia = ActiveMediaSession.snapshot()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        equalizer = SystemAudioEqualizer(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                val item = currentMedia
                var seek by remember(item?.positionMs) { mutableStateOf((item?.positionMs ?: 0L).toFloat()) }
                val duration = (item?.durationMs ?: 0L).coerceAtLeast(1L).toFloat()
                Column(
                    Modifier.fillMaxSize().background(Color(0xFF0B1222)).verticalScroll(rememberScrollState()).padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("MEDIA SESSION", color = Color(0xFF43E8FF), fontSize = 18.sp)
                    if (item?.artwork != null) {
                        Image(item.artwork.asImageBitmap(), "Artwork media aktif", modifier = Modifier.size(190.dp))
                    } else Spacer(Modifier.height(30.dp))
                    Text(item?.title?.ifBlank { "Tidak ada media aktif" } ?: "Tidak ada media aktif",
                        color = Color.White, fontSize = 20.sp)
                    Text(
                        listOfNotNull(item?.artist?.takeIf { it.isNotBlank() }, item?.album?.takeIf { it.isNotBlank() })
                            .joinToString(" • "),
                        color = Color.LightGray, fontSize = 12.sp
                    )
                    Text(item?.packageName.orEmpty(), color = Color(0xFF93A3BE), fontSize = 10.sp)
                    Slider(
                        value = seek.coerceIn(0f, duration),
                        onValueChange = { seek = it },
                        onValueChangeFinished = { ActiveMediaSession.seekTo(seek.toLong()) },
                        valueRange = 0f..duration,
                        enabled = (item?.durationMs ?: 0L) > 0L
                    )
                    Text("${timeLabel(item?.positionMs ?: 0L)} / ${timeLabel(item?.durationMs ?: 0L)}",
                        color = Color.LightGray, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { ActiveMediaSession.skipPrevious() }, enabled = item != null) { Text("Sebelumnya") }
                        Button(onClick = { ActiveMediaSession.togglePlayback() }, enabled = item != null) {
                            Text(if (item?.playing == true) "Jeda" else "Putar")
                        }
                        Button(onClick = { ActiveMediaSession.skipNext() }, enabled = item != null) { Text("Berikutnya") }
                    }
                    Text("Kontrol memerlukan MediaSession yang didukung dan Notification Access.",
                        color = Color(0xFF93A3BE), fontSize = 11.sp)
                    HorizontalDivider(color = Color(0xFF263650))
                    EqualizerPanel(
                        snapshot = eqSnapshot,
                        onEnabled = { eqSnapshot = equalizer.setEnabled(it) },
                        onPreset = { eqSnapshot = equalizer.applyPreset(it) },
                        onBand = { index, value -> eqSnapshot = equalizer.setBand(index, value) },
                        onPreamp = { eqSnapshot = equalizer.setPreamp(it) },
                        onRetry = { eqSnapshot = equalizer.attach() }
                    )
                    HorizontalDivider(color = Color(0xFF263650))
                    AudioRoutingPanel(
                        routeSummary = audioRouteSummary,
                        onOpenBluetooth = {
                            runCatching { startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
                        },
                        onRefresh = { audioRouteSummary = describeOutputDevices() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ActiveMediaSession.ACTION_CHANGED)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        else @Suppress("DEPRECATION") registerReceiver(receiver, filter)
        currentMedia = ActiveMediaSession.snapshot()
        eqSnapshot = equalizer.attach()
        audioRouteSummary = describeOutputDevices()
    }

    override fun onStop() {
        runCatching { unregisterReceiver(receiver) }
        equalizer.release()
        super.onStop()
    }

    @Suppress("DEPRECATION")
    private fun describeOutputDevices(): String {
        val manager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val outputs = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val labels = outputs.mapNotNull { device ->
            val label = when (device.type) {
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Speaker HP"
                AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Earpiece"
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Headset kabel"
                AudioDeviceInfo.TYPE_USB_HEADSET, AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_ACCESSORY -> "Audio USB"
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth media"
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth komunikasi"
                else -> null
            }
            label
        }.distinct()
        return if (labels.isEmpty()) "Belum ada output audio yang terdeteksi."
        else "Output yang terdeteksi: ${labels.joinToString(", ")}"
    }

    private fun timeLabel(milliseconds: Long): String {
        val seconds = milliseconds.coerceAtLeast(0L) / 1000L
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }
}

@Composable
private fun EqualizerPanel(
    snapshot: EqualizerSnapshot,
    onEnabled: (Boolean) -> Unit,
    onPreset: (String) -> Unit,
    onBand: (Int, Int) -> Unit,
    onPreamp: (Int) -> Unit,
    onRetry: () -> Unit
) {
    var presetMenu by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("EQUALIZER STUDIO", color = Color(0xFF43E8FF), fontSize = 18.sp)
        Text("Pengolah output Android • sesi output global (dukungan bergantung perangkat)",
            color = Color(0xFF93A3BE), fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Equalizer aktif", color = Color.White, modifier = Modifier.weight(1f))
            Switch(checked = snapshot.enabled, onCheckedChange = onEnabled, enabled = snapshot.available)
        }
        if (!snapshot.available) {
            Text(snapshot.message, color = Color(0xFFFFC46B), fontSize = 12.sp)
            OutlinedButton(onClick = onRetry) { Text("Coba deteksi ulang") }
        } else {
            Box {
                OutlinedButton(onClick = { presetMenu = true }) { Text("Preset: ${snapshot.preset}  ▾") }
                DropdownMenu(expanded = presetMenu, onDismissRequest = { presetMenu = false }) {
                    EqualizerPolicy.presets.forEach { preset ->
                        DropdownMenuItem(text = { Text(preset) }, onClick = {
                            presetMenu = false
                            onPreset(preset)
                        })
                    }
                }
            }
            Text("Preamp / gain: ${"%.1f".format(snapshot.preampMb / 100f)} dB",
                color = Color.White, fontSize = 12.sp)
            Slider(
                value = snapshot.preampMb.toFloat(),
                onValueChange = { onPreamp(it.toInt()) },
                valueRange = EqualizerPolicy.MIN_PREAMP_MB.toFloat()..EqualizerPolicy.MAX_PREAMP_MB.toFloat()
            )
            snapshot.bands.forEach { band ->
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row {
                        Text(if (band.centerHz >= 1000) "${band.centerHz / 1000f} kHz" else "${band.centerHz} Hz",
                            color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text("${"%.1f".format(band.levelMb / 100f)} dB", color = Color(0xFF93A3BE), fontSize = 11.sp)
                    }
                    Slider(
                        value = band.levelMb.toFloat(),
                        onValueChange = { onBand(band.index, it.toInt()) },
                        valueRange = snapshot.minLevelMb.toFloat()..snapshot.maxLevelMb.toFloat()
                    )
                }
            }
            Text("Preset dan kurva tersimpan otomatis di perangkat. Preamp dibatasi untuk mengurangi risiko clipping; limiter perangkat keras tidak dijamin.",
                color = Color(0xFF93A3BE), fontSize = 10.sp)
        }
    }
}

@Composable
private fun AudioRoutingPanel(
    routeSummary: String,
    onOpenBluetooth: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("DUAL AUDIO ROUTING", color = Color(0xFF43E8FF), fontSize = 18.sp)
        Text(routeSummary, color = Color.LightGray, fontSize = 12.sp)
        Text(
            "Untuk skenario musik tetap di Bluetooth dan panggilan lewat handset: buka pengaturan Bluetooth perangkat audio, lalu nonaktifkan profil 'Phone calls / Panggilan telepon' jika tersedia, tetapi biarkan 'Media audio' aktif. Android kemudian dapat memakai speaker/earpiece HP untuk panggilan dan Bluetooth untuk media. Hasilnya bergantung headset, versi Android dan kebijakan ponsel; musik tetap dapat di-pause/duck oleh sistem.",
            color = Color(0xFFFFC46B), fontSize = 11.sp
        )
        Text(
            "Dynamic Island Ultra tidak memaksa rute panggilan seluler atau WhatsApp milik aplikasi lain. Pemilihan perangkat komunikasi melalui API publik hanya dapat dikendalikan secara andal untuk sesi komunikasi milik aplikasi ini sendiri.",
            color = Color(0xFF93A3BE), fontSize = 11.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onOpenBluetooth) { Text("Pengaturan Bluetooth") }
            OutlinedButton(onClick = onRefresh) { Text("Perbarui output") }
        }
    }
}
