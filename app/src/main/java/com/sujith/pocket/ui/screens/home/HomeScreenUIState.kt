package com.sujith.pocket.ui.screens.home

import androidx.compose.runtime.Stable
import com.sujith.pocket.domain.model.HistoryDto


sealed class HomeStatus {
    object Idle : HomeStatus()
    object Loading : HomeStatus()
    data class Success(val message: String = "") : HomeStatus()
    data class Error(val message: String) : HomeStatus()
}

@Stable
data class HomeScreenUIState(
    val status: HomeStatus = HomeStatus.Idle,
    val searchUrl: String = "" ,
    val history : List<HistoryDto> = emptyList(),
    val isHistoryViewVisible : Boolean = false
)
