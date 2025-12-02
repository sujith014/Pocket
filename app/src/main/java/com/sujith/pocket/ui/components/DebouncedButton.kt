package com.sujith.pocket.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sujith.pocket.utils.Constants.CLICK_DEBOUNCE_DELAY

@Composable
fun DebouncedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    
    Button(
        onClick = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > CLICK_DEBOUNCE_DELAY) {
                lastClickTime = currentTime
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp)
    ) {
        content()
    }
}

@Composable
fun DebouncedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    
    IconButton(
        onClick = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > CLICK_DEBOUNCE_DELAY) {
                lastClickTime = currentTime
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled
    ) {
        content()
    }
}

