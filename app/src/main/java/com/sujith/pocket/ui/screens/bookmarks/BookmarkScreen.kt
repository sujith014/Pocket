package com.sujith.pocket.ui.screens.bookmarks

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sujith.pocket.domain.model.BookMarkDto
import com.sujith.pocket.ui.components.DebouncedIconButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bookmarks Screen
 */
@Composable
fun BookmarksScreen(
    modifier: Modifier = Modifier,
    uiState: BookmarksScreenUIState,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearchBar: () -> Unit,
    onToggleSortMenu: () -> Unit,
    onDismissSortMenu: () -> Unit,
    onSortTypeChange: (SortType) -> Unit,
    onShowDeleteDialog: (BookMarkDto?) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onDeleteBookmark: (BookMarkDto) -> Unit,
    onClearAllBookmarks: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Column layout instead of Scaffold (parent activity has scaffold)
    Column(modifier = modifier.fillMaxSize()) {
        // Custom Top Bar
        CustomBookmarksTopBar(
            title = "Bookmarks",
            showSearchBar = uiState.showSearchBar,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onToggleSearchBar = onToggleSearchBar,
            onNavigateBack = onNavigateBack,
            showSortMenu = uiState.showSortMenu,
            onToggleSortMenu = onToggleSortMenu,
            onDismissSortMenu = onDismissSortMenu,
            onSortTypeChange = onSortTypeChange,
            hasBookmarks = uiState.bookmarks.isNotEmpty(),
            onClearAll = { onShowDeleteDialog(null) }
        )

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.bookmarks.isEmpty()) {
                EmptyBookmarksView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.bookmarks,
                        key = { it.id }
                    ) { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onClick = { onBookmarkClick(bookmark.url) },
                            onDelete = { onShowDeleteDialog(bookmark) }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        DeleteBookmarkDialog(
            selectedBookmark = uiState.selectedBookmark,
            totalBookmarks = uiState.bookmarks.size,
            onDismiss = onDismissDeleteDialog,
            onConfirm = {
                if (uiState.selectedBookmark == null) {
                    onClearAllBookmarks()
                } else {
                    onDeleteBookmark(uiState.selectedBookmark)
                }
            }
        )
    }
}

/**
 * Custom Top Bar for Bookmarks Screen
 * Extracted as separate composable for reusability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBookmarksTopBar(
    title: String,
    showSearchBar: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearchBar: () -> Unit,
    onNavigateBack: () -> Unit,
    showSortMenu: Boolean,
    onToggleSortMenu: () -> Unit,
    onDismissSortMenu: () -> Unit,
    onSortTypeChange: (SortType) -> Unit,
    hasBookmarks: Boolean,
    onClearAll: () -> Unit
) {
    TopAppBar(
        title = {
            // Crossfade between Title <-> Search
            AnimatedContent(
                targetState = showSearchBar,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 2 } togetherWith
                            fadeOut() + slideOutVertically { -it / 2 }
                }
            ) { isSearchVisible ->
                if (isSearchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search bookmarks...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                } else {
                    Text(title)
                }
            }
        },
        navigationIcon = {
            DebouncedIconButton(
                onClick = {
                    if (showSearchBar) {
                        onToggleSearchBar()
                    } else {
                        onNavigateBack()
                    }
                }
            ) {
                // Smoothly animate between Back <-> Close icon
                AnimatedContent(
                    targetState = showSearchBar,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }
                ) { searchOpen ->
                    Icon(
                        if (searchOpen) Icons.Default.Close else Icons.Default.ArrowBack,
                        contentDescription = if (showSearchBar) "Close search" else "Back"
                    )
                }
            }
        },
        actions = {
            // Search button
            // Search icon only when search bar is NOT visible
            AnimatedVisibility(
                visible = !showSearchBar,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                DebouncedIconButton(onClick = onToggleSearchBar) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
            // Sort menu
            Box {
                DebouncedIconButton(onClick = onToggleSortMenu) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = onDismissSortMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("Latest First") },
                        onClick = {
                            onSortTypeChange(SortType.DATE_DESC)
                            onDismissSortMenu()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.NewReleases, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Oldest First") },
                        onClick = {
                            onSortTypeChange(SortType.DATE_ASC)
                            onDismissSortMenu()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.History, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Title (A-Z)") },
                        onClick = {
                            onSortTypeChange(SortType.TITLE_ASC)
                            onDismissSortMenu()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.SortByAlpha, contentDescription = null)
                        }
                    )
                }
            }

            // Clear all button
            if (hasBookmarks) {
                DebouncedIconButton(onClick = onClearAll) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Clear All",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

/**
 * Delete Confirmation Dialog
 * Extracted as separate composable
 */
@Composable
fun DeleteBookmarkDialog(
    selectedBookmark: BookMarkDto?,
    totalBookmarks: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                if (selectedBookmark == null) "Clear All Bookmarks?"
                else "Delete Bookmark?"
            )
        },
        text = {
            Text(
                if (selectedBookmark == null)
                    "This will delete all $totalBookmarks bookmarks. This action cannot be undone."
                else
                    "Are you sure you want to delete \"${selectedBookmark.title}\"?"
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Bookmark Item Card
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkItem(
    bookmark: BookMarkDto,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favicon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = bookmark.url,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(bookmark.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Actions menu
            Box {
                DebouncedIconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        onClick = {
                            onClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            // TODO: Implement share
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }
}

/**
 * Empty State View
 */
@Composable
fun EmptyBookmarksView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Bookmarks Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Save interesting articles and websites\nfor easy access later",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Helper function to format timestamp
 */
fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} min's ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        diff < 604800_000 -> "${diff / 86400_000} days ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}