package com.adebojel.dynamicislandultra.browser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.http.SslError
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject

/**
 * General HTTPS browser with DuckDuckGo as its default home/search engine.
 * Desktop mode is implemented by switching WebView's User-Agent and viewport
 * settings. All local notes and assistant functions remain completely offline.
 */
class DuckDuckGoBrowserActivity : ComponentActivity() {
    private var browser: WebView? = null
    private lateinit var address: EditText
    private lateinit var modeButton: Button
    private lateinit var progress: ProgressBar
    private var desktopMode = false
    private var mobileUserAgent = ""
    private var currentPageTitle = "DuckDuckGo"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        desktopMode = getSharedPreferences(PREFS, MODE_PRIVATE)
            .getBoolean(KEY_DESKTOP_MODE, false)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 11, 20))
            setPadding(10, 10, 10, 8)
        }

        val heading = TextView(this).apply {
            text = "DUCKDUCKGO • PRIVATE SEARCH"
            setTextColor(Color.rgb(67, 232, 255))
            textSize = 14f
            gravity = Gravity.CENTER_VERTICAL
            setPadding(4, 3, 4, 8)
        }

        val addressRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        address = EditText(this).apply {
            hint = "Cari di DuckDuckGo atau masukkan alamat HTTPS"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            textSize = 13f
            setSingleLine(true)
            setBackgroundColor(Color.rgb(24, 36, 58))
            setPadding(12, 4, 12, 4)
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
                    navigateFromAddress()
                    true
                } else false
            }
        }
        val goButton = Button(this).apply {
            text = "Go"
            setOnClickListener { navigateFromAddress() }
        }
        addressRow.addView(address, LinearLayout.LayoutParams(0, 52, 1f))
        addressRow.addView(goButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 52
        ))

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun addControl(label: String, action: () -> Unit) {
            controls.addView(Button(this).apply {
                text = label
                textSize = 10f
                setPadding(3, 0, 3, 0)
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(0, 44, 1f))
        }

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            visibility = ProgressBar.GONE
        }

        val web = WebView(this)
        browser = web
        mobileUserAgent = web.settings.userAgentString
        web.setBackgroundColor(Color.WHITE)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            if (android.os.Build.VERSION.SDK_INT >= 26) safeBrowsingEnabled = true
        }
        applyBrowserMode(web)

        val browserActions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun addBrowserAction(label: String, action: () -> Unit) {
            browserActions.addView(Button(this).apply {
                text = label
                textSize = 9f
                setPadding(2, 0, 2, 0)
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(0, 42, 1f))
        }
        addBrowserAction("Bookmark") { saveCurrentBookmark() }
        addBrowserAction("Bookmarks") { showSavedPages(KEY_BOOKMARKS, "Bookmarks") }
        addBrowserAction("History") { showSavedPages(KEY_HISTORY, "Riwayat", allowClear = true) }
        addBrowserAction("Downloads") { openDownloads() }

        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress in 1..99) ProgressBar.VISIBLE else ProgressBar.GONE
            }

            override fun onReceivedTitle(view: WebView, pageTitle: String?) {
                if (!pageTitle.isNullOrBlank()) {
                    currentPageTitle = pageTitle.take(120)
                    heading.text = currentPageTitle.take(72)
                }
            }
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val scheme = request.url.scheme?.lowercase()
                if (scheme == "https") return false
                if (scheme == "http") {
                    val upgraded = request.url.buildUpon().scheme("https").build()
                    view.loadUrl(upgraded.toString())
                    return true
                }
                Toast.makeText(
                    this@DuckDuckGoBrowserActivity,
                    "Browser hanya membuka alamat web HTTP/HTTPS.",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                address.setText(url.orEmpty())
                progress.visibility = ProgressBar.VISIBLE
                progress.progress = 0
            }

            override fun onPageFinished(view: WebView, url: String?) {
                if (!url.isNullOrBlank()) {
                    address.setText(url)
                    recordHistory(url, currentPageTitle)
                }
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                // Never bypass TLS certificate failures.
                handler.cancel()
            }
        }

        web.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            enqueueDownload(url, userAgent, contentDisposition, mimeType)
        }

        addControl("Back") { if (web.canGoBack()) web.goBack() }
        addControl("Next") { if (web.canGoForward()) web.goForward() }
        addControl("Reload") { web.reload() }
        addControl("Home") { web.loadUrl(DUCKDUCKGO_HOME) }
        modeButton = Button(this).apply {
            setOnClickListener { toggleDesktopMode(web) }
        }
        controls.addView(modeButton, LinearLayout.LayoutParams(0, 44, 1.2f))
        updateModeLabel()

        root.addView(heading)
        root.addView(addressRow)
        root.addView(controls)
        root.addView(browserActions)
        root.addView(progress, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 3
        ))
        root.addView(web, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)

        web.loadUrl(DUCKDUCKGO_HOME)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (web.canGoBack()) web.goBack() else finish()
            }
        })
    }

    private fun navigateFromAddress() {
        val input = address.text.toString().trim()
        if (input.isEmpty()) {
            browser?.loadUrl(DUCKDUCKGO_HOME)
            return
        }
        val explicit = Uri.parse(input)
        val hasWebScheme = explicit.scheme.equals("https", true) ||
            explicit.scheme.equals("http", true)
        val looksLikeHost = input.contains('.') && !input.contains(' ') &&
            !input.startsWith("javascript:", true) &&
            !input.startsWith("file:", true)
        val target = when {
            hasWebScheme && explicit.scheme.equals("https", true) -> input
            hasWebScheme && explicit.scheme.equals("http", true) ->
                explicit.buildUpon().scheme("https").build().toString()
            looksLikeHost -> "https://$input"
            else -> DUCKDUCKGO_SEARCH + URLEncoder.encode(input, Charsets.UTF_8.name())
        }
        browser?.loadUrl(target)
        address.clearFocus()
    }

    private fun enqueueDownload(url: String, userAgent: String, contentDisposition: String?, mimeType: String?) {
        if (!url.startsWith("https://", true)) {
            Toast.makeText(this, "Unduhan hanya diizinkan dari HTTPS.", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            val filename = URLUtil.guessFileName(url, contentDisposition, mimeType).take(120).ifBlank { "download" }
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                CookieManager.getInstance().getCookie(url)?.takeIf { it.isNotBlank() }?.let {
                    addRequestHeader("Cookie", it)
                }
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(false)
                setDestinationInExternalFilesDir(this@DuckDuckGoBrowserActivity, Environment.DIRECTORY_DOWNLOADS, filename)
            }
            val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(this, "Unduhan dimulai: $filename", Toast.LENGTH_LONG).show()
        }.onFailure {
            Toast.makeText(this, "Unduhan gagal dimulai: ${it.message ?: "kesalahan sistem"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openDownloads() {
        runCatching {
            startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
        }.onFailure {
            Toast.makeText(this, "Daftar unduhan Android tidak tersedia.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCurrentBookmark() {
        val url = browser?.url.orEmpty()
        if (!url.startsWith("https://", true)) {
            Toast.makeText(this, "Buka halaman HTTPS terlebih dahulu.", Toast.LENGTH_SHORT).show()
            return
        }
        val entries = readEntries(KEY_BOOKMARKS)
        val existing = (0 until entries.length()).firstOrNull {
            entries.optJSONObject(it)?.optString("url") == url
        }
        if (existing != null) entries.remove(existing)
        entries.put(JSONObject().apply {
            put("url", url)
            put("title", currentPageTitle)
            put("timestamp", System.currentTimeMillis())
        })
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_BOOKMARKS, entries.toString()).apply()
        Toast.makeText(this, "Bookmark disimpan di perangkat.", Toast.LENGTH_SHORT).show()
    }

    private fun recordHistory(url: String, title: String) {
        if (!url.startsWith("https://", true)) return
        val entries = readEntries(KEY_HISTORY)
        val duplicate = (0 until entries.length()).firstOrNull {
            entries.optJSONObject(it)?.optString("url") == url
        }
        if (duplicate != null) entries.remove(duplicate)
        entries.put(JSONObject().apply {
            put("url", url)
            put("title", title.ifBlank { url })
            put("timestamp", System.currentTimeMillis())
        })
        while (entries.length() > MAX_HISTORY_ITEMS) entries.remove(0)
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_HISTORY, entries.toString()).apply()
    }

    private fun readEntries(key: String): JSONArray {
        val raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(key, "[]")
        return runCatching { JSONArray(raw ?: "[]") }.getOrElse { JSONArray() }
    }

    private fun showSavedPages(key: String, title: String, allowClear: Boolean = false) {
        val entries = readEntries(key)
        if (entries.length() == 0) {
            Toast.makeText(this, "Belum ada data $title.", Toast.LENGTH_SHORT).show()
            return
        }
        val labels = Array(entries.length()) { index ->
            val item = entries.optJSONObject(index) ?: JSONObject()
            "${item.optString("title", "Halaman")}\\n${item.optString("url")}"
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(labels) { _, which ->
                entries.optJSONObject(which)?.optString("url")
                    ?.takeIf { it.startsWith("https://", true) }
                    ?.let { browser?.loadUrl(it) }
            }
            .setNegativeButton("Tutup", null)
        if (allowClear) {
            dialog.setNeutralButton("Hapus riwayat") { _, _ ->
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(key).apply()
                Toast.makeText(this, "Riwayat dihapus.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun toggleDesktopMode(web: WebView) {
        desktopMode = !desktopMode
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
            .putBoolean(KEY_DESKTOP_MODE, desktopMode).apply()
        applyBrowserMode(web)
        updateModeLabel()
        web.reload()
    }

    private fun applyBrowserMode(web: WebView) {
        web.settings.apply {
            if (desktopMode) {
                userAgentString = DESKTOP_USER_AGENT
                useWideViewPort = true
                loadWithOverviewMode = true
            } else {
                userAgentString = mobileUserAgent
                useWideViewPort = true
                loadWithOverviewMode = false
            }
        }
    }

    private fun updateModeLabel() {
        modeButton.text = if (desktopMode) "Desktop ✓" else "Mobile"
    }

    override fun onDestroy() {
        browser?.apply {
            stopLoading()
            loadUrl("about:blank")
            webChromeClient = null
            webViewClient = WebViewClient()
            removeAllViews()
            destroy()
        }
        browser = null
        super.onDestroy()
    }

    companion object {
        private const val PREFS = "browser_settings"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
        private const val MAX_HISTORY_ITEMS = 100
        private const val DUCKDUCKGO_HOME = "https://duckduckgo.com/"
        private const val DUCKDUCKGO_SEARCH = "https://duckduckgo.com/?q="
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    }
}
