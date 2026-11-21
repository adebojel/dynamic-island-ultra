package com.adebojel.dynamicislandultra.browser

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import java.io.ByteArrayInputStream
import java.net.URLEncoder

/**
 * A deliberately narrow in-app browser. Main-frame navigation is restricted
 * to YouTube; only YouTube playback and Google static/API resource domains are
 * allowed for subresources. It is not a general-purpose browser.
 */
class YoutubeBrowserActivity : ComponentActivity() {
    private var browser: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 11, 20))
            setPadding(12, 12, 12, 8)
        }
        val title = TextView(this).apply {
            text = "YOUTUBE BROWSER"
            setTextColor(Color.rgb(67, 232, 255))
            textSize = 15f
            gravity = Gravity.CENTER_VERTICAL
            setPadding(4, 4, 4, 10)
        }
        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val query = EditText(this).apply {
            hint = "Cari video atau tempel tautan YouTube"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            singleLine = true
            setBackgroundColor(Color.rgb(24, 36, 58))
            setPadding(12, 4, 12, 4)
        }
        val search = Button(this).apply { text = "Cari" }
        searchRow.addView(query, LinearLayout.LayoutParams(0, 52, 1f))
        searchRow.addView(search, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 52
        ))

        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun navButton(label: String, action: () -> Unit) {
            navRow.addView(Button(this).apply {
                text = label
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(0, 48, 1f))
        }

        val web = WebView(this)
        browser = web
        web.setBackgroundColor(Color.rgb(8, 11, 20))
        web.settings.apply {
            javaScriptEnabled = true // Required by YouTube's own player.
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false
            if (Build.VERSION.SDK_INT >= 26) safeBrowsingEnabled = true
        }
        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val uri = request.url
                if (isAllowedTopLevel(uri)) return false
                Toast.makeText(
                    this@YoutubeBrowserActivity,
                    "Browser ini dibatasi untuk YouTube.",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }

            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val uri = request.url
                if (uri.scheme == "data" || uri.scheme == "blob" || uri.scheme == "about") {
                    return null
                }
                val allowed = if (request.isForMainFrame) {
                    isAllowedTopLevel(uri)
                } else {
                    isAllowedResource(uri)
                }
                return if (allowed) null else blockedResponse()
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.cancel()
            }
        }

        navButton("Kembali") { if (web.canGoBack()) web.goBack() }
        navButton("Maju") { if (web.canGoForward()) web.goForward() }
        navButton("Muat ulang") { web.reload() }
        navButton("Beranda") { web.loadUrl(YOUTUBE_HOME) }

        search.setOnClickListener {
            val value = query.text.toString().trim()
            if (value.isEmpty()) {
                web.loadUrl(YOUTUBE_HOME)
            } else {
                val direct = Uri.parse(value)
                val destination = if (isAllowedTopLevel(direct)) {
                    direct.toString()
                } else {
                    "https://www.youtube.com/results?search_query=" +
                        URLEncoder.encode(value, Charsets.UTF_8.name())
                }
                web.loadUrl(destination)
            }
        }

        root.addView(title)
        root.addView(searchRow)
        root.addView(navRow)
        root.addView(web, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
        web.loadUrl(YOUTUBE_HOME)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (web.canGoBack()) web.goBack() else finish()
            }
        })
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

    private fun isAllowedTopLevel(uri: Uri): Boolean =
        uri.scheme == "https" && isYouTubeHost(uri.host.orEmpty())

    private fun isYouTubeHost(hostValue: String): Boolean {
        val host = hostValue.lowercase()
        return host == "youtube.com" || host.endsWith(".youtube.com") ||
            host == "youtube-nocookie.com" || host.endsWith(".youtube-nocookie.com")
    }

    private fun isAllowedResource(uri: Uri): Boolean {
        if (uri.scheme != "https") return false
        val host = uri.host?.lowercase() ?: return false
        val suffixes = listOf(
            "youtube.com", "youtube-nocookie.com", "ytimg.com",
            "googlevideo.com", "googleapis.com", "gstatic.com",
            "googleusercontent.com", "google.com", "doubleclick.net",
            "googlesyndication.com", "googleadservices.com"
        )
        return suffixes.any { host == it || host.endsWith(".$it") }
    }

    private fun blockedResponse() = WebResourceResponse(
        "text/plain",
        "UTF-8",
        403,
        "Blocked by YouTube-only browser policy",
        mapOf("Cache-Control" to "no-store"),
        ByteArrayInputStream(ByteArray(0))
    )

    companion object {
        private const val YOUTUBE_HOME = "https://www.youtube.com/"
    }
}
