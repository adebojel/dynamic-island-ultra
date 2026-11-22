package com.adebojel.dynamicislandultra.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
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

private val ClipboardNight = Color(0xFF0B1222)
private val ClipboardCyan = Color(0xFF43E8FF)

class ClipboardHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        setContent {
            var history by remember { mutableStateOf(ClipboardHistoryStore.read(this)) }
            var status by remember { mutableStateOf("Clipboard hanya dibaca setelah Anda menekan Capture.") }
            MaterialTheme(colorScheme = darkColorScheme(primary = ClipboardCyan, background = ClipboardNight, surface = Color(0xFF17243A))) {
                Column(
                    Modifier.fillMaxSize().background(ClipboardNight).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("CLIPBOARD HISTORY", color = ClipboardCyan, fontSize = 19.sp)
                    Text(
                        "Simpan riwayat teks secara lokal. Aplikasi tidak memantau clipboard di latar belakang dan tidak mengambil gambar atau URI.",
                        color = Color.LightGray, fontSize = 11.sp
                    )
                    Button(onClick = {
                        val text = runCatching {
                            val clip = clipboard.primaryClip
                            if (clip == null || clip.itemCount == 0) null
                            else clip.getItemAt(0).text?.toString()
                        }.getOrNull()
                        val clean = ClipboardHistoryRules.cleanText(text)
                        if (clean == null) {
                            status = "Clipboard kosong atau tidak berisi teks biasa."
                        } else {
                            history = ClipboardHistoryStore.add(this@ClipboardHistoryActivity, clean)
                            status = "Teks disimpan ke riwayat lokal."
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Capture teks clipboard sekarang") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TERSIMPAN · ${history.size}/${ClipboardHistoryRules.MAX_ITEMS}", color = ClipboardCyan, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            ClipboardHistoryStore.clear(this@ClipboardHistoryActivity)
                            history = emptyList()
                            status = "Riwayat clipboard dihapus."
                        }, enabled = history.isNotEmpty()) { Text("Hapus semua") }
                    }
                    Surface(color = Color(0xFF17243A), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(status, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                    }
                    LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(history, key = { it.id }) { entry ->
                            Surface(color = Color(0xFF17243A), shape = RoundedCornerShape(14.dp)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Text(entry.text, color = Color.White, fontSize = 13.sp, maxLines = 6)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        TextButton(onClick = {
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Dynamic Island Ultra", entry.text))
                                            Toast.makeText(this@ClipboardHistoryActivity, "Teks disalin.", Toast.LENGTH_SHORT).show()
                                        }) { Text("Salin") }
                                        TextButton(onClick = {
                                            history = ClipboardHistoryStore.delete(this@ClipboardHistoryActivity, entry.id)
                                            status = "Item dihapus."
                                        }) { Text("Hapus") }
                                    }
                                }
                            }
                        }
                    }
                    Text("Jangan simpan kata sandi, kode OTP, data keuangan atau informasi sensitif. Riwayat dapat dihapus kapan saja dan ikut dilindungi oleh backup terenkripsi.",
                        color = Color(0xFF9BA8C1), fontSize = 10.sp)
                }
            }
        }
    }
}

data class ClipboardEntry(val id: Long, val text: String)

object ClipboardHistoryRules {
    const val MAX_ITEMS = 50
    const val MAX_TEXT_LENGTH = 10_000
    fun cleanText(value: String?): String? =
        value?.replace("\u0000", "")?.trim()?.take(MAX_TEXT_LENGTH)?.takeIf { it.isNotBlank() }
}

object ClipboardHistoryStore {
    private const val PREFS = "clipboard_history"
    private const val KEY_ENTRIES = "entries"

    fun read(context: Context): List<ClipboardEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ENTRIES, "[]")
        val array = runCatching { org.json.JSONArray(raw ?: "[]") }.getOrElse { org.json.JSONArray() }
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optLong("id")
                val text = ClipboardHistoryRules.cleanText(item.optString("text")) ?: continue
                if (id > 0L) add(ClipboardEntry(id, text))
            }
        }.sortedByDescending { it.id }
    }

    fun add(context: Context, text: String): List<ClipboardEntry> {
        val clean = ClipboardHistoryRules.cleanText(text) ?: return read(context)
        val current = read(context).filterNot { it.text == clean }
        val id = maxOf(System.currentTimeMillis(), (current.maxOfOrNull { it.id } ?: 0L) + 1L)
        val next = (listOf(ClipboardEntry(id, clean)) + current).take(ClipboardHistoryRules.MAX_ITEMS)
        write(context, next)
        return next
    }

    fun delete(context: Context, id: Long): List<ClipboardEntry> =
        read(context).filterNot { it.id == id }.also { write(context, it) }

    fun clear(context: Context) = write(context, emptyList())

    private fun write(context: Context, items: List<ClipboardEntry>) {
        val array = org.json.JSONArray()
        items.forEach { array.put(org.json.JSONObject().put("id", it.id).put("text", it.text)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ENTRIES, array.toString()).apply()
    }
}
