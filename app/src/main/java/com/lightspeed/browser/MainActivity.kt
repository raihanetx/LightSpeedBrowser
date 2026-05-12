package com.lightspeed.browser

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lightspeed.browser.data.db.entities.Bookmark
import com.lightspeed.browser.data.db.entities.HistoryEntry
import com.lightspeed.browser.data.prefs.BrowserPreferences
import com.lightspeed.browser.databinding.ActivityMainBinding
import com.lightspeed.browser.ui.bookmarks.BookmarksActivity
import com.lightspeed.browser.ui.history.HistoryActivity
import com.lightspeed.browser.ui.webview.BrowserWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: BrowserWebView
    private val services get() = (application as BrowserApplication).serviceLocator

    private var currentUrl: String = ""
    private var currentTitle: String = ""

    // Activity result launchers (replaces deprecated startActivityForResult)
    private val bookmarksLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("url")?.let { loadUrl(it) }
        }
    }
    private val historyLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("url")?.let { loadUrl(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_LightSpeed)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupToolbar()
        setupBottomBar()
        syncAdBlockerState()

        loadUrl("about:blank")
    }

    // ── WebView ────────────────────────────────────────────

    private fun setupWebView() {
        webView = BrowserWebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = createWebViewClient()
            webChromeClient = createChromeClient()
            setDownloadListener { url, _, contentDisposition, _, _ -> handleDownload(url, contentDisposition) }
            applyCookiePolicy()
        }
        binding.webViewContainer.addView(webView)

        // Enable onBackPressedCallback for API 33+
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                androidx.activity.OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        }
    }

    private fun createWebViewClient(): WebViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if (url != null) {
                currentUrl = url
                binding.toolbar.setUrl(url)
                binding.progressBar.visibility = android.view.View.VISIBLE
            }
        }
        override fun onPageFinished(view: WebView?, url: String?) {
            binding.progressBar.visibility = android.view.View.GONE
            if (url != null && url == view?.url) { // Only main frame
                currentUrl = url
                binding.toolbar.setUrl(url)
                // Save history
                lifecycleScope.launch(Dispatchers.IO) {
                    services.historyDao.insert(HistoryEntry(title = currentTitle, url = url))
                }
                if (services.preferences.isTextModeEnabled) applyTextMode(view)
            }
            updateNavButtons()
        }
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
            val url = request.url?.toString() ?: return null
            if (services.adBlocker.isEnabled && services.adBlocker.shouldBlock(url)) {
                return WebResourceResponse("text/plain", "utf-8", 204, "No Content", emptyMap(), java.io.ByteArrayInputStream(ByteArray(0)))
            }
            return null
        }
    }

    private fun createChromeClient(): WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, progress: Int) {
            binding.progressBar.progress = progress
        }
        override fun onReceivedTitle(view: WebView?, title: String?) {
            if (title != null) { currentTitle = title; binding.toolbar.setTitle(title) }
        }
    }

    // ── Toolbar ────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.apply {
            setOnUrlEnterListener { url -> loadUrl(url) }
            setOnBackClickListener { if (webView.canGoBack()) webView.goBack() }
            setOnForwardClickListener { if (webView.canGoForward()) webView.goForward() }
            setOnRefreshClickListener { webView.reload() }
        }
    }

    // ── Bottom Bar ─────────────────────────────────────────

    private fun setupBottomBar() {
        binding.bottomBar.apply {
            setOnBackClickListener { if (webView.canGoBack()) webView.goBack() }
            setOnForwardClickListener { if (webView.canGoForward()) webView.goForward() }
            setOnMenuClickListener { showMenu() }
            setOnZoomInClickListener { webView.zoomIn() }
            setOnZoomOutClickListener { webView.zoomOut() }
        }
    }

    private fun updateNavButtons() {
        binding.toolbar.setBackEnabled(webView.canGoBack())
        binding.toolbar.setForwardEnabled(webView.canGoForward())
        binding.bottomBar.setBackEnabled(webView.canGoBack())
        binding.bottomBar.setForwardEnabled(webView.canGoForward())
    }

    // ── Navigation ─────────────────────────────────────────

    private fun loadUrl(input: String) {
        val url = formatUrl(input.trim())
        currentUrl = url
        webView.loadUrl(url)
    }

    private fun formatUrl(input: String): String {
        if (input.isEmpty() || input == "about:blank") return "about:blank"
        if (input.startsWith("http://") || input.startsWith("https://")) return input
        if (input.contains(".") && !input.contains(" ")) return "https://$input"
        val encoded = java.net.URLEncoder.encode(input, "UTF-8")
        return "https://www.google.com/search?q=$encoded"
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        webView.destroySafely()
        super.onDestroy()
    }

    // ── Menu ───────────────────────────────────────────────

    private fun showMenu() {
        val popup = PopupMenu(this, binding.bottomBar.menuButton)
        popup.menu.add(0, 1, 0, "Bookmarks")
        popup.menu.add(0, 2, 0, "History")
        popup.menu.add(0, 3, 0, "Downloads")
        popup.menu.add(0, 0, 0, "Save bookmark")
        popup.menu.add(0, 4, 0, if (services.adBlocker.isEnabled) "Ad block: ON" else "Ad block: OFF")
        popup.menu.add(0, 5, 0, if (services.preferences.isTextModeEnabled) "Text mode: ON" else "Text mode: OFF")
        popup.menu.add(0, 6, 0, cookiesLabel())
        popup.menu.add(0, 7, 0, "Share page")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> saveBookmark()
                1 -> bookmarksLauncher.launch(Intent(this, BookmarksActivity::class.java))
                2 -> historyLauncher.launch(Intent(this, HistoryActivity::class.java))
                3 -> Toast.makeText(this, "Check Downloads folder", Toast.LENGTH_SHORT).show()
                4 -> toggleAdBlock()
                5 -> toggleTextMode()
                6 -> cycleCookiePolicy()
                7 -> sharePage()
            }; true
        }
        popup.show()
    }

    private fun syncAdBlockerState() {
        services.adBlocker.isEnabled = services.preferences.isAdBlockEnabled
    }

    private fun toggleAdBlock() {
        val newState = !services.adBlocker.isEnabled
        services.adBlocker.isEnabled = newState
        services.preferences.isAdBlockEnabled = newState
        Toast.makeText(this, if (newState) "Ad blocking ON" else "Ad blocking OFF", Toast.LENGTH_SHORT).show()
    }

    // ── Bookmarks ──────────────────────────────────────────

    private fun saveBookmark() {
        if (currentUrl.isEmpty() || currentUrl == "about:blank") {
            Toast.makeText(this, "No page to save", Toast.LENGTH_SHORT).show(); return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val exists = services.bookmarkDao.bookmarkCount(currentUrl)
            if (exists > 0) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Already bookmarked", Toast.LENGTH_SHORT).show() }
            } else {
                services.bookmarkDao.insert(Bookmark(title = currentTitle.ifEmpty { currentUrl }, url = currentUrl))
                runOnUiThread { Toast.makeText(this@MainActivity, "Bookmark saved", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    // ── Text Mode ──────────────────────────────────────────

    private fun toggleTextMode() {
        val on = !services.preferences.isTextModeEnabled
        services.preferences.isTextModeEnabled = on
        Toast.makeText(this, if (on) "Text mode ON" else "Text mode OFF", Toast.LENGTH_SHORT).show()
        if (on) applyTextMode(webView) else webView.reload()
    }

    private fun applyTextMode(view: WebView?) {
        view?.evaluateJavascript(
            """(function(){
                var s=document.createElement('style');
                s.textContent='*{background:#fff!important;color:#222!important;'+
                'font-family:serif!important;font-size:18px!important;line-height:1.6!important}'+
                'img,video,iframe,canvas,svg{display:none!important}'+
                'a{color:#1a0dab!important}';
                document.head.appendChild(s);
            })()""", null
        )
    }

    // ── Cookie Policy ──────────────────────────────────────

    private fun cookiesLabel(): String = when (services.preferences.cookiePolicy) {
        BrowserPreferences.COOKIE_ALL -> "Cookies: All"
        BrowserPreferences.COOKIE_BLOCK_THIRD_PARTY -> "Cookies: Block 3rd"
        BrowserPreferences.COOKIE_BLOCK_ALL -> "Cookies: Block all"
        else -> "Cookies: All"
    }

    private fun cycleCookiePolicy() {
        val next = when (services.preferences.cookiePolicy) {
            BrowserPreferences.COOKIE_ALL -> BrowserPreferences.COOKIE_BLOCK_THIRD_PARTY
            BrowserPreferences.COOKIE_BLOCK_THIRD_PARTY -> BrowserPreferences.COOKIE_BLOCK_ALL
            else -> BrowserPreferences.COOKIE_ALL
        }
        services.preferences.cookiePolicy = next
        applyCookiePolicy()
        Toast.makeText(this, cookiesLabel(), Toast.LENGTH_SHORT).show()
    }

    private fun applyCookiePolicy() {
        val cm = CookieManager.getInstance()
        cm.setAcceptCookie(services.preferences.cookiePolicy != BrowserPreferences.COOKIE_BLOCK_ALL)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView,
            services.preferences.cookiePolicy == BrowserPreferences.COOKIE_ALL)
    }

    // ── Downloads ──────────────────────────────────────────

    private fun handleDownload(url: String, contentDisposition: String?) {
        // Extract filename from Content-Disposition header, or URL, or fallback
        var filename = extractFilename(contentDisposition, url)
        if (filename.isBlank()) filename = "download_${System.currentTimeMillis()}"

        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription("Downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
            getSystemService(Context.DOWNLOAD_SERVICE)?.let {
                (it as DownloadManager).enqueue(request)
                Toast.makeText(this, "Download started: $filename", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractFilename(contentDisposition: String?, url: String): String {
        // Try Content-Disposition header first
        contentDisposition?.let { cd ->
            val match = "filename\\*?=(?:UTF-8'')?([^;\\s]+)".toRegex(RegexOption.IGNORE_CASE)
                .find(cd)
            if (match != null) {
                return Uri.decode(match.groupValues[1]).substringBefore("?")
            }
        }
        // Try URL path
        val path = Uri.parse(url).lastPathSegment
        if (!path.isNullOrBlank() && !path.contains("?")) return path
        // Fallback
        return ""
    }

    // ── Share ──────────────────────────────────────────────

    private fun sharePage() {
        val text = if (currentTitle.isNotEmpty()) "$currentTitle - $currentUrl" else currentUrl
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share link"))
    }

    companion object {
        private const val REQUEST_BOOKMARKS = 1001
        private const val REQUEST_HISTORY = 1002
    }
}
