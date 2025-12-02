package com.sujith.pocket.ui.screens.bookmarks

import androidx.compose.runtime.Stable
import com.sujith.pocket.domain.model.BookMarkDto

@Stable
sealed class BookmarksStatus {
    object Idle : BookmarksStatus()
    object Loading : BookmarksStatus()
    data class Success(val message: String = "") : BookmarksStatus()
    data class Error(val message: String) : BookmarksStatus()
}

@Stable
data class BookmarksScreenUIState(
    val status: BookmarksStatus = BookmarksStatus.Idle,
    val bookmarks: List<BookMarkDto> = emptyList(),
    val searchQuery: String = "",
    val sortType: SortType = SortType.DATE_DESC,
    val showSearchBar: Boolean = false,
    val selectedBookmark: BookMarkDto? = null,
    val showDeleteDialog: Boolean = false,
    val showSortMenu: Boolean = false
)

