package com.lightspeed.browser.browser

import android.util.Log

/**
 * Simple hosts-based ad blocker.
 * Embedded domain list, binary search lookup. On/Off toggle.
 */
class AdBlocker {

    private var blockedHosts: Array<String> = emptyArray()
    @Volatile var isEnabled: Boolean = true

    init {
        blockedHosts = FALLBACK_DOMAINS.map { it.lowercase() }.sorted().toTypedArray()
        Log.d("AdBlocker", "Loaded ${blockedHosts.size} blocked domains")
    }

    fun shouldBlock(url: String): Boolean {
        if (!isEnabled || blockedHosts.isEmpty()) return false
        val host = extractHost(url) ?: return false
        var current = host
        while (current.isNotEmpty()) {
            if (blockedHosts.binarySearch(current) >= 0) return true
            val dot = current.indexOf('.')
            if (dot < 0 || dot == current.lastIndex) break
            current = current.substring(dot + 1)
        }
        return false
    }

    /**
     * Extract hostname from URL. Handles:
     * - http://host/path
     * - https://host:port/path
     * - user:pass@host (auth URLs)
     */
    private fun extractHost(url: String): String? {
        var s = url
        // Strip scheme
        val schemeIdx = s.indexOf("://")
        if (schemeIdx >= 0) s = s.substring(schemeIdx + 3)
        // Strip user:pass@
        val atIdx = s.indexOf('@')
        if (atIdx >= 0) s = s.substring(atIdx + 1)
        // Strip port and path
        val pathIdx = s.indexOf('/')
        val portIdx = s.indexOf(':')
        val end = when {
            pathIdx >= 0 && portIdx >= 0 -> minOf(pathIdx, portIdx)
            pathIdx >= 0 -> pathIdx
            portIdx >= 0 -> portIdx
            else -> s.length
        }
        return if (end > 0) s.substring(0, end).lowercase() else null
    }

    companion object {
        /** Minimal ad/tracker domain list — all hostname-only, no paths */
        val FALLBACK_DOMAINS = listOf(
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "google-analytics.com", "googletagmanager.com",
            "googlesyndication.com", "pagead2.googlesyndication.com",
            "connect.facebook.net", "facebook.com", "analytics.facebook.com",
            "amazon-adsystem.com", "aax.amazon-adsystem.com",
            "adnxs.com", "adzerk.net", "appnexus.com", "criteo.com",
            "taboola.com", "outbrain.com",
            "scorecardresearch.com", "quantserve.com",
            "krxd.net", "exelator.com", "bluekai.com",
            "adsafeprotected.com", "moatads.com", "moat.com",
            "pubmatic.com", "rubiconproject.com", "openx.net",
            "casalemedia.com", "contextweb.com",
            "bat.bing.com", "ads.yahoo.com",
            "popads.net", "propellerads.com",
            "coinhive.com", "coin-hive.com"
        )
    }
}
