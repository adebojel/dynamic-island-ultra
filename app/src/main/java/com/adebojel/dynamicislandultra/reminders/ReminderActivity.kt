package com.adebojel.dynamicislandultra.reminders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Date

private val ReminderNight = Color(0xFF0B1222)
private val ReminderCyan = Color(0xFF43E8FF)

class ReminderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var title by remember { mutableStateOf("Pengingat") }
            var minutes by remember { mutableStateOf("15") }
            var reminders by remember { mutableStateOf(ReminderScheduler.pending(this)) }
            var status by remember { mutableStateOf("Pengingat berjalan lokal; tidak memerlukan jaringan.") }
            MaterialTheme(colorScheme = darkColorScheme(primary = ReminderCyan, background = ReminderNight, surface = Color(0xFF17243A))) {
                Column(Modifier.fillMaxSize().background(ReminderNight).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PERSISTENT REMINDERS", color = ReminderCyan, fontSize = 19.sp)
                    Text("Pengingat tetap dijadwalkan setelah aplikasi ditutup dan dijadwalkan ulang setelah reboot. Android dapat menunda notifikasi ketika Doze aktif; ini bukan alarm exact.",
                        color = Color.LightGray, fontSize = 11.sp)
                    OutlinedTextField(value = title, onValueChange = { title = it.take(ReminderRules.MAX_TITLE_LENGTH) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Judul pengingat") }, singleLine = true)
                    OutlinedTextField(value = minutes, onValueChange = { minutes = it.filter(Char::isDigit).take(5) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Durasi (menit, 1–10080)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 15, 60).forEach { value ->
                            Button(onClick = { addReminder(title, value, { reminders = ReminderScheduler.pending(this@ReminderActivity) }, { status = it }) },
                                modifier = Modifier.weight(1f)) { Text(if (value == 60) "1 jam" else "$value menit") }
                        }
                    }
                    Button(onClick = { addReminder(title, minutes.toIntOrNull() ?: -1,
                        { reminders = ReminderScheduler.pending(this@ReminderActivity) }, { status = it }) },
                        modifier = Modifier.fillMaxWidth()) { Text("Jadwalkan pengingat") }
                    Surface(color = Color(0xFF17243A), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(status, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(12.dp))
                    }
                    Text("PENGINGAT AKTIF · ${reminders.size}", color = ReminderCyan, fontSize = 12.sp)
                    LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(reminders, key = { it.id }) { item ->
                            Surface(color = Color(0xFF17243A), shape = RoundedCornerShape(14.dp)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.title, color = Color.White)
                                        Text(DateFormat.getDateTimeInstance().format(Date(item.triggerAt)), color = Color.LightGray, fontSize = 10.sp)
                                    }
                                    TextButton(onClick = {
                                        ReminderScheduler.cancel(this@ReminderActivity, item.id)
                                        reminders = ReminderScheduler.pending(this@ReminderActivity)
                                        status = "Pengingat dibatalkan."
                                    }) { Text("Batalkan") }
                                }
                            }
                        }
                    }
                    Text("Notifikasi membutuhkan izin POST_NOTIFICATIONS pada Android 13+. Force-stop dapat menonaktifkan pengiriman hingga aplikasi dibuka kembali.",
                        color = Color(0xFF9BA8C1), fontSize = 10.sp)
                }
            }
        }
    }

    private fun addReminder(title: String, minutes: Int, refresh: () -> Unit, report: (String) -> Unit) {
        runCatching { ReminderScheduler.schedule(this, title, minutes) }
            .onSuccess { refresh(); report("Pengingat dijadwalkan.") }
            .onFailure { report(it.message ?: "Pengingat tidak dapat dijadwalkan.") }
    }
}
