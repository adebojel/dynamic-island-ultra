package com.adebojel.dynamicislandultra.floating

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class QrScannerActivity : ComponentActivity() {
    private var resultText by mutableStateOf("")
    private var statusText by mutableStateOf("Scanner memakai Google Play services; aplikasi tidak meminta izin kamera.")
    private var canOpenHttps by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                Column(Modifier.fillMaxSize().background(Color(0xFF0B1222)).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("QR & BARCODE SCANNER", color = Color(0xFF43E8FF), fontSize = 18.sp)
                    Text(statusText, color = Color.LightGray, fontSize = 12.sp)
                    Button(onClick = { scanCode() }, modifier = Modifier.fillMaxWidth()) { Text("Mulai pemindaian") }
                    Text(resultText.ifBlank { "Hasil kode muncul di sini." }, color = Color.White)
                    OutlinedButton(onClick = { copyResult() }, enabled = resultText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()) { Text("Salin hasil") }
                    OutlinedButton(onClick = { openHttpsLink() }, enabled = canOpenHttps,
                        modifier = Modifier.fillMaxWidth()) { Text("Buka tautan HTTPS") }
                }
            }
        }
    }

    private fun scanCode() {
        statusText = "Menyiapkan scanner…"
        val options = GmsBarcodeScannerOptions.Builder().enableAutoZoom().allowManualInput().build()
        GmsBarcodeScanning.getClient(this, options).startScan()
            .addOnSuccessListener { barcode: Barcode ->
                resultText = barcode.rawValue.orEmpty()
                canOpenHttps = isHttps(resultText)
                statusText = if (resultText.isBlank()) "Kode tidak berisi teks."
                    else "Pemindaian berhasil. Periksa hasil sebelum menyalin atau membuka tautan."
            }
            .addOnCanceledListener { statusText = "Pemindaian dibatalkan." }
            .addOnFailureListener { error ->
                statusText = "Scanner tidak tersedia: " + (error.localizedMessage ?: "periksa Google Play services")
            }
    }

    private fun isHttps(value: String): Boolean {
        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return false
        return uri.scheme == "https" && !uri.host.isNullOrBlank()
    }

    private fun copyResult() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("QR/barcode", resultText))
        Toast.makeText(this, "Hasil disalin", Toast.LENGTH_SHORT).show()
    }

    private fun openHttpsLink() {
        if (!isHttps(resultText)) return
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resultText))) }
            .onFailure { statusText = "Tidak ada aplikasi yang dapat membuka tautan." }
    }
}
