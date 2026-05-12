package com.lightspeed.browser.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * History entry — records a page visit.
 *
 * Tracks visits rather than unique URLs (same URL can be visited multiple times).
 * Indexed for fast queries by timestamp and URL.
 *
 * Optimization:
 * - Composite index on (visited_at, url) for common history queries
 * - Timestamp as Long (primitive, no Date objects)
 * - URL stored as String (not URI object — avoids allocation)
 */
@Entity(
    tableName = "history",
    indices = [
        Index(value = ["visited_at"]),
        Index(value = ["url"]),
        Index(value = ["visited_at", "url"])
    ]
)
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "url")
    val url: String,

    /** Visit timestamp in milliseconds since epoch */
    @ColumnInfo(name = "visited_at")
    val visitedAt: Long = System.currentTimeMillis(),

    /** Number of times this URL has been visited (for frequency ranking) */
    @ColumnInfo(name = "visit_count")
    val visitCount: Int = 1
)
