package com.lightspeed.browser.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bookmark entity — represents a user-saved bookmark.
 *
 * Indexed by URL for fast duplicate checking and sorted by creation date.
 * Uses primitive types where possible to minimize object overhead.
 *
 * Optimization:
 * - Indices on URL and creation_date for fast lookups
 * - Folder support via nullable folderName
 * - All fields non-null except folderName
 */
@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["url"], unique = true),
        Index(value = ["created_at"])
    ]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "url")
    val url: String,

    /** Optional folder for organization. Null means uncategorized. */
    @ColumnInfo(name = "folder_name")
    val folderName: String? = null,

    /** Creation timestamp in milliseconds since epoch */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /** Position for ordering within folder */
    @ColumnInfo(name = "position")
    val position: Int = 0
)
