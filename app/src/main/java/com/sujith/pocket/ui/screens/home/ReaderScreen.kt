package com.sujith.pocket.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.sujith.pocket.ui.components.DebouncedButton
import com.sujith.pocket.ui.components.DebouncedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sujith.pocket.domain.model.HistoryDto
import com.sujith.pocket.ui.extensions.showToast
import com.sujith.pocket.ui.extensions.toFormattedDate
import com.sujith.pocket.ui.screens.home.HomeStatus
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    modifier: Modifier,
    uiState: StateFlow<HomeScreenUIState>,
    onUrlChange: (String) -> Unit,
    onResetUrl: () -> Unit,
    onClickHistoryVisible: () -> Unit,
    onWebViewLoad: () -> Unit,
    onHistoryItemClick: (String) -> Unit
) {

    val uiState by uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle status messages (toasts)
    LaunchedEffect(uiState.status) {
        when (val status = uiState.status) {
            is HomeStatus.Error -> {
                context.showToast(status.message)
            }
            is HomeStatus.Success -> {
                if (status.message.isNotEmpty()) {
                    context.showToast(status.message)
                }
            }
            else -> { /* Idle or Loading - no toast needed */ }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            text = "Pocket Reader",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = uiState.searchUrl,
                    onValueChange = onUrlChange,
                    placeholder = { Text("Paste or enter URL…") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        if (uiState.searchUrl.isNotEmpty()) {
                            DebouncedIconButton(onClick = onResetUrl) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                DebouncedButton(
                    onClick = onWebViewLoad,
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 9.dp)
                .clickable { onClickHistoryVisible() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History - ${uiState.history.size}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            DebouncedIconButton(onClick = onClickHistoryVisible) {
                Icon(
                    imageVector = if (uiState.isHistoryViewVisible) Icons.Default.ArrowDownward
                    else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Toggle History"
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isHistoryViewVisible
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                items(uiState.history.size) { index ->
                    HistoryItems(
                        historyDto = uiState.history[index],
                        onClick = { onHistoryItemClick(uiState.history[index].url) }
                    )
                }
            }
        }
    }
}


@Composable
fun HistoryItems(
    historyDto: HistoryDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = historyDto.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 2
                )

                Text(
                    text = historyDto.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = historyDto.date.toFormattedDate(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}




