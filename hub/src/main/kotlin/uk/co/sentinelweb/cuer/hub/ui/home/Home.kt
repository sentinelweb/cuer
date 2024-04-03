package uk.co.sentinelweb.cuer.hub.ui.home

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesComposables.RemotesUi

fun home(coordinator: HomeUiCoordinator) = application {
    val windowState = rememberWindowState()

    fun onExit() {
        coordinator.destroy()
        exitApplication()
    }

    Window(
        onCloseRequest = ::onExit,
        state = windowState,
        title = "Cuer Hub"
    ) {
        Home(coordinator)
    }
}


@Composable
@Preview
fun Home(coordinator: HomeUiCoordinator) {
    var text by remember { mutableStateOf("Hello, World!") }
    val state = remember { mutableStateOf(HomeModel(1)) }
    coordinator.observeModel { newModel -> state.value = newModel }

    MaterialTheme {
        RemotesUi(coordinator.remotes)
    }
}