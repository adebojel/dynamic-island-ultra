package com.adebojel.dynamicislandultra

import android.Manifest
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.adebojel.dynamicislandultra.overlay.FloatingIslandService
import com.adebojel.dynamicislandultra.ai.OfflineAssistantActivity
import com.adebojel.dynamicislandultra.floating.InstalledAppsActivity
import com.adebojel.dynamicislandultra.floating.OfflineNotesActivity
import com.adebojel.dynamicislandultra.floating.OfflineCalculatorActivity
import com.adebojel.dynamicislandultra.floating.FloatingWorkspaceActivity
import com.adebojel.dynamicislandultra.browser.DuckDuckGoBrowserActivity
import com.adebojel.dynamicislandultra.update.AppUpdateActivity

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
                    onStop = { stopIsland() },
                    onOverlay = { openOverlaySettings() },
                    onNotifications = { openNotificationSettings() },
                    onBattery = { openBatterySettings() },
                    onPermission = { requestNotificationPermission() },
                    onInstalledApps = { openLocal(InstalledAppsActivity::class.java) },
                    onAssistant = { openLocal(OfflineAssistantActivity::class.java) },
                    onNotes = { openLocal(OfflineNotesActivity::class.java) },
                    onCalculator = { openLocal(OfflineCalculatorActivity::class.java) },
                    onWorkspace = { openLocal(FloatingWorkspaceActivity::class.java) },
                    onBrowser = { openLocal(DuckDuckGoBrowserActivity::class.java) },
                    onUpdate = { openLocal(AppUpdateActivity::class.java) }
                )
            }
        }
    }

    private fun startIsland() {
        getSharedPreferences("island", Context.MODE_PRIVATE)
            .edit().putBoolean("enabled", true).apply()
        if (!Settings.canDrawOverlays(this)) {
            openOverlaySettings()
            return
        }
        ContextCompat.startForegroundService(
            this,
            Intent(this, FloatingIslandService::class.java)
        )
    }

    private fun stopIsland() {
        getSharedPreferences("island", Context.MODE_PRIVATE)
            .edit().putBoolean("enabled", false).apply()
        stopService(
            Intent(this, FloatingIslandService::class.java)
                .setAction(FloatingIslandService.ACTION_STOP)
        )
    }

    private fun openOverlaySettings() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun openNotificationSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun openBatterySettings() {
        startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openLocal(type: Class<*>) {
        startActivity(Intent(this, type))
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
    onInstalledApps: () -> Unit,
    onAssistant: () -> Unit,
    onNotes: () -> Unit,
    onCalculator: () -> Unit,
    onWorkspace: () -> Unit,
    onBrowser: () -> Unit,
    onUpdate: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFF101A31), Night, Color(0xFF090D18))
            )
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier.width(190.dp).height(42.dp)
                            .background(Color.Black, RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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
                Button(onClick = onStart, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Bolt, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Start Island")
                }
                OutlinedButton(onClick = onStop, modifier = Modifier.weight(1f)) {
                    Text("Stop")
                }
            }
        }
        item { Text("PERMISSION CENTER", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp) }
        item {
            PermissionCard(
                "Display over other apps",
                "Required for the floating island and bubble",
                "Open settings",
                onOverlay
            )
        }
        item {
            PermissionCard(
                "Notification access",
                "Read selected notification previews for the island",
                "Grant access",
                onNotifications
            )
        }
        item {
            PermissionCard(
                "Notifications",
                "Allow the app to show its foreground-service status",
                "Allow",
                onPermission
            )
        }
        item {
            PermissionCard(
                "Battery settings",
                "Review device background restrictions; availability varies by manufacturer",
                "Review",
                onBattery
            )
        }
        item { Text("OFFLINE TOOLBOX", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp) }
        item {
            FeatureCard(Icons.Default.Apps, "Installed app collection", "Browse launchable apps with limited package visibility", "Offline", onInstalledApps)
        }
        item {
            FeatureCard(Icons.Default.Psychology, "Offline utility assistant", "Local commands and calculations; no chat service or network", "Offline", onAssistant)
        }
        item {
            FeatureCard(Icons.Default.Calculate, "Offline calculator", "Local arithmetic and expression evaluator", "Offline", onCalculator)
        }
        item {
            FeatureCard(Icons.Default.PictureInPictureAlt, "Floating workspace", "PiP for this app's own workspace activity", "Android PiP", onWorkspace)
        }
        item {
            FeatureCard(Icons.Default.Tune, "Offline notes", "Notes stored on this device only", "Local", onNotes)
        }
        item { Text("INTERNET & ONLINE FEATURES", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp) }
        item {
            FeatureCard(Icons.Default.Language, "DuckDuckGo Browser", "Browser bawaan DuckDuckGo, pencarian privat, semua situs HTTPS dan mode Desktop/Mobile", "Internet", onBrowser)
        }
        item {
            FeatureCard(Icons.Default.SystemUpdate, "GitHub update center", "Check latest release, verify SHA-256 and hand off installation to Android", "Internet", onUpdate)
        }
        item { Text("FEATURE CATEGORIES", color = Cyan, fontSize = 12.sp, letterSpacing = 2.sp) }
        item {
            FeatureCard(
                Icons.Default.Notifications,
                "Smart notifications",
                "Notification previews and app-aware activity cards",
                "Foundation"
            )
        }
        item {
            FeatureCard(
                Icons.Default.Widgets,
                "Assistive floating bubble",
                "Quick access to categorized floating tools",
                "Included"
            )
        }
        item {
            FeatureCard(
                Icons.Default.Bluetooth,
                "System shortcuts",
                "Open Android system panels where direct toggles are restricted",
                "Android-safe"
            )
        }
        item {
            FeatureCard(
                Icons.Default.Tune,
                "Personalization",
                "Theme, size, placement and interaction settings",
                "Roadmap"
            )
        }
        item {
            Text(
                "Permissions are requested only when needed. Android does not allow ordinary apps to silently grant SMS, storage, Wi-Fi, mobile-data, or privileged system access.",
                color = Color(0xFF9BA8C1),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    action: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White)
                Text(description, color = Color(0xFF9BA8C1), fontSize = 11.sp)
            }
            TextButton(onClick = onClick) { Text(action) }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    status: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
