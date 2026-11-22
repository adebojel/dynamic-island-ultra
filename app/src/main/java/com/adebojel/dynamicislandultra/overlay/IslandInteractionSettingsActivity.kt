package com.adebojel.dynamicislandultra.overlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class IslandInteractionSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("island", MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                var snap by remember { mutableStateOf(prefs.getBoolean("edge_docking", true)) }
                var haptics by remember { mutableStateOf(prefs.getBoolean("haptic_enabled", false)) }
                Column(Modifier.fillMaxSize().background(Color(0xFF0B1222)).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("ISLAND INTERACTION", color = Color(0xFF43E8FF), fontSize = 18.sp)
                    Text("Pengaturan disimpan lokal. Haptic dapat dimatikan kapan saja.",
                        color = Color.LightGray, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Snap ke tepi layar", color = Color.White)
                            Text("Pill menempel ke sisi kiri atau kanan setelah digeser.", color = Color.LightGray, fontSize = 10.sp)
                        }
                        Switch(checked = snap, onCheckedChange = {
                            snap = it
                            prefs.edit().putBoolean("edge_docking", it).apply()
                        })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Haptic feedback", color = Color.White)
                            Text("Getaran singkat saat pill dilepas.", color = Color.LightGray, fontSize = 10.sp)
                        }
                        Switch(checked = haptics, onCheckedChange = {
                            haptics = it
                            prefs.edit().putBoolean("haptic_enabled", it).apply()
                        })
                    }
                    OutlinedButton(onClick = { finish() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Selesai")
                    }
                }
            }
        }
    }
}
