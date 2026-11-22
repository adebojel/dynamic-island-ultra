package com.adebojel.dynamicislandultra.floating

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class DocumentPreviewActivity : ComponentActivity() {
    private var preview by mutableStateOf<Bitmap?>(null)
    private var status by mutableStateOf("Pilih gambar atau PDF untuk pratinjau lokal.")
    private var fileName by mutableStateOf("Belum ada berkas dipilih")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    if (uri != null) loadPickedFile(uri)
                }
                Column(Modifier.fillMaxSize().background(Color(0xFF0B1222)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("IMAGE & DOCUMENT PREVIEW", color = Color(0xFF43E8FF), fontSize = 17.sp)
                    Text(fileName, color = Color.White)
                    Text(status, color = Color.LightGray, fontSize = 11.sp)
                    Button(onClick = { picker.launch(arrayOf("image/*", "application/pdf")) },
                        modifier = Modifier.fillMaxWidth()) { Text("Pilih gambar / PDF") }
                    preview?.let { bitmap ->
                        Image(bitmap.asImageBitmap(), "Pratinjau berkas pilihan",
                            modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                            contentScale = ContentScale.Fit)
                    }
                }
            }
        }
    }

    private fun loadPickedFile(uri: Uri) {
        preview = null
        fileName = displayName(uri)
        status = "Memproses berkas lokal…"
        val name = fileName
        lifecycleScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    val mime = contentResolver.getType(uri).orEmpty()
                    if (mime == "application/pdf" || name.endsWith(".pdf", ignoreCase = true)) {
                        renderFirstPage(uri)
                    } else {
                        decodeImage(uri)
                    }
                }
            }
            loaded.onSuccess {
                preview = it
                status = "Pratinjau selesai. Berkas asli tidak diunggah."
            }.onFailure {
                status = "Gagal membuka berkas: ${it.message ?: "format tidak didukung"}"
            }
        }
    }

    private fun displayName(uri: Uri): String =
        runCatching {
            contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        }.getOrNull() ?: uri.lastPathSegment ?: "Berkas pilihan"

    private fun decodeImage(uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = contentResolver.openInputStream(uri) ?: error("Gambar tidak dapat dibaca.")
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Format gambar tidak dikenali." }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > 1800) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Gambar tidak dapat didekode.")
    }

    private fun renderFirstPage(uri: Uri): Bitmap {
        val descriptor: ParcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            ?: error("PDF tidak dapat dibuka.")
        descriptor.use { parcel ->
            PdfRenderer(parcel).use { renderer ->
                require(renderer.pageCount > 0) { "PDF tidak memiliki halaman." }
                val page = renderer.openPage(0)
                try {
                    val scale = minOf(1f, 1800f / max(page.width, page.height).coerceAtLeast(1))
                    val bitmap = Bitmap.createBitmap(
                        (page.width * scale).toInt().coerceAtLeast(1),
                        (page.height * scale).toInt().coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.eraseColor(AndroidColor.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    return bitmap
                } finally {
                    page.close()
                }
            }
        }
    }
}
