package com.adebojel.dynamicislandultra.browser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.http.SslError
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject

internal object BrowserUrlPolicy {
    private const val HOME = "https://duckduckgo.com/"
    private const val SEARCH = "https://duckduckgo.com/?q="
    private val schemePattern = Regex("^([A-Za-z][A-Za-z0-9+.-]*):")

    fun resolve(rawInput: String): String? {
        val input = rawInput.trim()
        if (input.isEmpty()) return HOME
        val scheme = schemePattern.find(input)?.groupValues?.getOrNull(1)?.lowercase()
        if (scheme == "https") return input
        if (scheme == "http") return input.replaceFirst(Regex("^http:", RegexOption.IGNORE_CASE), "https:")
        val looksLikeHost = scheme == null && input.contains('.') && !input.contains(' ')
        return if (looksLikeHost) "https://$input"
        else SEARCH + URLEncoder.encode(input, Charsets.UTF_8.name())
    }

    fun isAllowedWebScheme(url: String?): Boolean {
        val scheme = schemePattern.find(url.orEmpty())?.groupValues?.getOrNull(1)?.lowercase()
        return scheme == "https" || scheme == "http"
    }
}

private data class BrowserTab(val id: Int, val webView: WebView, var title: String = "Tab baru")

/**
 * DuckDuckGo browser with bounded multi-tab support and whole-session private mode.
 */
