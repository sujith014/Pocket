package com.sujith.pocket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sujith.pocket.ui.screens.bookmarks.BookmarksScreen
import com.sujith.pocket.ui.screens.bookmarks.BookmarksViewModel
import com.sujith.pocket.ui.screens.home.HomeScreen
import com.sujith.pocket.ui.screens.home.HomeScreenViewModel
import com.sujith.pocket.ui.screens.webview.WebScreenView
import com.sujith.pocket.ui.screens.webview.WebviewViewmodel
import kotlinx.serialization.Serializable


sealed interface AppRoutes {
    @Serializable
    data object Home : AppRoutes

    @Serializable
    data object Bookmarks : AppRoutes

    @Serializable
    data class WebViewScreen(val url: String) : AppRoutes
}


@Composable
fun AppNavController(
    modifier: Modifier,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Home
    ) {
        homeScreen(
            modifier = modifier,
            navController = navController
        )

        bookmarksScreen(
            modifier = modifier,
            navController = navController
        )

        webViewScreen(
            modifier = modifier
        )
    }
}

private fun NavGraphBuilder.homeScreen(
    modifier: Modifier,
    navController: NavHostController
) {
    composable<AppRoutes.Home> {
        val homeViewModel = hiltViewModel<HomeScreenViewModel>()

        HomeScreen(
            modifier = modifier,
            uiState = homeViewModel.state,
            onUrlChange = homeViewModel::onUrlChange,
            onResetUrl = homeViewModel::onResetUrl,
            onClickHistoryVisible = homeViewModel::onClickHistoryVisible,
            onWebViewLoad = handleWebViewLoad(homeViewModel, navController),
            onHistoryItemClick = { url ->
                navController.navigate(AppRoutes.WebViewScreen(url))
            }
        )
    }
}

private fun handleWebViewLoad(
    viewModel: HomeScreenViewModel,
    navController: NavHostController
): () -> Unit = {
    val url = viewModel.state.value.searchUrl.trim()
    if (url.isNotEmpty()) {
        // Only navigate if URL is valid, saveToHistory will validate and show toast if needed
        val saved = viewModel.saveToHistory()
        if (saved) {
            navController.navigate(AppRoutes.WebViewScreen(url))
        }
        // If saveToHistory returns false, it means validation failed and toast is already shown
    } else {
        // Show toast for empty URL
        viewModel.saveToHistory() // This will show the error toast
    }
}

private fun NavGraphBuilder.bookmarksScreen(
    modifier: Modifier,
    navController: NavHostController
) {
    composable<AppRoutes.Bookmarks> {
        val viewModel = hiltViewModel<BookmarksViewModel>()
        val uiState by viewModel.state.collectAsStateWithLifecycle()

        BookmarksScreen(
            modifier = modifier,
            uiState = uiState,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onToggleSearchBar = viewModel::toggleSearchBar,
            onToggleSortMenu = viewModel::toggleSortMenu,
            onDismissSortMenu = viewModel::dismissSortMenu,
            onSortTypeChange = viewModel::sortBookmarks,
            onShowDeleteDialog = viewModel::showDeleteDialog,
            onDismissDeleteDialog = viewModel::dismissDeleteDialog,
            onDeleteBookmark = viewModel::deleteBookmark,
            onClearAllBookmarks = viewModel::clearAllBookmarks,
            onBookmarkClick = navigateToWebView(navController),
            onNavigateBack = navigateBack(navController)
        )

    }
}

private fun navigateToWebView(navController: NavHostController): (String) -> Unit = { url ->
    navController.navigate(AppRoutes.WebViewScreen(url))
}

private fun navigateBack(navController: NavHostController): () -> Unit = {
    navController.popBackStack()
}

private fun NavGraphBuilder.webViewScreen(
    modifier: Modifier
) {
    composable<AppRoutes.WebViewScreen> { backStackEntry ->
        val viewModel = hiltViewModel<WebviewViewmodel>()
        val uiState by viewModel.state.collectAsStateWithLifecycle()
        val args = backStackEntry.toRoute<AppRoutes.WebViewScreen>()
        val url = args.url

        WebScreenView(
            modifier = modifier,
            url = url,
            uiState = uiState,

            // Progress & Loading callbacks
            onProgressChange = viewModel::updateProgress,
            onLoadingChange = viewModel::updateLoading,

            // Page info callbacks
            onPageTitleChange = viewModel::updatePageTitle,
            onCurrentUrlChange = viewModel::updateCurrentUrl,

            // Navigation state callbacks
            onNavigationStateChange = viewModel::updateNavigationState,

            // URL input callbacks
            onUrlInputChange = viewModel::updateUrlInput,
            onToggleUrlInput = viewModel::toggleUrlInput,

            // View mode callbacks
            onToggleDesktopMode = viewModel::toggleDesktopMode,

            // Download dialog callbacks
            onShowDownloadDialog = viewModel::showDownloadDialog,
            onDismissDownloadDialog = viewModel::dismissDownloadDialog,

            // Bottom bar visibility
            onUpdateBottomBarVisibility = viewModel::updateBottomBarVisibility,

            // Duplicate status callbacks
            onClearBookmarkDuplicate = viewModel::clearBookmarkDuplicateStatus,

            // History & Bookmark callbacks (suspend functions)
            onSaveToHistory = viewModel::saveToHistory,
            onBookmarkClick = viewModel::bookmarkClick,

            // Reader mode callbacks
            onShowReaderMode = viewModel::showReaderMode,
            onDismissReaderMode = viewModel::dismissReaderMode,
            onReaderContentExtracted = viewModel::updateReaderContent
        )
    }
}
