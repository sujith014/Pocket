package com.sujith.pocket.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujith.pocket.domain.model.HistoryDto
import com.sujith.pocket.domain.repository.BookmarkRepository
import com.sujith.pocket.ui.extensions.UrlUtils
import com.sujith.pocket.ui.screens.home.HomeStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val repository: BookmarkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenUIState())
    val state = _state.asStateFlow()


    init {
        viewModelScope.launch {
            repository.getHistory().collect {
                _state.value = _state.value.copy(history = it)
            }
        }
    }


    fun onUrlChange(url: String) {
        _state.value = _state.value.copy(searchUrl = url)
    }

    fun onResetUrl() {
        _state.value = _state.value.copy(searchUrl = "")
    }


    /**
     * Save to history with validation
     * Returns true if saved successfully, false if validation failed
     */
    fun saveToHistory(): Boolean {
        val url = _state.value.searchUrl.trim()
        
        // Validate URL is not empty
        if (url.isEmpty()) {
            _state.value = _state.value.copy(
                status = HomeStatus.Error("Please enter a URL")
            )
            return false
        }
        
        // Validate URL format
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _state.value = _state.value.copy(
                status = HomeStatus.Error("Please enter a valid URL")
            )
            return false
        }
        
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(status = HomeStatus.Loading)
                
                val webInfo = UrlUtils.fetchWebInfo(url)
                
                // Use fallback values if webInfo is empty
                val title = webInfo.title.ifEmpty { 
                    webInfo.domain.ifEmpty { 
                        UrlUtils.extractDomain(url).ifEmpty { "Untitled" }
                    }
                }
                val finalUrl = if (webInfo.domain.isNotEmpty()) {
                    url // Use original URL if domain extraction worked
                } else {
                    url // Fallback to original URL
                }
                
                val history = HistoryDto(
                    title = title,
                    url = finalUrl,
                    date = System.currentTimeMillis()
                )
                
                // Check for duplicates before saving
                val isDuplicate = repository.isInHistory(finalUrl)
                if (isDuplicate) {
                    _state.value = _state.value.copy(
                        status = HomeStatus.Success("Already in history")
                    )
                } else {
                    repository.saveToHistory(history)
                    _state.value = _state.value.copy(
                        status = HomeStatus.Success("Saved to history")
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    status = HomeStatus.Error("Failed to save: ${e.message ?: "Unknown error"}")
                )
            }
        }
        
        return true
    }


    fun onClickHistoryVisible() {
        _state.value = _state.value.copy(isHistoryViewVisible = !_state.value.isHistoryViewVisible)
    }


}