class DuckDuckGoBrowserActivity : ComponentActivity() {
    private val tabs = mutableListOf<BrowserTab>()
    private var nextTabId = 1
    private var activeTabId: Int? = null
    private var browser: WebView? = null
    private lateinit var heading: TextView
    private lateinit var address: EditText
    private lateinit var modeButton: Button
    private lateinit var privateButton: Button
    private lateinit var progress: ProgressBar
    private lateinit var webHost: FrameLayout
    private var desktopMode = false
    private var mobileUserAgent = ""
    private var privateMode = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        desktopMode = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_DESKTOP_MODE, false)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 11, 20))
            setPadding(10, 10, 10, 8)
        }
        heading = TextView(this).apply {
            text = "DUCKDUCKGO • HTTPS BROWSER"
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
        addressRow.addView(address, LinearLayout.LayoutParams(0, 52, 1f))
        addressRow.addView(Button(this).apply {
            text = "Go"
            setOnClickListener { navigateFromAddress() }
        }, LinearLayout.LayoutParams(-2, 52))

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun control(label: String, weight: Float = 1f, action: () -> Unit): Button {
            val button = Button(this).apply {
                text = label
                textSize = 9f
                setPadding(1, 0, 1, 0)
                setOnClickListener { action() }
            }
            controls.addView(button, LinearLayout.LayoutParams(0, 42, weight))
            return button
        }

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            visibility = View.GONE
        }
        webHost = FrameLayout(this).apply { setBackgroundColor(Color.WHITE) }
        control("Back") { browser?.let { if (it.canGoBack()) it.goBack() } }
        control("Next") { browser?.let { if (it.canGoForward()) it.goForward() } }
        control("Reload") { browser?.reload() }
        control("Home") { browser?.loadUrl(DUCKDUCKGO_HOME) }
        modeButton = control("Mobile", 1.2f) { toggleDesktopMode() }
        modeButton.text = if (desktopMode) "Desktop ✓" else "Mobile"

        val library = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun libraryButton(label: String, action: () -> Unit) {
            library.addView(Button(this).apply {
                text = label
                textSize = 9f
                setPadding(1, 0, 1, 0)
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(0, 40, 1f))
        }
        libraryButton("Bookmark") { saveCurrentBookmark() }
        libraryButton("Bookmarks") { showSavedPages(KEY_BOOKMARKS, "Bookmarks") }
        libraryButton("History") { showSavedPages(KEY_HISTORY, "Riwayat", true) }
        libraryButton("Downloads") { openDownloads() }

        val tabControls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun tabButton(label: String, action: () -> Unit) {
            tabControls.addView(Button(this).apply {
                text = label
                textSize = 9f
                setPadding(1, 0, 1, 0)
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(0, 40, 1f))
        }
        tabButton("+ Tab") { createTab(true) }
        tabButton("Tabs") { showTabs() }
        tabButton("Close Tab") { closeActiveTab() }
        privateButton = Button(this).apply {
            textSize = 9f
            setPadding(1, 0, 1, 0)
            setOnClickListener { togglePrivateMode() }
        }
        tabControls.addView(privateButton, LinearLayout.LayoutParams(0, 40, 1.1f))
        updatePrivateLabel()

        root.addView(heading)
        root.addView(addressRow)
        root.addView(controls)
        root.addView(library)
        root.addView(tabControls)
        root.addView(progress, LinearLayout.LayoutParams(-1, 3))
        root.addView(webHost, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        createTab(true)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val current = browser
                if (current?.canGoBack() == true) current.goBack()
                else if (tabs.size > 1) closeActiveTab()
                else finish()
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createTab(loadHome: Boolean) {
        if (tabs.size >= MAX_TABS) {
            Toast.makeText(this, "Maksimal $MAX_TABS tab aktif.", Toast.LENGTH_SHORT).show()
            return
        }
        val view = WebView(this)
        if (mobileUserAgent.isBlank()) mobileUserAgent = view.settings.userAgentString
        view.setBackgroundColor(Color.WHITE)
        view.isSaveEnabled = !privateMode
        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = !privateMode
            databaseEnabled = !privateMode
            mediaPlaybackRequiresUserGesture = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            cacheMode = if (privateMode) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            if (android.os.Build.VERSION.SDK_INT >= 26) safeBrowsingEnabled = true
            userAgentString = if (desktopMode) DESKTOP_USER_AGENT else mobileUserAgent
            useWideViewPort = true
            loadWithOverviewMode = desktopMode
        }
        CookieManager.getInstance().setAcceptCookie(!privateMode)
        if (privateMode) view.clearCache(true)

        val tab = BrowserTab(nextTabId++, view)
        view.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(webView: WebView, newProgress: Int) {
                if (activeTabId == tab.id) {
                    progress.progress = newProgress
                    progress.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
                }
            }
            override fun onReceivedTitle(webView: WebView, pageTitle: String?) {
                if (!pageTitle.isNullOrBlank()) {
                    tab.title = pageTitle.take(100)
                    if (activeTabId == tab.id) updateActiveTabUi(tab)
                }
            }
        }
        view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(webView: WebView, request: WebResourceRequest): Boolean {
                val scheme = request.url.scheme?.lowercase()
                if (scheme == "https") return false
                if (scheme == "http") {
                    webView.loadUrl(request.url.buildUpon().scheme("https").build().toString())
                    return true
                }
                Toast.makeText(this@DuckDuckGoBrowserActivity,
                    "Browser hanya membuka alamat web HTTP/HTTPS.", Toast.LENGTH_SHORT).show()
                return true
            }
            override fun onPageStarted(webView: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                if (activeTabId == tab.id) {
                    address.setText(url.orEmpty())
                    heading.text = (if (privateMode) "PRIVATE • " else "") + tab.title.take(60)
                    progress.visibility = View.VISIBLE
                    progress.progress = 0
                }
            }
            override fun onPageFinished(webView: WebView, url: String?) {
                if (!url.isNullOrBlank() && activeTabId == tab.id) {
                    address.setText(url)
                    if (!privateMode) recordHistory(url, tab.title)
                    updateActiveTabUi(tab)
                }
            }
            override fun onReceivedSslError(webView: WebView, handler: SslErrorHandler, error: SslError) {
                handler.cancel()
            }
        }
        view.setDownloadListener { url, userAgent, disposition, mime, _ ->
            if (privateMode) Toast.makeText(this, "Unduhan dinonaktifkan dalam mode privat.", Toast.LENGTH_SHORT).show()
            else enqueueDownload(url, userAgent, disposition, mime)
        }
        tabs.add(tab)
        switchTab(tab.id)
        if (loadHome) view.loadUrl(DUCKDUCKGO_HOME)
    }

    private fun switchTab(id: Int) {
        val tab = tabs.firstOrNull { it.id == id } ?: return
        (browser?.parent as? android.view.ViewGroup)?.removeView(browser)
        webHost.removeAllViews()
        webHost.addView(tab.webView, FrameLayout.LayoutParams(-1, -1))
        browser = tab.webView
        activeTabId = tab.id
        updateActiveTabUi(tab)
        progress.visibility = View.GONE
    }

    private fun updateActiveTabUi(tab: BrowserTab) {
        heading.text = (if (privateMode) "PRIVATE • " else "") + tab.title.take(60)
        address.setText(tab.webView.url.orEmpty())
        modeButton.text = if (desktopMode) "Desktop ✓" else "Mobile"
        updatePrivateLabel()
    }

    private fun showTabs() {
        val labels = tabs.mapIndexed { index, tab ->
            val marker = if (tab.id == activeTabId) "● " else ""
            "$marker${index + 1}. ${tab.title}\n${tab.webView.url.orEmpty().take(72)}"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(if (privateMode) "Tab privat (${tabs.size}/$MAX_TABS)" else "Tab (${tabs.size}/$MAX_TABS)")
            .setItems(labels) { _, which -> tabs.getOrNull(which)?.let { switchTab(it.id) } }
            .setPositiveButton("Tab baru") { _, _ -> createTab(true) }
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun closeActiveTab() {
        val id = activeTabId ?: return
        val closingIndex = tabs.indexOfFirst { it.id == id }
        val closing = tabs.firstOrNull { it.id == id } ?: return
        tabs.remove(closing)
        destroyWebView(closing.webView)
        if (tabs.isEmpty()) createTab(true)
        else {
            val next = tabs.getOrNull(closingIndex.coerceAtMost(tabs.lastIndex)) ?: tabs.last()
            switchTab(next.id)
        }
    }

    private fun togglePrivateMode() {
        val nextMode = !privateMode
        CookieManager.getInstance().removeAllCookies {
            WebStorage.getInstance().deleteAllData()
            CookieManager.getInstance().flush()
            tabs.toList().forEach { destroyWebView(it.webView) }
            tabs.clear()
            browser = null
            activeTabId = null
            privateMode = nextMode
            CookieManager.getInstance().setAcceptCookie(!privateMode)
            updatePrivateLabel()
            createTab(true)
            Toast.makeText(this,
                if (privateMode) "Mode privat aktif: cookie, DOM storage dan history dinonaktifkan."
                else "Mode standar aktif. Penyimpanan web dimulai dari sesi bersih.",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun updatePrivateLabel() {
        privateButton.text = if (privateMode) "Private ✓" else "Private OFF"
    }

    private fun navigateFromAddress() {
        val target = BrowserUrlPolicy.resolve(address.text.toString())
        if (target == null) {
            Toast.makeText(this, "Alamat tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }
        browser?.loadUrl(target)
        address.clearFocus()
    }

    private fun enqueueDownload(url: String, userAgent: String, disposition: String?, mime: String?) {
        if (!url.startsWith("https://", true)) {
            Toast.makeText(this, "Unduhan hanya diizinkan dari HTTPS.", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            val filename = android.webkit.URLUtil.guessFileName(url, disposition, mime)
                .take(120).ifBlank { "download" }
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mime)
                addRequestHeader("User-Agent", userAgent)
                CookieManager.getInstance().getCookie(url)?.takeIf { it.isNotBlank() }?.let {
                    addRequestHeader("Cookie", it)
                }
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(false)
                setDestinationInExternalFilesDir(this@DuckDuckGoBrowserActivity,
                    Environment.DIRECTORY_DOWNLOADS, filename)
            }
            (getSystemService(DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            Toast.makeText(this, "Unduhan dimulai: $filename", Toast.LENGTH_LONG).show()
        }.onFailure {
            Toast.makeText(this, "Unduhan gagal dimulai: ${it.message ?: "kesalahan sistem"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openDownloads() {
        if (privateMode) {
            Toast.makeText(this, "Unduhan dinonaktifkan dalam mode privat.", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching { startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) }
            .onFailure { Toast.makeText(this, "Daftar unduhan Android tidak tersedia.", Toast.LENGTH_SHORT).show() }
    }

    private fun saveCurrentBookmark() {
        if (privateMode) {
            Toast.makeText(this, "Bookmark dinonaktifkan dalam mode privat.", Toast.LENGTH_SHORT).show()
            return
        }
        val url = browser?.url.orEmpty()
        if (!url.startsWith("https://", true)) {
            Toast.makeText(this, "Buka halaman HTTPS terlebih dahulu.", Toast.LENGTH_SHORT).show()
            return
        }
        val entries = readEntries(KEY_BOOKMARKS)
        val existing = (0 until entries.length()).firstOrNull { entries.optJSONObject(it)?.optString("url") == url }
        if (existing != null) entries.remove(existing)
        entries.put(JSONObject().apply {
            put("url", url)
            put("title", activeTab()?.title ?: url)
            put("timestamp", System.currentTimeMillis())
        })
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_BOOKMARKS, entries.toString()).apply()
        Toast.makeText(this, "Bookmark disimpan di perangkat.", Toast.LENGTH_SHORT).show()
    }

    private fun recordHistory(url: String, title: String) {
        if (privateMode || !url.startsWith("https://", true)) return
        val entries = readEntries(KEY_HISTORY)
        val duplicate = (0 until entries.length()).firstOrNull { entries.optJSONObject(it)?.optString("url") == url }
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
        if (privateMode) {
            Toast.makeText(this, "Riwayat dan bookmark tidak tersedia dalam mode privat.", Toast.LENGTH_SHORT).show()
            return
        }
        if (key == KEY_BOOKMARKS) {
            showBookmarkManager()
            return
        }
        val entries = readEntries(key)
        if (entries.length() == 0) {
            Toast.makeText(this, "Belum ada data $title.", Toast.LENGTH_SHORT).show()
            return
        }
        val labels = Array(entries.length()) { index ->
            val item = entries.optJSONObject(index) ?: JSONObject()
            "${item.optString("title", "Halaman")}\n${item.optString("url")}"
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

    private fun showBookmarkManager(query: String = "") {
        val entries = readEntries(KEY_BOOKMARKS)
        val filtered = (0 until entries.length()).mapNotNull { index ->
            entries.optJSONObject(index)?.let { index to it }
        }.filter { (_, item) ->
            val haystack = item.optString("title") + " " + item.optString("url")
            haystack.contains(query.trim(), ignoreCase = true)
        }
        if (filtered.isEmpty()) {
            Toast.makeText(this, if (query.isBlank()) "Belum ada bookmark." else "Bookmark tidak ditemukan.", Toast.LENGTH_SHORT).show()
            if (query.isNotBlank()) showBookmarkSearch()
            return
        }
        val labels = filtered.map { (_, item) ->
            "${item.optString("title", "Halaman")}\n${item.optString("url")}"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Bookmark (${filtered.size})")
            .setItems(labels) { _, which ->
                val originalIndex = filtered[which].first
                val item = readEntries(KEY_BOOKMARKS).optJSONObject(originalIndex) ?: return@setItems
                val url = item.optString("url")
                AlertDialog.Builder(this)
                    .setTitle(item.optString("title", "Bookmark"))
                    .setItems(arrayOf("Buka halaman", "Ubah nama", "Hapus bookmark")) { _, action ->
                        when (action) {
                            0 -> if (url.startsWith("https://", true)) browser?.loadUrl(url)
                            1 -> renameBookmark(originalIndex, item)
                            2 -> deleteBookmark(originalIndex)
                        }
                    }
                    .setNegativeButton("Tutup", null)
                    .show()
            }
            .setNeutralButton("Cari") { _, _ -> showBookmarkSearch() }
            .setPositiveButton("Selesai", null)
            .show()
    }

    private fun showBookmarkSearch() {
        val input = EditText(this).apply {
            hint = "Judul atau alamat HTTPS"
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("Cari bookmark")
            .setView(input)
            .setPositiveButton("Cari") { _, _ -> showBookmarkManager(input.text.toString()) }
            .setNeutralButton("Semua") { _, _ -> showBookmarkManager() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun renameBookmark(index: Int, item: JSONObject) {
        val input = EditText(this).apply {
            setText(item.optString("title"))
            setSelection(text.length)
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("Ubah nama bookmark")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val title = input.text.toString().trim().take(100)
                if (title.isBlank()) {
                    Toast.makeText(this, "Nama bookmark tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                } else {
                    val entries = readEntries(KEY_BOOKMARKS)
                    entries.optJSONObject(index)?.put("title", title)
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_BOOKMARKS, entries.toString()).apply()
                    Toast.makeText(this, "Nama bookmark diperbarui.", Toast.LENGTH_SHORT).show()
                    showBookmarkManager()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteBookmark(index: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus bookmark?")
            .setMessage("Bookmark ini akan dihapus dari penyimpanan perangkat.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                val entries = readEntries(KEY_BOOKMARKS)
                if (index in 0 until entries.length()) entries.remove(index)
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_BOOKMARKS, entries.toString()).apply()
                Toast.makeText(this, "Bookmark dihapus.", Toast.LENGTH_SHORT).show()
                showBookmarkManager()
            }
            .show()
    }

    private fun toggleDesktopMode() {
        desktopMode = !desktopMode
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_DESKTOP_MODE, desktopMode).apply()
        browser?.settings?.apply {
            userAgentString = if (desktopMode) DESKTOP_USER_AGENT else mobileUserAgent
            useWideViewPort = true
            loadWithOverviewMode = desktopMode
        }
        modeButton.text = if (desktopMode) "Desktop ✓" else "Mobile"
        browser?.reload()
    }

    private fun activeTab(): BrowserTab? = tabs.firstOrNull { it.id == activeTabId }

    private fun destroyWebView(view: WebView) {
        (view.parent as? android.view.ViewGroup)?.removeView(view)
        runCatching {
            view.stopLoading()
            view.loadUrl("about:blank")
            view.webChromeClient = null
            view.webViewClient = WebViewClient()
            view.removeAllViews()
            view.destroy()
        }
    }

    override fun onDestroy() {
        tabs.toList().forEach { destroyWebView(it.webView) }
        tabs.clear()
        browser = null
        super.onDestroy()
    }

    companion object {
        private const val PREFS = "browser_settings"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
        private const val MAX_HISTORY_ITEMS = 100
        private const val MAX_TABS = 8
        private const val DUCKDUCKGO_HOME = "https://duckduckgo.com/"
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    }
}
