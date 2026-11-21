package com.adebojel.dynamicislandultra.floating

import android.app.Activity
import android.app.PictureInPictureParams
import android.util.Rational
import android.content.res.Configuration
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adebojel.dynamicislandultra.ai.OfflineAssistant

class OfflineNotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("offline_notes", Context.MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = ComposeColor(0xFF43E8FF))) {
                var note by remember { mutableStateOf(prefs.getString("note", "") ?: "") }
                Column(
                    Modifier.fillMaxSize().background(ComposeColor(0xFF0B1222)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("OFFLINE NOTES", color = ComposeColor(0xFF43E8FF), fontSize = 18.sp)
                    OutlinedTextField(
                        value = note,
                        onValueChange = {
                            note = it
                            prefs.edit().putString("note", it).apply()
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        label = { Text("Catatan lokal") }
                    )
                    Text("Tersimpan di perangkat ini • tanpa sinkronisasi cloud", color = ComposeColor.LightGray, fontSize = 11.sp)
                }
            }
        }
    }
}

class OfflineCalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = ComposeColor(0xFF43E8FF))) {
                var expression by remember { mutableStateOf("") }
                var result by remember { mutableStateOf("Masukkan ekspresi matematika") }
                val assistant = remember { OfflineAssistant() }
                Column(
                    Modifier.fillMaxSize().background(ComposeColor(0xFF0B1222)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("OFFLINE CALCULATOR", color = ComposeColor(0xFF43E8FF), fontSize = 16.sp)
                    OutlinedTextField(expression, { expression = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Contoh: 12 * (3 + 4)") })
                    Button(onClick = { result = assistant.respond(expression).text }, modifier = Modifier.fillMaxWidth()) { Text("Hitung") }
                    Text(result, color = ComposeColor.White, fontSize = 20.sp)
                }
            }
        }
    }
}

/** Android-managed PiP is enabled for this app's own activities, not arbitrary third-party apps. */
class FloatingWorkspaceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            setPictureInPictureParams(
                PictureInPictureParams.Builder().setAspectRatio(Rational(9, 16)).build()
            )
        }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = ComposeColor(0xFF43E8FF))) {
                Column(
                    Modifier.fillMaxSize().background(ComposeColor(0xFF0B1222)).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("FLOATING WORKSPACE", color = ComposeColor(0xFF43E8FF), fontSize = 16.sp)
                    Text("Pilih alat lokal untuk dibuka sebagai aktivitas terpisah.", color = ComposeColor.White)
                    Button(onClick = { startActivity(android.content.Intent(this@FloatingWorkspaceActivity, OfflineNotesActivity::class.java)) }) { Text("Catatan") }
                    Button(onClick = { startActivity(android.content.Intent(this@FloatingWorkspaceActivity, OfflineCalculatorActivity::class.java)) }) { Text("Kalkulator") }
                    Button(onClick = { enterWorkspacePip() }) { Text("Minimize to PiP") }
                    Text("PiP hanya berlaku pada workspace milik aplikasi ini, bukan memaksa aplikasi lain mendukung PiP.", color = ComposeColor.LightGray, fontSize = 11.sp)
                }
            }
        }
    }

    private fun enterWorkspacePip() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder().setAspectRatio(Rational(9, 16)).build()
            )
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }
}
