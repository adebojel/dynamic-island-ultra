package com.adebojel.dynamicislandultra.floating

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStream
import java.net.URLConnection
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** File operations scoped to document trees explicitly selected by the user. */
internal object FileManagerOperations {
    const val MAX_ZIP_ENTRIES = 5_000
    const val MAX_ZIP_DEPTH = 32
    const val MAX_COPY_ENTRIES = 5_000
    const val MAX_COPY_DEPTH = 32
    const val MAX_EXTRACTED_BYTES = 536_870_912L
    const val MAX_EXTRACTED_FILE_BYTES = 268_435_456L

    fun copyOrMove(
        resolver: ContentResolver,
        source: DocumentFile,
        destination: DocumentFile,
        move: Boolean
    ): String {
        require((source.isFile || source.isDirectory) && source.canRead()) {
            "Sumber tidak dapat dibaca."
        }
        require(destination.isDirectory && destination.canWrite()) { "Folder tujuan tidak dapat ditulisi." }
        require(source.uri != destination.uri) { "Sumber dan tujuan tidak boleh sama." }
        if (source.isDirectory) require(!containsUri(source, destination.uri)) {
            "Folder tujuan tidak boleh berada di dalam folder sumber."
        }

        val created = mutableListOf<DocumentFile>()
        var entries = 0
        fun copyNode(node: DocumentFile, parent: DocumentFile, depth: Int) {
            require(depth <= MAX_COPY_DEPTH) {
                "Struktur folder melebihi batas $MAX_COPY_DEPTH tingkat."
            }
            require(++entries <= MAX_COPY_ENTRIES) {
                "Transfer melebihi batas $MAX_COPY_ENTRIES item."
            }
            val name = node.name?.takeIf { FileManagerRules.isValidName(it) }
                ?: error("Nama item sumber tidak valid.")
            require(parent.findFile(name) == null) { "Nama item sudah ada di folder tujuan: $name" }

            if (node.isDirectory) {
                val target = parent.createDirectory(name)
                    ?: error("Penyedia penyimpanan menolak pembuatan folder: $name")
                created.add(target)
                node.listFiles().forEach { child -> copyNode(child, target, depth + 1) }
            } else if (node.isFile) {
                require(node.canRead()) { "File sumber tidak dapat dibaca: $name" }
                val mime = node.type ?: resolver.getType(node.uri) ?: "application/octet-stream"
                val target = parent.createFile(mime, name)
                    ?: error("Penyedia penyimpanan menolak pembuatan file: $name")
                created.add(target)
                val input = resolver.openInputStream(node.uri) ?: error("File sumber tidak dapat dibuka: $name")
                val output = resolver.openOutputStream(target.uri, "wt")
                    ?: error("File tujuan tidak dapat ditulis: $name")
                input.use { sourceStream -> output.use { targetStream -> sourceStream.copyTo(targetStream) } }
            } else {
                error("Jenis item tidak didukung: $name")
            }
        }

        try {
            copyNode(source, destination, 0)
        } catch (failure: Throwable) {
            created.asReversed().forEach { runCatching { it.delete() } }
            throw failure
        }

        if (move && !source.delete()) {
            return "Salinan berhasil, tetapi penyedia penyimpanan tidak mengizinkan penghapusan sumber."
        }
        return if (move) "Folder/file berhasil dipindahkan." else "Folder/file berhasil disalin."
    }

    private fun containsUri(root: DocumentFile, targetUri: android.net.Uri): Boolean {
        var visited = 0
        fun visit(node: DocumentFile, depth: Int): Boolean {
            require(depth <= MAX_COPY_DEPTH) {
                "Struktur sumber terlalu dalam untuk memvalidasi tujuan."
            }
            require(++visited <= MAX_COPY_ENTRIES) {
                "Folder sumber terlalu besar untuk memvalidasi tujuan dengan aman."
            }
            if (node.uri == targetUri) return true
            if (!node.isDirectory) return false
            return node.listFiles().any { child -> visit(child, depth + 1) }
        }
        return visit(root, 0)
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

    fun extractZip(
        resolver: ContentResolver,
        source: DocumentFile,
        destination: DocumentFile
    ): Int {
        require(source.isFile && source.canRead()) { "Arsip ZIP tidak dapat dibaca." }
        require(destination.isDirectory && destination.canWrite()) { "Folder tujuan tidak dapat ditulisi." }
        val created = mutableListOf<DocumentFile>()
        val seen = mutableSetOf<String>()
        var entryCount = 0
        var totalBytes = 0L
        try {
            val input = resolver.openInputStream(source.uri) ?: error("Arsip ZIP tidak dapat dibuka.")
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    require(++entryCount <= MAX_ZIP_ENTRIES) {
                        "Arsip melebihi batas $MAX_ZIP_ENTRIES entri."
                    }
                    val segments = normalizeZipEntryPath(entry.name)
                        ?: error("Path ZIP tidak aman: \${entry.name.take(80)}")
                    require(segments.size <= MAX_ZIP_DEPTH) {
                        "Struktur ZIP terlalu dalam (maksimal $MAX_ZIP_DEPTH tingkat)."
                    }
                    val canonical = segments.joinToString("/")
                    require(seen.add(canonical)) { "Path ZIP ganda: $canonical" }
                    var parent = destination
                    segments.forEachIndexed { index, segment ->
                        val last = index == segments.lastIndex
                        if (last && !entry.isDirectory) {
                            require(parent.findFile(segment) == null) {
                                "File tujuan sudah ada: $segment"
                            }
                            val mime = URLConnection.guessContentTypeFromName(segment)
                                ?: "application/octet-stream"
                            val target = parent.createFile(mime, segment)
                                ?: error("Penyedia penyimpanan menolak file: $segment")
                            created.add(target)
                            var fileBytes = 0L
                            val output = resolver.openOutputStream(target.uri, "wt")
                                ?: error("Tidak dapat menulis: $segment")
                            output.use { sink ->
                                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                while (true) {
                                    val read = zip.read(buffer)
                                    if (read < 0) break
                                    fileBytes += read
                                    totalBytes += read
                                    require(fileBytes <= MAX_EXTRACTED_FILE_BYTES) {
                                        "File hasil ekstraksi melebihi batas ukuran."
                                    }
                                    require(totalBytes <= MAX_EXTRACTED_BYTES) {
                                        "Total ekstraksi melebihi batas ukuran."
                                    }
                                    sink.write(buffer, 0, read)
                                }
                            }
                        } else {
                            val existing = parent.findFile(segment)
                            if (existing != null) {
                                require(existing.isDirectory) {
                                    "Nama tujuan sudah digunakan oleh file: $segment"
                                }
                                parent = existing
                            } else {
                                val directory = parent.createDirectory(segment)
                                    ?: error("Tidak dapat membuat folder: $segment")
                                created.add(directory)
                                parent = directory
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }
            return entryCount
        } catch (failure: Throwable) {
            created.asReversed().forEach { runCatching { it.delete() } }
            throw failure
        }
    }

    internal fun normalizeZipEntryPath(rawName: String): List<String>? {
        if (rawName.isBlank() || rawName.startsWith('/') || rawName.startsWith('\\')) return null
        if (rawName.any { it.isISOControl() } || rawName.length > 2048) return null
        val segments = rawName.replace('\\', '/').split('/').filter { it.isNotEmpty() }
        if (segments.isEmpty()) return null
        if (segments.any { it == "." || it == ".." || ':' in it || !FileManagerRules.isValidName(it) }) return null
        return segments
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
