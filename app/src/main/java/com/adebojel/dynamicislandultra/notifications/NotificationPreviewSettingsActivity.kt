package com.adebojel.dynamicislandultra.notifications

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adebojel.dynamicislandultra.floating.FloatingAppRepository

class NotificationPreviewSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val visibleApps = FloatingAppRepository(this).loadLaunchableApps()
        val initial = NotificationPreviewSettingsStore.load(this)
        val appContext = applicationContext

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                var previewEnabled by remember { mutableStateOf(initial.enabled) }
                var hideContent by remember { mutableStateOf(initial.hideContent) }
                var allowlistOnly by remember { mutableStateOf(initial.allowlistOnly) }
                var allowedPackages by remember { mutableStateOf(initial.allowedPackages.toSet()) }

                fun persist(
                    enabled: Boolean = previewEnabled,
                    hide: Boolean = hideContent,
                    allowlist: Boolean = allowlistOnly,
                    packages: Set<String> = allowedPackages
                ) {
                    val updated = NotificationPreviewSettings(
                        enabled = enabled,
                        hideContent = hide,
                        allowlistOnly = allowlist,
                        allowedPackages = packages
                    )
                    NotificationPreviewSettingsStore.save(appContext, updated)
                    previewEnabled = updated.enabled
                    hideContent = updated.hideContent
                    allowlistOnly = updated.allowlistOnly
                    allowedPackages = updated.allowedPackages
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B1222))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "PRIVASI NOTIFIKASI",
                        color = Color(0xFF43E8FF),
                        fontSize = 18.sp
                    )
                    Text(
                        "Filter berjalan lokal di perangkat. Isi notifikasi tidak disimpan atau dikirim ke server aplikasi.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18243A))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            PrivacyToggle(
                                title = "Preview notifikasi",
                                description = "Matikan untuk menghentikan semua preview.",
                                checked = previewEnabled,
                                onCheckedChange = { persist(enabled = it) }
                            )
                            Spacer(Modifier.height(8.dp))
                            PrivacyToggle(
                                title = "Sembunyikan isi",
                                description = "Aktif secara default; hanya nama aplikasi yang ditampilkan.",
                                checked = hideContent,
                                onCheckedChange = { persist(hide = it) }
                            )
                            Spacer(Modifier.height(8.dp))
                            PrivacyToggle(
                                title = "Hanya aplikasi pilihan",
                                description = "Saat aktif, hanya aplikasi yang dicentang di bawah yang boleh tampil.",
                                checked = allowlistOnly,
                                onCheckedChange = { persist(allowlist = it) }
                            )
                        }
                    }

                    Text("ALLOWLIST APLIKASI", color = Color(0xFF43E8FF), fontSize = 11.sp)
                    Text(
                        when {
                            !allowlistOnly -> "Aktifkan mode aplikasi pilihan untuk mengatur daftar."
                            allowedPackages.isEmpty() -> "Belum ada aplikasi dipilih; tidak ada preview yang diteruskan."
                            else -> "${allowedPackages.size} aplikasi diizinkan."
                        },
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(visibleApps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF18243A), MaterialTheme.shapes.medium)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(app.label, color = Color.White, fontSize = 13.sp)
                                    Text(app.packageName, color = Color.LightGray, fontSize = 9.sp)
                                }
                                Checkbox(
                                    checked = app.packageName in allowedPackages,
                                    enabled = allowlistOnly,
                                    onCheckedChange = { checked ->
                                        val next = allowedPackages.toMutableSet()
                                        if (checked) next.add(app.packageName)
                                        else next.remove(app.packageName)
                                        persist(packages = next)
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kelola akses notifikasi Android")
                    }
                    OutlinedButton(
                        onClick = { finish() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Selesai")
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.sp)
            Text(description, color = Color.LightGray, fontSize = 10.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
