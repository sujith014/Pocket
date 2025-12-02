package com.sujith.pocket.ui.screens.webview

sealed class WebViewStatus {
    object Idle : WebViewStatus()
    object Loading : WebViewStatus()
    data class Success(val message: String = "") : WebViewStatus()
    data class Error(val message: String) : WebViewStatus()
}

data class WebViewScreenUIState(
    val status: WebViewStatus = WebViewStatus.Idle,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val currentUrl: String = "",
    val pageTitle: String = "",
    val isDesktopMode: Boolean = false,
    val showDownloadDialog: Boolean = false,
    val isLoading: Boolean = false,
    val urlInput: String = "",
    val showUrlInput: Boolean = false,
    val isBottomBarVisible: Boolean = true,
    val isHistoryDuplicate: Boolean = false,
    val isBookmarkDuplicate: Boolean = false,
    val showReaderMode: Boolean = false,
    val readerContent: String = ""
)
