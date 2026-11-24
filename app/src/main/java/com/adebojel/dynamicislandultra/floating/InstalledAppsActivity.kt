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
import androidx.compose.ui.Alignment
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
                    Text("Buka aplikasi normal atau minta Android menjalankannya dalam freeform. Mode jendela bergantung pada dukungan versi Android dan OEM.", color = Color.LightGray)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(apps, key = { it.packageName }) { app ->
                            Surface(color = Color(0xFF18243A), onClick = { repository.launch(app.packageName) }) {
                                Row(
                                    Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(app.label, color = Color.White, modifier = Modifier.weight(1f))
                                    TextButton(onClick = { repository.launch(app.packageName) }) {
                                        Text("BUKA", color = Color(0xFF43E8FF))
                                    }
                                    OutlinedButton(onClick = {
                                        val launched = repository.launchInFreeform(app.packageName)
                                        if (!launched) {
                                            android.widget.Toast.makeText(
                                                this@InstalledAppsActivity,
                                                "Mode freeform tidak tersedia untuk aplikasi ini.",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }) {
                                        Text("FLOAT", color = Color(0xFF43E8FF))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
