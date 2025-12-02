package com.sujith.pocket.data.repository

import com.sujith.pocket.data.local.BookMarkDao
import com.sujith.pocket.data.local.HistoryDao
import com.sujith.pocket.data.mappers.toBookMarkEntity
import com.sujith.pocket.data.mappers.toBookMarkDto
import com.sujith.pocket.data.mappers.toHistoryEntity
import com.sujith.pocket.data.mappers.toHistoryDto
import com.sujith.pocket.domain.model.BookMarkDto
import com.sujith.pocket.domain.model.HistoryDto
import com.sujith.pocket.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookMarkDao,
    private val historyDao: HistoryDao
) : BookmarkRepository {


    override fun getAllBookmarks(): Flow<List<BookMarkDto>> {
        return bookmarkDao.getBookmarks().map { entities ->
            entities.map { it.toBookMarkDto() }
        }
    }


    override suspend fun deleteBookmark(bookmark: BookMarkDto) {
        val bookmarkEntity = bookmark.toBookMarkEntity()
        bookmarkDao.delete(bookmarkEntity)
    }

    override suspend fun clearAllBookmarks() {
        bookmarkDao.clearAllBookmarks()
    }

    override fun isBookmarked(url: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(url)
    }

    override suspend fun insertBookmark(bookMarkDto: BookMarkDto) {
        val bookMarkEntity = bookMarkDto.toBookMarkEntity()
        bookmarkDao.insert(bookMarkEntity)
    }

    override suspend fun saveToHistory(historyDto : HistoryDto) {
        val historyEntity = historyDto.toHistoryEntity()
        historyDao.insert(historyEntity)
    }

    override fun getHistory(): Flow<List<HistoryDto>> {
        return historyDao.getHistory().map { entities ->
            entities.map { it.toHistoryDto() }
        }
    }

    override suspend fun isInHistory(url: String): Boolean {
        return historyDao.isInHistory(url)
    }

}