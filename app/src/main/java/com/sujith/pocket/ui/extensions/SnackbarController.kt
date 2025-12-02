package com.sujith.pocket.ui.extensions


import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class SnackbarEvent(
    val message: String,
    val action: SnackbarAction? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short
)

data class SnackbarAction (
    val label: String,
    val action: () -> Unit
)


object SnackbarController {
    private val _events = Channel<SnackbarEvent>()

    val events = _events.receiveAsFlow()


    suspend fun sendEvent (event: SnackbarEvent){
        _events.send(event)
    }

}

/*
object SnackbarControllerNew {

    private val snackbarHostState = SnackbarHostState()
    private lateinit var scope: CoroutineScope

    fun init(scope: CoroutineScope) {
        this.scope = scope
    }

    fun hostState(): SnackbarHostState = snackbarHostState

    fun show(message: String) {
        if (::scope.isInitialized) {
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }


}*/
