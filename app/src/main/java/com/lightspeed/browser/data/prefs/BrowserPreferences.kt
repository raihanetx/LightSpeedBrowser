package com.lightspeed.browser.data.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Browser preferences — minimal settings storage.
 */
class BrowserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("lightspeed_prefs", Context.MODE_PRIVATE)

    // ── Ad Blocking ──
    var isAdBlockEnabled: Boolean
        get() = prefs.getBoolean(KEY_AD_BLOCK, true)
        set(v) = prefs.edit().putBoolean(KEY_AD_BLOCK, v).apply()

    // ── Text Mode ──
    var isTextModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_TEXT_MODE, false)
        set(v) = prefs.edit().putBoolean(KEY_TEXT_MODE, v).apply()

    // ── Cookies: 0=all, 1=block 3rd-party, 2=block all ──
    var cookiePolicy: Int
        get() = prefs.getInt(KEY_COOKIE_POLICY, 0)
        set(v) = prefs.edit().putInt(KEY_COOKIE_POLICY, v).apply()

    companion object {
        private const val KEY_AD_BLOCK = "ad_block"
        private const val KEY_TEXT_MODE = "text_mode"
        private const val KEY_COOKIE_POLICY = "cookie_policy"

        const val COOKIE_ALL = 0
        const val COOKIE_BLOCK_THIRD_PARTY = 1
        const val COOKIE_BLOCK_ALL = 2
    }
}
