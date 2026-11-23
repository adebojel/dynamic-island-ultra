package com.adebojel.dynamicislandultra.system

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val ControlNight = Color(0xFF0B1222)
private val ControlCyan = Color(0xFF43E8FF)

class DeviceControlActivity : ComponentActivity() {
    private var serviceEnabled by mutableStateOf(false)
    private var hasWriteSettings by mutableStateOf(false)
    private var brightness by mutableIntStateOf(128)
    private var autoRotate by mutableStateOf(true)
    private var mediaVolume by mutableIntStateOf(0)
    private var maxMediaVolume by mutableIntStateOf(1)
    private var torchAvailable by mutableStateOf(false)
    private var torchOn by mutableStateOf(false)
    private lateinit var audioManager: AudioManager
    private lateinit var cameraManager: CameraManager
    private var torchCameraId: String? = null

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) setTorch(true)
        else Toast.makeText(this, "Izin kamera diperlukan untuk menyalakan lampu flash.", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        torchCameraId = findTorchCamera()
        torchAvailable = torchCameraId != null
        maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        refreshSystemSettings()
        mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = ControlCyan, background = ControlNight, surface = Color(0xFF17243A))) {
                DeviceControlScreen(
                    enabled = serviceEnabled,
                    hasWriteSettings = hasWriteSettings,
                    brightness = brightness,
                    autoRotate = autoRotate,
                    mediaVolume = mediaVolume,
                    maxMediaVolume = maxMediaVolume,
                    torchAvailable = torchAvailable,
                    torchOn = torchOn,
                    onEnable = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onAction = ::performAction,
                    onRequestWriteSettings = ::requestWriteSettings,
                    onBrightnessChange = ::applyScreenBrightness,
                    onAutoRotateChange = ::applyAutoRotate,
                    onVolumeChange = ::applyMediaVolume,
                    onTorchToggle = ::toggleTorch,
                    onOpenScreenRecorder = { startActivity(Intent(this, ScreenRecorderActivity::class.java)) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        serviceEnabled = isGlobalActionServiceEnabled()
        refreshSystemSettings()
        if (::audioManager.isInitialized) {
            mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        }
    }

    private fun refreshSystemSettings() {
        hasWriteSettings = Settings.System.canWrite(this)
        brightness = runCatching {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
        }.getOrDefault(128).coerceIn(1, 255)
        autoRotate = runCatching {
            Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1
        }.getOrDefault(true)
    }

    private fun requestWriteSettings() {
        if (Settings.System.canWrite(this)) {
            hasWriteSettings = true
            return
        }
        runCatching {
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
        }.onFailure {
            Toast.makeText(this, "Pengaturan brightness/rotasi tidak dapat dibuka.", Toast.LENGTH_LONG).show()
        }
    }

    private fun applyScreenBrightness(value: Int) {
        if (!Settings.System.canWrite(this)) {
            requestWriteSettings()
            return
        }
        val safe = value.coerceIn(1, 255)
        val ok = runCatching {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            ) && Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, safe)
        }.getOrDefault(false)
        if (ok) brightness = safe
        else Toast.makeText(this, "Brightness tidak dapat diubah pada perangkat ini.", Toast.LENGTH_LONG).show()
    }

    private fun applyAutoRotate(enabled: Boolean) {
        if (!Settings.System.canWrite(this)) {
            requestWriteSettings()
            return
        }
        val ok = runCatching {
            Settings.System.putInt(
                contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (enabled) 1 else 0
            )
        }.getOrDefault(false)
        if (ok) autoRotate = enabled
        else Toast.makeText(this, "Pengaturan rotasi ditolak oleh perangkat.", Toast.LENGTH_LONG).show()
    }

    private fun applyMediaVolume(value: Int) {
        val safe = value.coerceIn(0, maxMediaVolume)
        runCatching {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, safe, AudioManager.FLAG_SHOW_UI)
            mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        }.onFailure {
            Toast.makeText(this, "Volume tidak dapat diubah.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findTorchCamera(): String? = runCatching {
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }.getOrNull()

    private fun toggleTorch() {
        if (!torchAvailable || torchCameraId == null) {
            Toast.makeText(this, "Lampu flash tidak tersedia pada perangkat ini.", Toast.LENGTH_SHORT).show()
            return
        }
        if (torchOn) {
            setTorch(false)
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setTorch(true)
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setTorch(enabled: Boolean) {
        val id = torchCameraId ?: return
        runCatching { cameraManager.setTorchMode(id, enabled) }
            .onSuccess { torchOn = enabled }
            .onFailure {
                torchOn = false
                Toast.makeText(this, "Lampu flash gagal diubah: ${it.message ?: "camera service sibuk"}", Toast.LENGTH_LONG).show()
            }
    }

    private fun isGlobalActionServiceEnabled(): Boolean {
        val expected = ComponentName(this, GlobalActionAccessibilityService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    private fun performAction(action: Int) {
        if (!isGlobalActionServiceEnabled()) {
            Toast.makeText(this, "Aktifkan Global Navigation Service di pengaturan Accessibility Android.", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }
        if (!GlobalActionController.perform(action)) {
            Toast.makeText(this, "Service belum siap. Periksa kembali pengaturan Accessibility.", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun DeviceControlScreen(
    enabled: Boolean,
    hasWriteSettings: Boolean,
    brightness: Int,
    autoRotate: Boolean,
    mediaVolume: Int,
    maxMediaVolume: Int,
    torchAvailable: Boolean,
    torchOn: Boolean,
    onEnable: () -> Unit,
    onAction: (Int) -> Unit,
    onRequestWriteSettings: () -> Unit,
    onBrightnessChange: (Int) -> Unit,
    onAutoRotateChange: (Boolean) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onTorchToggle: () -> Unit,
    onOpenScreenRecorder: () -> Unit
) {
    val actions = buildList {
        add("Home" to AccessibilityService.GLOBAL_ACTION_HOME)
        add("Back" to AccessibilityService.GLOBAL_ACTION_BACK)
        add("Recent Apps" to AccessibilityService.GLOBAL_ACTION_RECENTS)
        add("Notifications" to AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
        add("Quick Settings" to AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
        add("Power Dialog" to AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
        if (Build.VERSION.SDK_INT >= 28) {
            add("Lock Screen" to AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
            add("Take Screenshot" to AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
        }
    }
    Column(
        Modifier.fillMaxSize().background(ControlNight).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("GLOBAL SYSTEM CONTROLS", color = ControlCyan, fontSize = 19.sp)
        Surface(color = Color(0xFF17243A), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (enabled) "Accessibility service terdeteksi" else "Accessibility service belum diaktifkan",
                    color = if (enabled) ControlCyan else Color.White
                )
                Text(
                    "Service ini hanya menjalankan aksi global yang Anda tekan. Service tidak membaca teks, isi layar, notifikasi, atau data aplikasi lain.",
                    color = Color.LightGray, fontSize = 12.sp
                )
                if (!enabled) Button(onClick = onEnable, modifier = Modifier.fillMaxWidth()) {
                    Text("Buka pengaturan Accessibility")
                }
            }
        }
        actions.forEach { (label, action) ->
            Button(onClick = { onAction(action) }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text(label)
            }
        }
        Divider()
        Text("HARDWARE & DISPLAY", color = ControlCyan, fontSize = 14.sp)
        OutlinedButton(onClick = onOpenScreenRecorder, modifier = Modifier.fillMaxWidth()) {
            Text("Screen Recorder • MP4")
        }
        Button(onClick = onTorchToggle, enabled = torchAvailable, modifier = Modifier.fillMaxWidth()) {
            Text(if (torchOn) "Matikan Flashlight" else "Nyalakan Flashlight")
        }
        Text("Media volume · $mediaVolume / $maxMediaVolume", color = Color.White, fontSize = 12.sp)
        Slider(
            value = mediaVolume.toFloat(),
            onValueChange = { onVolumeChange(it.toInt()) },
            valueRange = 0f..maxMediaVolume.toFloat()
        )
        Text(
            if (hasWriteSettings) "Brightness dan auto-rotation dapat diubah."
            else "Brightness dan auto-rotation membutuhkan izin Modify System Settings dari Android.",
            color = Color.LightGray, fontSize = 11.sp
        )
        if (!hasWriteSettings) {
            OutlinedButton(onClick = onRequestWriteSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Izinkan pengaturan brightness/rotasi")
            }
        }
        Text("Brightness · $brightness / 255", color = Color.White, fontSize = 12.sp)
        Slider(
            value = brightness.toFloat(),
            onValueChange = { if (hasWriteSettings) onBrightnessChange(it.toInt()) },
            valueRange = 1f..255f,
            enabled = hasWriteSettings
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Auto-rotation", color = Color.White)
            Switch(checked = autoRotate, onCheckedChange = onAutoRotateChange, enabled = hasWriteSettings)
        }
        Text(
            "Brightness/rotasi membutuhkan persetujuan khusus Android. Flashlight meminta izin kamera hanya ketika pertama kali dipakai. Volume mengubah stream media. Hasil dapat dibatasi OEM.",
            color = Color(0xFF8FA3C2), fontSize = 10.sp
        )
    }
}
