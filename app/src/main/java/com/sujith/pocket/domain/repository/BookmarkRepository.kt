package com.sujith.pocket.domain.repository

import com.sujith.pocket.domain.model.BookMarkDto
import com.sujith.pocket.domain.model.HistoryDto
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    fun getAllBookmarks(): Flow<List<BookMarkDto>>
    suspend fun insertBookmark(bookmark: BookMarkDto)
    suspend fun deleteBookmark(bookmark: BookMarkDto)
    suspend fun clearAllBookmarks()
    fun isBookmarked(url: String): Flow<Boolean>


    suspend fun saveToHistory(historyDto: HistoryDto)
    fun getHistory(): Flow<List<HistoryDto>>
    suspend fun isInHistory(url: String): Boolean
}