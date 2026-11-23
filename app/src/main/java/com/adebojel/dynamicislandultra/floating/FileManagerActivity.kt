package com.adebojel.dynamicislandultra.floating

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FileNight = Color(0xFF0B1222)
private val FileCyan = Color(0xFF43E8FF)
private val FilePanel = Color(0xFF17243A)

/**
 * A scoped file manager built on Android's Storage Access Framework.
 * The user selects the folder explicitly; no broad storage permission is requested.
 */
class FileManagerActivity : ComponentActivity() {
    private var selectedRoot by mutableStateOf<DocumentFile?>(null)
    private var pendingTransfer: DocumentFile? = null
    private var pendingMove = false
    private var pendingZip: DocumentFile? = null
    private var pendingZipExtract: DocumentFile? = null

    private val folderPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(KEY_TREE_URI, uri.toString()).apply()
                selectedRoot = DocumentFile.fromTreeUri(this, uri)
            }.onFailure {
                Toast.makeText(this, "Folder tidak dapat diakses: ${it.message ?: "izin tidak tersedia"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val destinationPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        val source = pendingTransfer
        pendingTransfer = null
        if (uri != null && source != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val target = DocumentFile.fromTreeUri(this, uri) ?: error("Folder tujuan tidak dapat dibuka.")
                performTransfer(source, target, pendingMove)
            }.onFailure {
                Toast.makeText(this, "Transfer gagal: ${it.message ?: "folder tujuan tidak tersedia"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val zipPicker = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        val source = pendingZip
        pendingZip = null
        if (uri != null && source != null) performZip(source, uri)
    }

    private val extractDestinationPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        val source = pendingZipExtract
        pendingZipExtract = null
        if (uri != null && source != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val destination = DocumentFile.fromTreeUri(this, uri)
                    ?: error("Folder tujuan tidak dapat dibuka.")
                performExtractZip(source, destination)
            }.onFailure {
                Toast.makeText(this, "Ekstraksi gagal: ${it.message ?: "folder tujuan tidak tersedia"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedUri = getSharedPreferences(PREFS, MODE_PRIVATE)
            .getString(KEY_TREE_URI, null)?.let(Uri::parse)
        selectedRoot = savedUri?.let { uri ->
            runCatching { DocumentFile.fromTreeUri(this, uri) }.getOrNull()
                ?.takeIf { it.canRead() }
        }
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = FileCyan,
                    background = FileNight,
                    surface = FilePanel,
                    onSurface = Color.White
                )
            ) {
                FileManagerScreen(
                    root = selectedRoot,
                    onChooseFolder = { folderPicker.launch(savedUri) },
                    onOpenFile = ::openFile,
                    onShareFile = ::shareFile,
                    onTransferFile = ::requestTransfer,
                    onZipItem = ::requestZip,
                    onExtractZip = ::requestExtractZip
                )
            }
        }
    }

    private fun requestTransfer(source: DocumentFile, move: Boolean) {
        if (!source.isFile) {
            Toast.makeText(this, "Salin/pindahkan saat ini hanya tersedia untuk file. Folder dapat diekspor sebagai ZIP.", Toast.LENGTH_LONG).show()
            return
        }
        pendingTransfer = source
        pendingMove = move
        destinationPicker.launch(selectedRoot?.uri)
    }

    private fun performTransfer(source: DocumentFile, destination: DocumentFile, move: Boolean) {
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    FileManagerOperations.copyOrMove(contentResolver, source, destination, move)
                }
            }
            result.onSuccess { Toast.makeText(this@FileManagerActivity, it, Toast.LENGTH_LONG).show() }
                .onFailure { Toast.makeText(this@FileManagerActivity, "Transfer gagal: ${it.message ?: "kesalahan penyimpanan"}", Toast.LENGTH_LONG).show() }
        }
    }

    private fun requestZip(source: DocumentFile) {
        pendingZip = source
        val name = source.name ?: "arsip"
        zipPicker.launch("${name.substringBeforeLast('.', name)}.zip")
    }

    private fun performZip(source: DocumentFile, destination: Uri) {
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val output = contentResolver.openOutputStream(destination, "wt")
                        ?: error("Berkas ZIP tujuan tidak dapat dibuat.")
                    FileManagerOperations.createZip(contentResolver, source, output)
                }
            }
            result.onSuccess { Toast.makeText(this@FileManagerActivity, "ZIP berhasil dibuat ($it entri).", Toast.LENGTH_LONG).show() }
                .onFailure { Toast.makeText(this@FileManagerActivity, "ZIP gagal: ${it.message ?: "kesalahan penyimpanan"}", Toast.LENGTH_LONG).show() }
        }
    }

    private fun requestExtractZip(source: DocumentFile) {
        pendingZipExtract = source
        extractDestinationPicker.launch(selectedRoot?.uri)
    }

    private fun performExtractZip(source: DocumentFile, destination: DocumentFile) {
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    FileManagerOperations.extractZip(contentResolver, source, destination)
                }
            }
            result.onSuccess {
                Toast.makeText(this@FileManagerActivity, "Ekstraksi selesai ($it entri).", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(this@FileManagerActivity, "Ekstraksi gagal: ${it.message ?: "arsip tidak valid"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openFile(file: DocumentFile) {
        val mime = file.type ?: contentResolver.getType(file.uri) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { startActivity(Intent.createChooser(intent, "Buka file")) }
            .onFailure {
                Toast.makeText(this, "Tidak ada aplikasi yang dapat membuka file ini", Toast.LENGTH_SHORT).show()
            }
    }

    private fun shareFile(file: DocumentFile) {
        val mime = file.type ?: contentResolver.getType(file.uri) ?: "*/*"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, file.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newUri(contentResolver, file.name ?: "file", file.uri)
        }
        runCatching { startActivity(Intent.createChooser(intent, "Bagikan file")) }
            .onFailure {
                Toast.makeText(this, "File tidak dapat dibagikan", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val PREFS = "file_manager"
        private const val KEY_TREE_URI = "selected_tree_uri"
    }
}

@Composable
private fun FileManagerScreen(
    root: DocumentFile?,
    onChooseFolder: () -> Unit,
    onOpenFile: (DocumentFile) -> Unit,
    onShareFile: (DocumentFile) -> Unit,
    onTransferFile: (DocumentFile, Boolean) -> Unit,
    onZipItem: (DocumentFile) -> Unit,
    onExtractZip: (DocumentFile) -> Unit
) {
    val stack: SnapshotStateList<DocumentFile> = remember(root?.uri) {
        androidx.compose.runtime.mutableStateListOf<DocumentFile>().apply {
            if (root != null) add(root)
        }
    }
    val current = stack.lastOrNull()
    var query by remember { mutableStateOf("") }
    var revision by remember { mutableIntStateOf(0) }
    var sortNewest by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<DocumentFile?>(null) }
    var renameTarget by remember { mutableStateOf<DocumentFile?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<DocumentFile?>(null) }
    var showCreateFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    val entries = remember(current?.uri, revision, query, sortNewest) {
        val folderFirst = compareBy<DocumentFile> { !it.isDirectory }
        val matching = current?.listFiles()?.filter { file ->
            file.name.orEmpty().contains(query.trim(), ignoreCase = true)
        }.orEmpty()
        if (sortNewest) {
            matching.sortedWith(
                folderFirst.thenByDescending { it.lastModified() }
                    .thenBy { it.name.orEmpty().lowercase() }
            )
        } else {
            matching.sortedWith(folderFirst.thenBy { it.name.orEmpty().lowercase() })
        }
    }

    Column(
        Modifier.fillMaxSize().background(FileNight).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("FILE MANAGER", color = FileCyan, fontSize = 19.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f))
            TextButton(onClick = onChooseFolder) { Text("Pilih folder") }
        }

        if (current == null) {
            Surface(color = FilePanel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Folder, null, tint = FileCyan, modifier = Modifier.size(42.dp))
                    Text("Pilih folder yang ingin dikelola", color = Color.White, fontSize = 18.sp)
                    Text(
                        "Akses dibatasi pada folder yang Anda pilih melalui pemilih resmi Android. Aplikasi tidak meminta izin mengakses seluruh penyimpanan.",
                        color = Color.LightGray, fontSize = 12.sp
                    )
                    Button(onClick = onChooseFolder, modifier = Modifier.fillMaxWidth()) {
                        Text("Pilih folder penyimpanan")
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = if (stack.size > 1) FileCyan else Color.Gray)
                }
                Column(Modifier.weight(1f)) {
                    Text(current.name ?: "Folder dipilih", color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("${entries.size} item • akses folder terpilih", color = Color.LightGray, fontSize = 10.sp)
                }
                IconButton(onClick = { sortNewest = !sortNewest }) {
                    Text(if (sortNewest) "Terbaru" else "Nama", color = FileCyan, fontSize = 10.sp)
                }
                IconButton(onClick = { showCreateFolder = true }) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Buat folder", tint = FileCyan)
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Cari file atau folder") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (entries.isEmpty()) {
                Text(
                    if (query.isBlank()) "Folder ini kosong." else "Tidak ada hasil untuk pencarian ini.",
                    color = Color.LightGray, modifier = Modifier.padding(12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(entries, key = { it.uri.toString() }) { entry ->
                        Surface(
                            color = FilePanel,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (entry.isDirectory) {
                                    stack.add(entry)
                                    query = ""
                                } else {
                                    onOpenFile(entry)
                                }
                            }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (entry.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                                    contentDescription = if (entry.isDirectory) "Folder" else "File",
                                    tint = FileCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                    Text(entry.name ?: "(tanpa nama)", color = Color.White, maxLines = 1)
                                    Text(
                                        if (entry.isDirectory) "Folder" else "${formatFileSize(entry.length())} • ${entry.type ?: "File"}",
                                        color = Color.LightGray, fontSize = 10.sp, maxLines = 1
                                    )
                                }
                                IconButton(onClick = { selected = entry }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Aksi file", tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
        Text(
            "Lokal • SAF • Tanpa izin penyimpanan luas",
            color = Color(0xFF8FA3C2), fontSize = 10.sp
        )
    }

    if (selected != null) {
        val target = selected!!
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(target.name ?: "Item") },
            text = {
                Text(
                    if (target.isDirectory) "Folder • diubah: ${target.lastModified()}"
                    else "Ukuran: ${formatFileSize(target.length())}\nTipe: ${target.type ?: "tidak diketahui"}\nDiubah: ${target.lastModified()}"
                )
            },
            confirmButton = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (!target.isDirectory) {
                            TextButton(onClick = { onShareFile(target); selected = null }) { Text("Bagikan") }
                            TextButton(onClick = { onTransferFile(target, false); selected = null }) { Text("Salin") }
                            TextButton(onClick = { onTransferFile(target, true); selected = null }) { Text("Pindahkan") }
                        }
                        TextButton(onClick = { onZipItem(target); selected = null }) { Text("ZIP") }
                        if (!target.isDirectory && target.name?.endsWith(".zip", ignoreCase = true) == true) {
                            TextButton(onClick = { onExtractZip(target); selected = null }) { Text("Ekstrak ZIP") }
                        }
                    }
                    TextButton(onClick = {
                        renameTarget = target
                        renameValue = target.name.orEmpty()
                        selected = null
                    }) { Text("Ganti nama") }
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { deleteTarget = target; selected = null }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = { selected = null }) { Text("Tutup") }
                }
            }
        )
    }

    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Ganti nama") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("Nama baru") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = renameTarget
                    if (FileManagerRules.isValidName(renameValue) && target?.renameTo(renameValue.trim()) == true) {
                        revision++
                    }
                    renameTarget = null
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Batal") } }
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Hapus item?") },
            text = { Text("Item akan dihapus dari penyedia penyimpanan yang dipilih. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget?.delete()
                    deleteTarget = null
                    revision++
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Batal") } }
        )
    }

    if (showCreateFolder) {
        AlertDialog(
            onDismissRequest = { showCreateFolder = false },
            title = { Text("Folder baru") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Nama folder") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (FileManagerRules.isValidName(newFolderName)) {
                        current?.createDirectory(newFolderName.trim())
                        revision++
                    }
                    newFolderName = ""
                    showCreateFolder = false
                }) { Text("Buat") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolder = false; newFolderName = "" }) { Text("Batal") }
            }
        )
    }
}

internal object FileManagerRules {
    fun isValidName(value: String): Boolean {
        val name = value.trim()
        return name.isNotEmpty() && name != "." && name != ".." &&
            name.none { it == '/' || it == '\\' || it.isISOControl() }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 0L) return "Ukuran tidak tersedia"
    if (bytes < 1_024L) return "$bytes B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    do {
        value /= 1_024.0
        index++
    } while (value >= 1_024.0 && index < units.lastIndex)
    return "%.1f %s".format(value, units[index])
}
