package com.lightspeed.browser.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lightspeed.browser.data.db.entities.Bookmark
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for bookmarks.
 *
 * All queries return Flow for reactive UI updates.
 * Write operations are suspend functions for coroutine integration.
 */
@Dao
interface BookmarkDao {

    /** Get all bookmarks ordered by creation date (newest first) */
    @Query("SELECT * FROM bookmarks ORDER BY created_at DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    /** Get bookmarks in a specific folder */
    @Query("SELECT * FROM bookmarks WHERE folder_name = :folder ORDER BY position ASC")
    fun getBookmarksByFolder(folder: String): Flow<List<Bookmark>>

    /** Check if a URL is already bookmarked */
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    fun isBookmarked(url: String): Flow<Boolean>

    /** Check if a URL is already bookmarked (suspend version) */
    @Query("SELECT COUNT(*) FROM bookmarks WHERE url = :url")
    suspend fun bookmarkCount(url: String): Int

    /** Get a bookmark by its URL */
    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): Bookmark?

    /** Get bookmark by ID */
    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmarkById(id: Long): Bookmark?

    /** Insert a bookmark. If URL already exists, replace it. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: Bookmark): Long

    /** Update an existing bookmark */
    @Update
    suspend fun update(bookmark: Bookmark)

    /** Delete a bookmark */
    @Delete
    suspend fun delete(bookmark: Bookmark)

    /** Delete a bookmark by its ID */
    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Delete all bookmarks */
    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()

    /** Get total bookmark count */
    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun count(): Int

    /** Get all folder names */
    @Query("SELECT DISTINCT folder_name FROM bookmarks WHERE folder_name IS NOT NULL")
    fun getAllFolders(): Flow<List<String>>

    /** Search bookmarks by title or URL */
    @Query("""
        SELECT * FROM bookmarks 
        WHERE title LIKE '%' || :query || '%' 
           OR url LIKE '%' || :query || '%'
        ORDER BY created_at DESC
    """)
    fun search(query: String): Flow<List<Bookmark>>
}
