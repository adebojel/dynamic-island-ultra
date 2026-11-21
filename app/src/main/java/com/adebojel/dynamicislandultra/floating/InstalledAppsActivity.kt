package com.adebojel.dynamicislandultra.floating

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class InstalledAppsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = FloatingAppRepository(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                var apps by remember { mutableStateOf(repository.loadLaunchableApps()) }
                Column(Modifier.fillMaxSize().background(Color(0xFF0B1222)).padding(14.dp)) {
                    Text("APLIKASI DI PERANGKAT", color = Color(0xFF43E8FF))
                    Text("Pilih aplikasi untuk dibuka. Jendela PiP bergantung pada dukungan aplikasi tujuan.", color = Color.LightGray)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(apps, key = { it.packageName }) { app ->
                            Surface(color = Color(0xFF18243A), onClick = { repository.launch(app.packageName) }) {
                                Row(Modifier.fillMaxWidth().padding(10.dp)) {
                                    Text(app.label, color = Color.White, modifier = Modifier.weight(1f))
                                    Text("BUKA", color = Color(0xFF43E8FF))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
