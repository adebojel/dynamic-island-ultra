package com.adebojel.dynamicislandultra.system

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ControlNight = Color(0xFF0B1222)
private val ControlCyan = Color(0xFF43E8FF)

class DeviceControlActivity : ComponentActivity() {
    private var serviceEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceEnabled = isGlobalActionServiceEnabled()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = ControlCyan, background = ControlNight, surface = Color(0xFF17243A))) {
                DeviceControlScreen(
                    enabled = serviceEnabled,
                    onEnable = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onAction = ::performAction
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        serviceEnabled = isGlobalActionServiceEnabled()
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
private fun DeviceControlScreen(enabled: Boolean, onEnable: () -> Unit, onAction: (Int) -> Unit) {
    val actions = buildList {
        add("Home" to AccessibilityService.GLOBAL_ACTION_HOME)
        add("Back" to AccessibilityService.GLOBAL_ACTION_BACK)
        add("Recent Apps" to AccessibilityService.GLOBAL_ACTION_RECENTS)
        add("Notifications" to AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
        add("Quick Settings" to AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
        add("Power Dialog" to AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
        if (Build.VERSION.SDK_INT >= 28) add("Lock Screen" to AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
    }
    Column(
        Modifier.fillMaxSize().background(ControlNight).padding(16.dp),
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
        Text(
            "Aksi hanya tersedia setelah pengguna mengaktifkan service secara manual. Hasil tetap mengikuti versi Android dan kebijakan perangkat.",
            color = Color(0xFF8FA3C2), fontSize = 10.sp
        )
    }
}
