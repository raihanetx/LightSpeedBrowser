package com.lightspeed.browser.core.di

import android.content.Context
import com.lightspeed.browser.browser.AdBlocker
import com.lightspeed.browser.data.db.BrowserDatabase
import com.lightspeed.browser.data.db.dao.BookmarkDao
import com.lightspeed.browser.data.db.dao.HistoryDao
import com.lightspeed.browser.data.prefs.BrowserPreferences

/**
 * Minimal dependency container.
 */
class ServiceLocator(private val appContext: Context) {

    val preferences: BrowserPreferences by lazy { BrowserPreferences(appContext) }

    val database: BrowserDatabase by lazy { BrowserDatabase.getInstance(appContext) }
    val bookmarkDao: BookmarkDao by lazy { database.bookmarkDao() }
    val historyDao: HistoryDao by lazy { database.historyDao() }

    val adBlocker: AdBlocker by lazy { AdBlocker() }
}
