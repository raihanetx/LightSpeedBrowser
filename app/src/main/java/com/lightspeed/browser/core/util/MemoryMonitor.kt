package com.lightspeed.browser.core.util

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.util.Log

/**
 * Monitors system memory pressure and dispatches events to registered listeners.
 *
 * Uses Android's ComponentCallbacks2 API which provides granular trim levels:
 * - TRIM_MEMORY_COMPLETE: App is in the background and likely to be killed
 * - TRIM_MEMORY_MODERATE: System is running low on memory
 * - TRIM_MEMORY_UI_HIDDEN: App UI is no longer visible
 * - TRIM_MEMORY_RUNNING_CRITICAL: App is running but system is critically low
 *
 * Optimization: Single callback registered at application level to avoid
 * each component registering separately.
 */
class MemoryMonitor(private val context: Context) {

    private val listeners = mutableListOf<MemoryListener>()
    private val callback = createTrimCallback()

    /**
     * Start monitoring memory pressure.
     * Call from Application.onCreate().
     */
    fun start() {
        context.registerComponentCallbacks(callback)
        Log.d(TAG, "MemoryMonitor started")
    }

    /**
     * Stop monitoring. Call when no longer needed.
     */
    fun stop() {
        context.unregisterComponentCallbacks(callback)
        listeners.clear()
        Log.d(TAG, "MemoryMonitor stopped")
    }

    /**
     * Register a listener for memory pressure events.
     */
    fun addListener(listener: MemoryListener) {
        listeners.add(listener)
    }

    /**
     * Remove a previously registered listener.
     */
    fun removeListener(listener: MemoryListener) {
        listeners.remove(listener)
    }

    private fun createTrimCallback(): ComponentCallbacks2 {
        return object : ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) {
                Log.d(TAG, "onTrimMemory: level=$level (${trimLevelToString(level)})")
                for (listener in listeners.toList()) {
                    listener.onTrim(level)
                }
            }

            override fun onConfigurationChanged(config: Configuration) {
                // Not needed
            }

            override fun onLowMemory() {
                Log.w(TAG, "onLowMemory: system-wide memory low")
                for (listener in listeners.toList()) {
                    listener.onLowMemory()
                }
            }
        }
    }

    interface MemoryListener {
        /** Called when system requests memory trim */
        fun onTrim(level: Int) = Unit
        /** Called when system-wide memory is low */
        fun onLowMemory() = Unit
    }

    companion object {
        private const val TAG = "MemoryMonitor"

        fun trimLevelToString(level: Int): String = when (level) {
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "COMPLETE"
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
            else -> "UNKNOWN($level)"
        }
    }
}
