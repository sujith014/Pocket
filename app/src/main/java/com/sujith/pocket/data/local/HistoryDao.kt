package com.sujith.pocket.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sujith.pocket.data.model.BookMarkEntity
import com.sujith.pocket.data.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY date DESC")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM history WHERE url = :url LIMIT 1)")
    suspend fun isInHistory(url: String): Boolean

}