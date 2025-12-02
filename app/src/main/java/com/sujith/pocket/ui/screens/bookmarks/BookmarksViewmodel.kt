package com.sujith.pocket.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujith.pocket.domain.model.BookMarkDto
import com.sujith.pocket.domain.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter
import kotlin.collections.sortedBy
import kotlin.collections.sortedByDescending

enum class SortType {
    DATE_DESC,
    DATE_ASC,
    TITLE_ASC
}

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: BookmarkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksScreenUIState())
    val state: StateFlow<BookmarksScreenUIState> = _state.asStateFlow()

    // Get all bookmarks from repository
    private val allBookmarks = repository.getAllBookmarks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            combine(
                allBookmarks,
                _state.map { it.searchQuery },
                _state.map { it.sortType }
            ) { bookmarks, query, sortType ->
                var filtered = bookmarks

                // Apply search filter
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.url.contains(query, ignoreCase = true)
                    }
                }

                // Apply sorting
                val sorted = when (sortType) {
                    SortType.DATE_DESC -> filtered.sortedByDescending { it.date }
                    SortType.DATE_ASC -> filtered.sortedBy { it.date }
                    SortType.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
                }

                _state.value = _state.value.copy(
                    bookmarks = sorted,
                    status = BookmarksStatus.Success()
                )
            }.collect()
        }
    }

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun sortBookmarks(sortType: SortType) {
        _state.value = _state.value.copy(sortType = sortType)
    }

    fun toggleSearchBar() {
        _state.value = _state.value.copy(
            showSearchBar = !_state.value.showSearchBar,
            searchQuery = if (!_state.value.showSearchBar) "" else _state.value.searchQuery
        )
    }

    fun showDeleteDialog(bookmark: BookMarkDto? = null) {
        _state.value = _state.value.copy(
            showDeleteDialog = true,
            selectedBookmark = bookmark
        )
    }

    fun dismissDeleteDialog() {
        _state.value = _state.value.copy(
            showDeleteDialog = false,
            selectedBookmark = null
        )
    }

    fun toggleSortMenu() {
        _state.value = _state.value.copy(showSortMenu = !_state.value.showSortMenu)
    }

    fun dismissSortMenu() {
        _state.value = _state.value.copy(showSortMenu = false)
    }

    fun saveBookmark(url: String, title: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(status = BookmarksStatus.Loading)
            try {
                val bookmark = BookMarkDto(
                    title = title.ifEmpty { "Untitled" },
                    url = url,
                    date = System.currentTimeMillis()
                )
                repository.insertBookmark(bookmark)
                _state.value = _state.value.copy(status = BookmarksStatus.Success("Bookmark saved"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(status = BookmarksStatus.Error(e.message ?: "Failed to save bookmark"))
            }
        }
    }

    fun deleteBookmark(bookmark: BookMarkDto) {
        viewModelScope.launch {
            _state.value = _state.value.copy(status = BookmarksStatus.Loading)
            try {
                repository.deleteBookmark(bookmark)
                _state.value = _state.value.copy(
                    status = BookmarksStatus.Success("Bookmark deleted"),
                    showDeleteDialog = false,
                    selectedBookmark = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(status = BookmarksStatus.Error(e.message ?: "Failed to delete bookmark"))
            }
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(status = BookmarksStatus.Loading)
            try {
                repository.clearAllBookmarks()
                _state.value = _state.value.copy(
                    status = BookmarksStatus.Success("All bookmarks cleared"),
                    showDeleteDialog = false,
                    selectedBookmark = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(status = BookmarksStatus.Error(e.message ?: "Failed to clear bookmarks"))
            }
        }
    }

    fun isBookmarked(url: String): Flow<Boolean> {
        return repository.isBookmarked(url)
    }
}
