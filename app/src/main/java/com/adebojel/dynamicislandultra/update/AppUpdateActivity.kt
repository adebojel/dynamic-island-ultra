package com.adebojel.dynamicislandultra.update

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.adebojel.dynamicislandultra.BuildConfig
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Checks only this public GitHub repository's latest release. The downloaded
 * APK is checksum-verified before Android's package installer is opened.
 */
class AppUpdateActivity : ComponentActivity() {
    private lateinit var status: TextView
    private lateinit var checkButton: Button
    private var pendingApk: File? = null
    private var waitingForInstallPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(android.graphics.Color.rgb(8, 11, 20))
        }
        val heading = TextView(this).apply {
            text = "GITHUB UPDATE CENTER"
            textSize = 20f
            setTextColor(android.graphics.Color.rgb(67, 232, 255))
        }
        status = TextView(this).apply {
            text = "Periksa rilis terbaru dari repository resmi Dynamic Island Ultra."
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 24, 0, 24)
        }
        checkButton = Button(this).apply {
            text = "Periksa pembaruan"
            setOnClickListener { checkForUpdate() }
        }
        val note = TextView(this).apply {
            text = "Aplikasi memiliki akses internet untuk browser, pembaruan, dan modul online yang ditambahkan kemudian. Perintah AI lokal, kalkulator, catatan dan diagnostik tetap berjalan di perangkat. Android tetap meminta persetujuan instalasi pembaruan."
            textSize = 12f
            setTextColor(android.graphics.Color.LTGRAY)
            setPadding(0, 24, 0, 0)
        }
        root.addView(heading)
        root.addView(status)
        root.addView(checkButton)
        root.addView(note)
        setContentView(root)

        if (!BuildConfig.ENABLE_GITHUB_APK_UPDATER) {
            heading.text = "GOOGLE PLAY UPDATE"
            status.text = "Versi ini diperbarui melalui Google Play. Gunakan tombol di bawah untuk membuka halaman resmi aplikasi."
            checkButton.text = "Buka Google Play"
            checkButton.setOnClickListener { openPlayStoreListing() }
            return
        }
    }

    private fun openPlayStoreListing() {
        val storeUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        val marketIntent = Intent(Intent.ACTION_VIEW, storeUri).setPackage("com.android.vending")
        try {
            startActivity(marketIntent)
        } catch (_: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, storeUri))
        }
    }

    private fun checkForUpdate() {
        if (!BuildConfig.ENABLE_GITHUB_APK_UPDATER) {
            status.text = "Pembaruan untuk versi Google Play dikelola oleh Google Play."
            return
        }
        checkButton.isEnabled = false
        status.text = "Menghubungi GitHub dan memeriksa versi…"
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { findAndDownloadUpdate() }
                if (result == null) {
                    status.text = "Aplikasi sudah menggunakan versi terbaru."
                } else {
                    pendingApk = result
                    status.text = "Pembaruan sudah diunduh dan SHA-256 cocok. Membuka pemasang Android…"
                    installVerifiedApk()
                }
            } catch (error: Exception) {
                status.text = "Pembaruan gagal: ${error.message ?: "kesalahan jaringan atau rilis"}"
            } finally {
                checkButton.isEnabled = true
            }
        }
    }

    private fun findAndDownloadUpdate(): File? {
        val releaseText = getHttpsText(
            "https://api.github.com/repos/adebojel/dynamic-island-ultra/releases/latest",
            "application/vnd.github+json"
        )
        val release = JSONObject(releaseText)
        val assets = release.getJSONArray("assets")
        fun assetUrl(name: String): String {
            for (index in 0 until assets.length()) {
                val asset = assets.getJSONObject(index)
                if (asset.optString("name") == name) return asset.getString("browser_download_url")
            }
            error("Asset $name tidak tersedia pada rilis terbaru.")
        }

        val info = JSONObject(getHttpsText(assetUrl("release-info.json")))
        val remoteCode = info.getLong("versionCode")
        val localCode = currentVersionCode()
        if (remoteCode <= localCode) return null

        val expectedHashText = getHttpsText(assetUrl("app-release.apk.sha256"))
        val expectedHash = Regex("(?i)^[a-f0-9]{64}").find(expectedHashText.trim())
            ?.value?.lowercase() ?: error("Checksum SHA-256 rilis tidak valid.")

        val apkUrl = assetUrl("app-release.apk")
        val apkUrlHost = Uri.parse(apkUrl).host.orEmpty().lowercase()
        require(apkUrl.startsWith("https://") && apkUrlHost == "github.com") {
            "Tautan APK bukan dari GitHub."
        }

        val destinationDir = File(cacheDir, "updates").apply { mkdirs() }
        val destination = File(destinationDir, "dynamic-island-${remoteCode}.apk")
        if (destination.exists()) destination.delete()
        downloadGithubAsset(apkUrl, destination, 400L * 1024L * 1024L)

        val actualHash = sha256(destination)
        if (actualHash != expectedHash) {
            destination.delete()
            error("SHA-256 APK tidak cocok. File dibuang demi keamanan.")
        }
        return destination
    }

    private fun currentVersionCode(): Long {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return if (Build.VERSION.SDK_INT >= 28) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }

    private fun getHttpsText(url: String, accept: String = "*/*"): String {
        val connection = openGithubConnection(url, accept)
        return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            .also { connection.disconnect() }
    }

    private fun downloadGithubAsset(url: String, destination: File, maxBytes: Long) {
        val connection = openGithubConnection(url, "*/*")
        try {
            val contentLength = connection.contentLengthLong
            require(contentLength <= 0L || contentLength <= maxBytes) {
                "APK melebihi batas 400 MiB."
            }
            BufferedInputStream(connection.inputStream).use { input ->
                destination.outputStream().buffered().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= maxBytes) { "APK melebihi batas 400 MiB." }
                        output.write(buffer, 0, count)
                    }
                    require(total > 0L) { "APK kosong." }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun openGithubConnection(source: String, accept: String): HttpURLConnection {
        var current = URL(source)
        repeat(6) {
            require(current.protocol == "https") { "Koneksi non-HTTPS ditolak." }
            val host = current.host.lowercase()
            require(
                host == "api.github.com" || host == "github.com" ||
                    host == "release-assets.githubusercontent.com" ||
                    host.endsWith(".githubusercontent.com")
            ) { "Host unduhan bukan host GitHub yang diizinkan." }
            val connection = current.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15_000
            connection.readTimeout = 45_000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Dynamic-Island-Ultra-Android")
            connection.setRequestProperty("Accept", accept)
            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                    ?: error("GitHub mengirim redirect tanpa lokasi.")
                connection.disconnect()
                current = URL(current, location)
            } else {
                require(code in 200..299) { "GitHub mengembalikan HTTP $code." }
                return connection
            }
        }
        error("Terlalu banyak redirect dari GitHub.")
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun installVerifiedApk() {
        if (!BuildConfig.ENABLE_GITHUB_APK_UPDATER) {
            status.text = "Pemasangan APK mandiri dinonaktifkan untuk versi Google Play."
            return
        }
        val apk = pendingApk ?: return
        if (Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
            waitingForInstallPermission = true
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName")
                )
            )
            status.text = "Izinkan pemasangan aplikasi dari sumber ini, lalu kembali ke layar update."
            return
        }
        val contentUri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            apk
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(installIntent)
    }

    override fun onResume() {
        super.onResume()
        if (waitingForInstallPermission) {
            waitingForInstallPermission = false
            if (Build.VERSION.SDK_INT < 26 || packageManager.canRequestPackageInstalls()) {
                installVerifiedApk()
            }
        }
    }
}
