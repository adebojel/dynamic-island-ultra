package com.adebojel.dynamicislandultra.backup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.adebojel.dynamicislandultra.overlay.FloatingIslandService
import com.adebojel.dynamicislandultra.reminders.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val BackupNight = Color(0xFF0B1222)
private val BackupCyan = Color(0xFF43E8FF)

class BackupActivity : ComponentActivity() {
    private var activePassword = charArrayOf()
    private var status by mutableStateOf("Backup dienkripsi di perangkat; tidak ada unggahan cloud.")
    private var pendingRestore by mutableStateOf<ByteArray?>(null)

    private val createBackup = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) exportTo(uri)
    }

    private val openBackup = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) importFrom(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var password by remember { mutableStateOf("") }
            MaterialTheme(colorScheme = darkColorScheme(primary = BackupCyan, background = BackupNight, surface = Color(0xFF17243A))) {
                Box(Modifier.fillMaxSize().background(BackupNight)) {
                    Column(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("ENCRYPTED BACKUP", color = BackupCyan, fontSize = 20.sp)
                        Text(
                            "Simpan dan pulihkan data lokal memakai berkas .diub dan kata sandi Anda. Proses berjalan di perangkat; data tidak dikirim ke server.",
                            color = Color.LightGray, fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Kata sandi backup (minimal 8 karakter)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Button(
                            onClick = {
                                if (password.length < BackupCrypto.MIN_PASSWORD_LENGTH) {
                                    status = "Gunakan kata sandi minimal 8 karakter."
                                } else {
                                    activePassword = password.toCharArray()
                                    createBackup.launch("dynamic-island-ultra-backup.diub")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Buat backup terenkripsi") }
                        OutlinedButton(
                            onClick = {
                                if (password.length < BackupCrypto.MIN_PASSWORD_LENGTH) {
                                    status = "Masukkan kata sandi backup terlebih dahulu."
                                } else {
                                    activePassword = password.toCharArray()
                                    openBackup.launch(arrayOf("application/octet-stream", "application/*", "*/*"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Pulihkan backup") }
                        Surface(color = Color(0xFF17243A), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(status, color = Color.White, modifier = Modifier.padding(14.dp), fontSize = 12.sp)
                        }
                        Text(
                            "Termasuk: catatan, preferensi Island, pengaturan privasi notifikasi, bookmark dan riwayat browser. Tidak termasuk: entitlement Premium/Pembelian, izin Android, akses folder SAF, file media atau data aplikasi lain. Simpan kata sandi dengan aman; backup tidak dapat dibuka tanpa kata sandi.",
                            color = Color(0xFF9BA8C1), fontSize = 10.sp
                        )
                    }

                    val payload = pendingRestore
                    if (payload != null) {
                        AlertDialog(
                            onDismissRequest = { pendingRestore = null },
                            title = { Text("Pulihkan backup?") },
                            text = {
                                Text("Data lokal yang termasuk dalam backup akan menggantikan data saat ini. Island akan dihentikan dan dinonaktifkan. Pembelian Premium dan izin Android tidak diubah.")
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    runCatching {
                                        stopService(Intent(this@BackupActivity, FloatingIslandService::class.java))
                                        getSharedPreferences("island", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
                                        ReminderScheduler.pending(this@BackupActivity).forEach { ReminderScheduler.cancel(this@BackupActivity, it.id) }
                                        BackupArchive.restore(this@BackupActivity, payload)
                                        ReminderScheduler.rescheduleAll(this@BackupActivity)
                                    }.onSuccess {
                                        status = "Backup dipulihkan. Periksa kembali izin sistem dan mulai Island secara manual."
                                        Toast.makeText(this@BackupActivity, "Backup berhasil dipulihkan.", Toast.LENGTH_LONG).show()
                                    }.onFailure {
                                        status = "Pemulihan gagal: ${it.message ?: "data tidak valid"}"
                                    }
                                    pendingRestore = null
                                }) { Text("Pulihkan") }
                            },
                            dismissButton = {
                                TextButton(onClick = { pendingRestore = null }) { Text("Batal") }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun exportTo(uri: Uri) {
        val password = activePassword.copyOf()
        lifecycleScope.launch {
            status = "Membuat backup terenkripsi…"
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val plain = BackupArchive.create(this@BackupActivity)
                    val encrypted = try { BackupCrypto.encrypt(plain, password) } finally { plain.fill(0) }
                    val output = contentResolver.openOutputStream(uri, "wt")
                        ?: error("Penyimpanan tujuan tidak dapat dibuka.")
                    output.use { it.write(encrypted) }
                    encrypted.size
                }
            }
            password.fill('\u0000')
            activePassword.fill('\u0000')
            activePassword = charArrayOf()
            status = result.fold(
                { "Backup tersimpan. Ukuran berkas terenkripsi: $it byte." },
                { "Backup gagal: ${it.message ?: "kesalahan penyimpanan"}" }
            )
        }
    }

    private fun importFrom(uri: Uri) {
        val password = activePassword.copyOf()
        lifecycleScope.launch {
            status = "Memeriksa dan mendekripsi backup…"
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val input = contentResolver.openInputStream(uri)
                        ?: error("Berkas backup tidak dapat dibuka.")
                    val encrypted = input.use { it.readBytes() }
                    BackupCrypto.decrypt(encrypted, password)
                }
            }
            password.fill('\u0000')
            activePassword.fill('\u0000')
            activePassword = charArrayOf()
            result.onSuccess {
                pendingRestore = it
                status = "Backup valid. Konfirmasi pemulihan untuk mengganti preferensi dan catatan yang termasuk."
            }.onFailure {
                status = "Tidak dapat membuka backup. Periksa kata sandi dan keutuhan berkas. (${it.javaClass.simpleName})"
            }
        }
    }
}
