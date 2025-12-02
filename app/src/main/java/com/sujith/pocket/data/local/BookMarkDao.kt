package com.sujith.pocket.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sujith.pocket.data.model.BookMarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookMarkDao  {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookMarkEntity)

    @Query("SELECT * FROM bookmarks ORDER BY date DESC")
    fun getBookmarks(): Flow<List<BookMarkEntity>>

    @Delete
    suspend fun delete(bookmark: BookMarkEntity)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAllBookmarks()

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url LIMIT 1)")
    fun isBookmarked(url: String): Flow<Boolean>
}