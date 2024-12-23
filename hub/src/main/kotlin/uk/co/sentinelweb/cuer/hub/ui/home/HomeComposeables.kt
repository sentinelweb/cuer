package uk.co.sentinelweb.cuer.hub.ui.home

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import uk.co.sentinelweb.cuer.app.ui.common.compose.*
import uk.co.sentinelweb.cuer.app.ui.local.LocalComposables
import uk.co.sentinelweb.cuer.hub.ui.home.HomeContract.HomeModel.DisplayRoute.*
import uk.co.sentinelweb.cuer.hub.ui.home.HomeContract.Label
import uk.co.sentinelweb.cuer.hub.ui.home.HomeContract.Label.None
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
        .collectAsState(initial = HomeContract.HomeModel(Settings))
    val snackbarHostState = remember { SnackbarHostState() }
    val label = coordinator.label.collectAsState(initial = None)

    LaunchedEffect(label.value) {
        when (label.value) {
            is Label.ErrorMessage -> snackbarHostState.showSnackbar(
                message = (label.value as Label.ErrorMessage).message,
                duration = SnackbarDuration.Short
            )

            else -> Unit
        }
    }

    CuerSharedTheme {
        Scaffold(
            topBar = {
                CuerSharedAppBarComposables.CuerSharedAppBar(
                    title = "Cuer hub",
                    contentColor = Color.White,
                    backgroundColor = Color(0xFF222222),
                    actions = listOf(
                        Action(CuerMenuItem.LocalConfig, action = { coordinator.go(LocalConfig) }),
                        Action(CuerMenuItem.Folders, action = { coordinator.go(Folders()) }),
                        Action(CuerMenuItem.Settings, action = { coordinator.go(Settings) }),
                    ),
                    overflowActions = listOf(
                        Action(CuerMenuItem.ThemeTest, action = { coordinator.go(ThemeTest) }),
                        Action(CuerMenuItem.Help, action = {}),
                    ),
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState, modifier = Modifier.padding(8.dp)) { data ->
                    CustomSnackbar(snackbarData = data)
                }
            },
        ) { padding ->
            Row(modifier = Modifier.padding(padding)) {
                Box(modifier = Modifier.width(400.dp)) {
                    coordinator.remotesCoordinator.RemotesDesktopUi()
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    val route = state.value.route
                    when (route) {
                        Settings -> PreferencesUi(coordinator.preferencesUiCoordinator)
                        is Folders -> coordinator.filesUiCoordinator.FileBrowserDesktopUi()
                        ThemeTest -> SharedThemeView.View()
                        LocalConfig -> LocalComposables.LocalDesktopUi(coordinator.localCoordinator)
                    }
                }
            }
        }
    }
}
