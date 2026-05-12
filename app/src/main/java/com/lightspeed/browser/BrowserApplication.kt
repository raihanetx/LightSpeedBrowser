package com.lightspeed.browser

import android.app.Application
import com.lightspeed.browser.core.di.ServiceLocator
import com.lightspeed.browser.core.util.MemoryMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BrowserApplication : Application() {
    lateinit var serviceLocator: ServiceLocator; private set
    lateinit var memoryMonitor: MemoryMonitor; private set
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this
        serviceLocator = ServiceLocator(this)

        // Start memory monitoring to respond to system pressure
        memoryMonitor = MemoryMonitor(this)
        memoryMonitor.start()

        // Periodic history cleanup (runs once after app start, then every 24h)
        scheduleHistoryCleanup()
    }

    private fun scheduleHistoryCleanup() {
        appScope.launch {
            kotlinx.coroutines.delay(30_000L) // 30s after startup
            try {
                val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 // 30 days
                val deleted = serviceLocator.historyDao.deleteOlderThan(cutoff)
                android.util.Log.d("BrowserApp", "Cleaned up $deleted old history entries")
            } catch (e: Exception) {
                android.util.Log.e("BrowserApp", "History cleanup failed", e)
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // MemoryMonitor handles dispatching to registered listeners
    }

    companion object { lateinit var instance: BrowserApplication; private set }
}
