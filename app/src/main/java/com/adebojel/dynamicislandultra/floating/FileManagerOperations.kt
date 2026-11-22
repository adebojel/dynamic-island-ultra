package com.adebojel.dynamicislandultra.floating

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** File operations scoped to document trees explicitly selected by the user. */
internal object FileManagerOperations {
    const val MAX_ZIP_ENTRIES = 5_000
    const val MAX_ZIP_DEPTH = 32

    fun copyOrMove(
        resolver: ContentResolver,
        source: DocumentFile,
        destination: DocumentFile,
        move: Boolean
    ): String {
        require(source.isFile && source.canRead()) { "File sumber tidak dapat dibaca." }
        require(destination.isDirectory && destination.canWrite()) { "Folder tujuan tidak dapat ditulisi." }
        val name = source.name?.takeIf { FileManagerRules.isValidName(it) }
            ?: error("Nama file sumber tidak valid.")
        require(destination.findFile(name) == null) { "Nama file sudah ada di folder tujuan." }
        val mime = source.type ?: resolver.getType(source.uri) ?: "application/octet-stream"
        val target = destination.createFile(mime, name)
            ?: error("Penyedia penyimpanan menolak pembuatan file.")
        try {
            val input = resolver.openInputStream(source.uri) ?: error("File sumber tidak dapat dibuka.")
            val output = resolver.openOutputStream(target.uri, "wt") ?: error("File tujuan tidak dapat ditulis.")
            input.use { sourceStream ->
                output.use { targetStream -> sourceStream.copyTo(targetStream) }
            }
        } catch (failure: Throwable) {
            target.delete()
            throw failure
        }
        if (move && !source.delete()) {
            return "File disalin, tetapi file asli tidak dapat dihapus oleh penyedia penyimpanan."
        }
        return if (move) "File berhasil dipindahkan." else "File berhasil disalin."
    }

    fun createZip(resolver: ContentResolver, source: DocumentFile, output: OutputStream): Int {
        require(source.canRead()) { "Sumber tidak dapat dibaca." }
        val seenPaths = mutableSetOf<String>()
        var count = 0
        ZipOutputStream(output.buffered()).use { zip ->
            appendNode(resolver, source, "", zip, seenPaths, 0) { count++ }
        }
        return count
    }

    internal fun safeZipEntryName(rawName: String): String =
        rawName.replace('/', '_')
            .replace('\\', '_')
            .filterNot { it.isISOControl() }
            .take(180)
            .ifBlank { "item" }

    private fun appendNode(
        resolver: ContentResolver,
        node: DocumentFile,
        parentPath: String,
        zip: ZipOutputStream,
        seenPaths: MutableSet<String>,
        depth: Int,
        onEntry: () -> Unit
    ) {
        require(depth <= MAX_ZIP_DEPTH) { "Struktur folder terlalu dalam (maksimal $MAX_ZIP_DEPTH tingkat)." }
        require(seenPaths.size < MAX_ZIP_ENTRIES) { "Arsip melebihi batas $MAX_ZIP_ENTRIES entri." }
        val name = safeZipEntryName(node.name.orEmpty())
        val path = parentPath + name + if (node.isDirectory) "/" else ""
        require(seenPaths.add(path)) { "Nama item ganda di dalam ZIP: $name" }
        onEntry()
        if (node.isDirectory) {
            zip.putNextEntry(ZipEntry(path))
            zip.closeEntry()
            node.listFiles().forEach { child ->
                appendNode(resolver, child, path, zip, seenPaths, depth + 1, onEntry)
            }
        } else {
            zip.putNextEntry(ZipEntry(path))
            val input = resolver.openInputStream(node.uri) ?: error("File tidak dapat dibaca: $name")
            input.use { it.copyTo(zip) }
            zip.closeEntry()
        }
    }
}
