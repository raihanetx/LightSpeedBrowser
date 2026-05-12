package com.lightspeed.browser.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lightspeed.browser.data.db.entities.HistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for browsing history.
 *
 * Each page visit creates a new row. No dedup by URL (this is simpler
 * and preserves chronological order).
 */
@Dao
interface HistoryDao {

    /** Get all history entries ordered by visit time (newest first) */
    @Query("SELECT * FROM history ORDER BY visited_at DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    /** Get recent history with a limit */
    @Query("SELECT * FROM history ORDER BY visited_at DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<HistoryEntry>>

    /** Search history by title or URL */
    @Query("""
        SELECT * FROM history 
        WHERE title LIKE '%' || :query || '%' 
           OR url LIKE '%' || :query || '%'
        ORDER BY visited_at DESC 
        LIMIT :limit
    """)
    fun search(query: String, limit: Int = 50): Flow<List<HistoryEntry>>

    /** Insert a visit */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntry): Long

    /** Delete a single history entry */
    @Delete
    suspend fun delete(entry: HistoryEntry)

    /** Delete history entries older than the given timestamp */
    @Query("DELETE FROM history WHERE visited_at < :beforeTime")
    suspend fun deleteOlderThan(beforeTime: Long): Int

    /** Clear all history */
    @Query("DELETE FROM history")
    suspend fun deleteAll()

    /** Get total history count */
    @Query("SELECT COUNT(*) FROM history")
    suspend fun count(): Int
}
