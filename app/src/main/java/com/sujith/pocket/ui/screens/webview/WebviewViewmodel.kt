package com.sujith.pocket.ui.screens.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujith.pocket.domain.model.BookMarkDto
import com.sujith.pocket.domain.model.HistoryDto
import com.sujith.pocket.domain.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebviewViewmodel @Inject constructor(
    private val repository: BookmarkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WebViewScreenUIState())
    val state = _state.asStateFlow()

    /**
     * Update progress and loading state together
     * Optimized to batch state updates
     */
    fun updateProgress(progress: Int) {
        _state.update { currentState ->
            currentState.copy(
                progress = progress,
                isLoading = progress < 100
            )
        }
    }

    fun updateLoading(isLoading: Boolean) {
        _state.update { it.copy(isLoading = isLoading) }
    }

    fun updatePageTitle(title: String) {
        _state.update { it.copy(pageTitle = title) }
    }

    /**
     * Update URL and URL input together
     * Optimized to batch state updates
     */
    fun updateCurrentUrl(url: String) {
        _state.update { currentState ->
            currentState.copy(
                currentUrl = url,
                urlInput = url
            )
        }
    }

    /**
     * Update navigation state (back/forward)
     * Batched update for better performance
     */
    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    fun updateUrlInput(url: String) {
        _state.update { it.copy(urlInput = url) }
    }

    fun toggleUrlInput() {
        _state.update { currentState ->
            currentState.copy(showUrlInput = !currentState.showUrlInput)
        }
    }

    fun toggleDesktopMode() {
        _state.update { currentState ->
            currentState.copy(isDesktopMode = !currentState.isDesktopMode)
        }
    }

    fun showDownloadDialog() {
        _state.update { it.copy(showDownloadDialog = true) }
    }

    fun dismissDownloadDialog() {
        _state.update { it.copy(showDownloadDialog = false) }
    }

    fun updateBottomBarVisibility(isVisible: Boolean) {
        _state.update { it.copy(isBottomBarVisible = isVisible) }
    }

    fun clearHistoryDuplicateStatus() {
        _state.update { it.copy(isHistoryDuplicate = false) }
    }

    fun clearBookmarkDuplicateStatus() {
        _state.update { it.copy(isBookmarkDuplicate = false) }
    }

    /**
     * Reader Mode functions
     */
    fun showReaderMode() {
        _state.update { it.copy(showReaderMode = true) }
    }

    fun dismissReaderMode() {
        _state.update {
            it.copy(
                showReaderMode = false,
                readerContent = "" // Clear content when dismissed
            )
        }
    }

    fun updateReaderContent(content: String) {
        _state.update { it.copy(readerContent = content) }
    }

    /**
     * Save to history
     * Returns true if saved, false if duplicate
     */
    suspend fun saveToHistory(url: String, title: String): Boolean {
        return try {
            val isDuplicate = repository.isInHistory(url)
            if (isDuplicate) {
                _state.update { currentState ->
                    currentState.copy(
                        status = WebViewStatus.Success("Already in history"),
                        isHistoryDuplicate = true
                    )
                }
                false
            } else {
                val historyDto = HistoryDto(
                    title = title.ifEmpty { "Untitled" },
                    url = url,
                    date = System.currentTimeMillis()
                )
                repository.saveToHistory(historyDto)
                _state.update { currentState ->
                    currentState.copy(
                        status = WebViewStatus.Success("Saved to history"),
                        isHistoryDuplicate = false
                    )
                }
                true
            }
        } catch (e: Exception) {
            _state.update { currentState ->
                currentState.copy(
                    status = WebViewStatus.Error(e.message ?: "Failed to save to history"),
                    isHistoryDuplicate = false
                )
            }
            false
        }
    }

    /**
     * Add bookmark
     * Returns true if saved, false if duplicate
     */
    suspend fun bookmarkClick(url: String, title: String): Boolean {
        return try {
            val isBookmarked = repository.isBookmarked(url).first()
            if (isBookmarked) {
                _state.update { currentState ->
                    currentState.copy(
                        status = WebViewStatus.Success("Already bookmarked"),
                        isBookmarkDuplicate = true
                    )
                }
                false
            } else {
                val bookMarkDto = BookMarkDto(
                    title = title.ifEmpty { "Untitled" },
                    url = url,
                    date = System.currentTimeMillis()
                )
                repository.insertBookmark(bookMarkDto)
                _state.update { currentState ->
                    currentState.copy(
                        status = WebViewStatus.Success("Bookmarked"),
                        isBookmarkDuplicate = false
                    )
                }
                true
            }
        } catch (e: Exception) {
            _state.update { currentState ->
                currentState.copy(
                    status = WebViewStatus.Error(e.message ?: "Failed to bookmark"),
                    isBookmarkDuplicate = false
                )
            }
            false
        }
    }
}