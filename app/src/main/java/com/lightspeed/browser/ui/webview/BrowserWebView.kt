package com.lightspeed.browser.ui.webview

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Simple memory-optimized WebView for low-end devices.
 */
class BrowserWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private var destroyed = false

    init {
        settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setRenderPriority(WebSettings.RenderPriority.LOW)
            }
            cacheMode = WebSettings.LOAD_DEFAULT
            loadsImagesAutomatically = true
            allowFileAccess = false
            allowContentAccess = false
            databaseEnabled = false
            domStorageEnabled = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            saveFormData = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
    }

    fun destroySafely() {
        if (destroyed) return
        destroyed = true
        try {
            (parent as? ViewGroup)?.removeView(this)
            stopLoading()
            removeAllViews()
            clearHistory(); clearCache(true); clearFormData()
            onPause(); pauseTimers()
            destroy()
        } catch (_: Exception) {}
    }
}
