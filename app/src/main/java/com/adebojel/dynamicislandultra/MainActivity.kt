package com.adebojel.dynamicislandultra

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.adebojel.dynamicislandultra.overlay.FloatingIslandService

private val Night = Color(0xFF080B14)
private val Panel = Color(0xFF121A2B)
private val Cyan = Color(0xFF43E8FF)
private val Violet = Color(0xFFB36BFF)

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Cyan,
                    secondary = Violet,
                    background = Night,
                    surface = Panel,
                    onSurface = Color.White
                )
            ) {
                Dashboard(
                    onStart = { startIsland() },
                    onStop = { stopService(Intent(this, FloatingIslandService::class.java)) },
                    onOverlay = { openOverlaySettings() },
                    onNotifications = { openNotificationSettings() },
                    onBattery = { requestBatteryExemption() },
                    onPermission = { requestNotificationPermission() },
                    overlayGranted = { Settings.canDrawOverlays(this) }
                )
            }
        }
    }

    private fun startIsland() {
        getSharedPreferences("island", Context.MODE_PRIVATE).edit().putBoolean("enabled", true).apply()
        if (!Settings.canDrawOverlays(this)) {
            openOverlaySettings()
            return
        }
        ContextCompat.startForegroundService(this, Intent(this, FloatingIslandService::class.java))
    }

    private fun openOverlaySettings() {
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
    }

    private fun openNotificationSettings() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    private fun requestBatteryExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
            runCatching { startActivity(intent) }.onFailure {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
private fun Dashboard(
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOverlay: () -> Unit,
    onNotifications: () -> Unit,
    onBattery: () -> Unit,
    onPermission: () -> Unit,
    overlayGranted: () -> Boolean
) {
    var running by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF101A31), Night, Color(0xFF090D18)))
        ),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DYNAMIC ISLAND", color = Color.White, fontSize = 12.sp, letterSpacing = 3.sp)
                    Text("ULTRA", color = Cyan, fontSize = 34.sp)
                    Text("SMART INTERACTION • ANDROID", color = Color.LightGray, fontSize = 10.sp, letterSpacing = 1.5.sp)
                }
                Icon(Icons.Default.Bolt, null, tint = Violet, modifier = Modifier.size(42.dp))
            }
        }
        item {
            Box(
                Modifier.fillMaxWidth().background(
                    Brush.horizontalGradient(listOf(Color(0xFF17243A), Color(0xFF241A3A))),
                    RoundedCornerShape(28.dp)
                ).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.width(190.dp).height(42.dp).background(Color.Black, RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(8.dp).background(Cyan, RoundedCornerShape(50)))
                            Text("ISLAND READY", color = Color.White, fontSize = 10.sp, letterSpacing = 1.sp)
                            Box(Modifier.size(7.dp).background(Violet, RoundedCornerShape(50)))
                        }
                    }
                    Text("Your screen. Your control.", color = Color.White, fontSize = 17.sp)
                    Text("Floating island + assistive bubble", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { onStart(); running = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Bolt, null); Spacer(Modifier.width(6.dp)); Text("Start Island")
                }
                OutlinedButton(onClick = { onStop(); running = false }, modifier = Modifier.weight(1f)) {
                    Text("Stop")
                }
            }
        }
        item { Text("PERMISSION CENTER", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp) }
        item {
            PermissionCard("Display over other apps", "Required for the floating island and bubble", "Open settings", onOverlay)
        }
        item {
            PermissionCard("Notification access", "Read selected notification previews for the island", "Grant access", onNotifications)
        }
        item {
            PermissionCard("Notifications", "Allow the app to show its foreground-service status", "Allow", onPermission)
        }
        item {
            PermissionCard("Battery settings", "Optional: request exemption from some background limits", "Review", onBattery)
        }
        item { Text("FEATURE CATEGORIES", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp) }
        item { FeatureCard(Icons.Default.Notifications, "Smart notifications", "Notification previews and app-aware activity cards", "Foundation") }
        item { FeatureCard(Icons.Default.Widgets, "Assistive floating bubble", "Quick access to categorized floating tools", "Included") }
        item { FeatureCard(Icons.Default.Bluetooth, "System shortcuts", "Open Android system panels where direct toggles are restricted", "Android-safe") }
        item { FeatureCard(Icons.Default.Tune, "Personalization", "Theme, size, placement and interaction settings", "Roadmap") }
        item {
            Text(
                "Permissions are requested only when needed. Android does not allow ordinary apps to silently grant SMS, storage, Wi-Fi, mobile-data, or privileged system access.",
                color = Color(0xFF9BA8C1), fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PermissionCard(title: String, description: String, action: String, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White)
                Text(description, color = Color(0xFF9BA8C1), fontSize = 11.sp)
            }
            TextButton(onClick = onClick) { Text(action) }
        }
    }
}

@Composable
private fun FeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, status: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Cyan, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White)
                Text(detail, color = Color(0xFF9BA8C1), fontSize = 11.sp)
            }
            Text(status, color = Violet, fontSize = 10.sp)
        }
    }
}
