package com.adebojel.dynamicislandultra.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adebojel.dynamicislandultra.floating.FloatingWorkspaceActivity
import com.adebojel.dynamicislandultra.floating.OfflineCalculatorActivity
import com.adebojel.dynamicislandultra.floating.OfflineNotesActivity
import com.adebojel.dynamicislandultra.system.UtilityIntents

class OfflineAssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                var input by remember { mutableStateOf("") }
                var history by remember { mutableStateOf(listOf("DIALOG OFFLINE • Semua pemrosesan berlangsung di perangkat.")) }
                val engine = remember { OfflineAssistant() }
                Column(
                    Modifier.fillMaxSize().background(Color(0xFF0B1222)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("OFFLINE UTILITY ASSISTANT", color = Color(0xFF43E8FF), fontSize = 16.sp)
                    Text("Bukan chat AI cloud. Asisten ini menjalankan perintah perangkat dan hitungan sederhana secara lokal.", color = Color.LightGray, fontSize = 11.sp)
                    LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(history) { line ->
                            Surface(color = Color(0xFF18243A), shape = RoundedCornerShape(12.dp)) {
                                Text(line, color = Color.White, modifier = Modifier.fillMaxWidth().padding(12.dp))
                            }
                        }
                    }
                    OutlinedTextField(input, { input = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Perintah offline") })
                    Button(
                        onClick = {
                            val reply = engine.respond(input)
                            history = history + "Anda: $input" + "Ultra: ${reply.text}"
                            input = ""
                            when (reply.action) {
                                OfflineAction.OpenInstalledApps -> startActivity(Intent(this@OfflineAssistantActivity, InstalledAppsActivity::class.java))
                                OfflineAction.OpenCalculator -> startActivity(Intent(this@OfflineAssistantActivity, OfflineCalculatorActivity::class.java))
                                OfflineAction.OpenFiles -> startActivity(UtilityIntents.create("files", packageName))
                                OfflineAction.OpenWifiSettings -> startActivity(UtilityIntents.create("wifi", packageName))
                                OfflineAction.OpenBluetoothSettings -> startActivity(UtilityIntents.create("bluetooth", packageName))
                                OfflineAction.OpenAccessibilitySettings -> startActivity(UtilityIntents.create("accessibility", packageName))
                                OfflineAction.OpenTimer -> startActivity(UtilityIntents.create("timer", packageName))
                                OfflineAction.OpenDisplaySettings -> startActivity(UtilityIntents.create("display", packageName))
                                OfflineAction.OpenSoundSettings -> startActivity(UtilityIntents.create("sound", packageName))
                                OfflineAction.ShowBattery, null -> Unit
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Jalankan") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { startActivity(Intent(this@OfflineAssistantActivity, FloatingWorkspaceActivity::class.java)) }) { Text("Workspace") }
                        TextButton(onClick = { startActivity(Intent(this@OfflineAssistantActivity, OfflineNotesActivity::class.java)) }) { Text("Catatan") }
                    }
                }
            }
        }
    }
}
