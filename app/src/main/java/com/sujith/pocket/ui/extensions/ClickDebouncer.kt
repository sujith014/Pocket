package com.sujith.pocket.ui.extensions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.sujith.pocket.utils.Constants.CLICK_DEBOUNCE_DELAY

fun CoroutineScope.debounceClick(
    onClick: () -> Unit
): () -> Unit {
    var lastClickTime by mutableLongStateOf(0L)
    
    return {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > CLICK_DEBOUNCE_DELAY) {
            lastClickTime = currentTime
            launch {
                onClick()
            }
        }
    }
}

