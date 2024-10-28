package uk.co.sentinelweb.cuer.hub.ui.home

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesComposeables.FileBrowserDesktopUi
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesComposables
import uk.co.sentinelweb.cuer.hub.ui.home.HomeModel.DisplayRoute.Files
import uk.co.sentinelweb.cuer.hub.ui.home.HomeModel.DisplayRoute.Settings
import uk.co.sentinelweb.cuer.hub.ui.preferences.PreferenceComposeables.PreferencesUi
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncher
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncherComposeables.ShowRemotesDialogIfNecessary

fun home(coordinator: HomeUiCoordinator) = application {
    val windowState = rememberWindowState(
        position = WindowPosition(Alignment.Center), size = DpSize(1280.dp, 768.dp)
    )

    fun onExit() {
        coordinator.destroy()
        exitApplication()
    }

    Window(
        onCloseRequest = ::onExit,
        state = windowState,
        title = "Cuer Hub",
    ) {
        Home(coordinator)
    }
}

@Composable
@Preview
fun Home(coordinator: HomeUiCoordinator) {
    val state = coordinator.modelObservable
        .collectAsState(initial = HomeModel(Settings))
    MaterialTheme {
        Scaffold(
            topBar = {
                //CuerSharedAppBarComposables.CuerSharedAppBar
                TopAppBar(
                    title = { Text(text = "Cuer hub") },
                    actions = {
                        IconButton(onClick = { coordinator.go(Files) }) {
                            Icon(Icons.Filled.List, contentDescription = "Files")
                        }
                        IconButton(onClick = { coordinator.go(Settings) }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        ) {
            Row {
                Box(modifier = Modifier.width(400.dp)) {
                    coordinator.remotes.RemotesDesktopUi()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(Color.White)
                ) {
                    when (state.value.route) {
                        Settings -> PreferencesUi(coordinator.preferencesUiCoordinator)
                        Files -> FileBrowserDesktopUi(coordinator.filesUiCoordinator.viewModel)
                    }
                }
            }
        }
    }
}
