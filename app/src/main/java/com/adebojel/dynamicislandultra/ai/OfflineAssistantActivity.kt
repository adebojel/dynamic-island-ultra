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
import android.widget.Toast
import com.adebojel.dynamicislandultra.floating.FloatingWorkspaceActivity
import com.adebojel.dynamicislandultra.floating.OfflineCalculatorActivity
import com.adebojel.dynamicislandultra.floating.OfflineNotesActivity
import com.adebojel.dynamicislandultra.floating.InstalledAppsActivity
import com.adebojel.dynamicislandultra.browser.DuckDuckGoBrowserActivity
import com.adebojel.dynamicislandultra.update.AppUpdateActivity
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
                    Text("BUILT-IN OFFLINE ASSISTANT", color = Color(0xFF43E8FF), fontSize = 16.sp)
                    Text("Mesin keputusan lokal bawaan APK: perintah perangkat, hitungan dan pemeriksaan kesehatan fitur. Tidak memakai internet atau model impor.", color = Color.LightGray, fontSize = 11.sp)
                    LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(history) { line ->
                            Surface(color = Color(0xFF18243A), shape = RoundedCornerShape(12.dp)) {
                                Text(line, color = Color.White, modifier = Modifier.fillMaxWidth().padding(12.dp))
                            }
                        }
                    }
                    OutlinedTextField(input, { input = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Perintah lokal / offline") })
                    Button(
                        onClick = {
                            val reply = engine.respond(input)
                            history = history + "Anda: $input" + "Ultra: ${reply.text}"
                            input = ""
                            when (reply.action) {
                                OfflineAction.OpenBrowser -> startActivity(Intent(this@OfflineAssistantActivity, DuckDuckGoBrowserActivity::class.java))
                                OfflineAction.OpenUpdateCenter -> startActivity(Intent(this@OfflineAssistantActivity, AppUpdateActivity::class.java))
                                OfflineAction.RunDiagnostics -> history = history + "Ultra: ${LocalDiagnostics.createReport(this@OfflineAssistantActivity)}"
                                OfflineAction.OpenInstalledApps -> startActivity(Intent(this@OfflineAssistantActivity, InstalledAppsActivity::class.java))
                                OfflineAction.OpenCalculator -> startActivity(Intent(this@OfflineAssistantActivity, OfflineCalculatorActivity::class.java))
                                OfflineAction.OpenFiles -> launchUtility("files")
                                OfflineAction.OpenWifiSettings -> launchUtility("wifi")
                                OfflineAction.OpenBluetoothSettings -> launchUtility("bluetooth")
                                OfflineAction.OpenAccessibilitySettings -> launchUtility("accessibility")
                                OfflineAction.OpenTimer -> launchUtility("timer")
                                OfflineAction.OpenDisplaySettings -> launchUtility("display")
                                OfflineAction.OpenSoundSettings -> launchUtility("sound")
                                OfflineAction.ShowBattery -> {
                                    val manager = getSystemService(android.content.Context.BATTERY_SERVICE) as android.os.BatteryManager
                                    val level = manager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
                                    history = history + "Ultra: Baterai perangkat ${if (level >= 0) "$level%" else "belum tersedia"}."
                                }
                                null -> Unit
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

    private fun launchUtility(key: String) {
        val intent = UtilityIntents.create(key, packageName)
        if (intent == null) {
            Toast.makeText(this, "Fitur tidak tersedia pada perangkat ini", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(this, "Tidak dapat membuka alat tersebut", Toast.LENGTH_SHORT).show()
        }
    }

}
