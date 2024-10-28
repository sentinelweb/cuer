package uk.co.sentinelweb.cuer.hub.ui.home

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
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
import uk.co.sentinelweb.cuer.app.ui.common.compose.SharedThemeView
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesComposeables.FileBrowserDesktopUi
import uk.co.sentinelweb.cuer.app.ui.local.LocalComposables
import uk.co.sentinelweb.cuer.hub.ui.home.HomeModel.DisplayRoute.*
import uk.co.sentinelweb.cuer.hub.ui.preferences.PreferenceComposeables.PreferencesUi

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
                        IconButton(onClick = { coordinator.go(ThemeTest) }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "ThemeTest")
                        }

                        IconButton(onClick = { coordinator.go(Files) }) {
                            Icon(Icons.Filled.List, contentDescription = "Files")
                        }

                        IconButton(onClick = { coordinator.go(Settings) }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }

                        IconButton(onClick = { coordinator.go(LocalConfig) }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Local config")
                        }
                    }
                )
            }
        ) {
            Row {
                Box(modifier = Modifier.width(400.dp)) {
                    coordinator.remotesCoordinator.RemotesDesktopUi()
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
                        ThemeTest -> SharedThemeView.View()
                        LocalConfig -> LocalComposables.LocalDesktopUi(coordinator.localCoordinator)
                    }
                }
            }
        }
    }
}
