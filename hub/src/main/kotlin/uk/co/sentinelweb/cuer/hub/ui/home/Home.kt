package uk.co.sentinelweb.cuer.hub.ui.home

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import loadSVG
import toImageBitmap
import uk.co.sentinelweb.cuer.hub.ui.common.button.HeaderButton
import uk.co.sentinelweb.cuer.hub.ui.common.notif.AppleScriptNotif
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesComposables.RemotesUi

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
        .collectAsState(initial = HomeModel(1))
    MaterialTheme {
        Row {
            Box(modifier = Modifier.width(360.dp)) {
                RemotesUi(coordinator.remotes)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White)
            ) {
                Row {
                    val imageSize = 32
                    val svgImage = loadSVG("drawable/ic_wifi_tethering.svg", Color.Blue, imageSize)
                    val imageBitmap = svgImage.toImageBitmap()
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "SVG Icon",
                        modifier = Modifier
                            .padding(20.dp)
                            .width(imageSize.dp)
                            .height(imageSize.dp)
                    )
                    Text(
                        text = "Child",
                        color = Color.Black,
                        style = TextStyle(fontSize = 30.sp),
                        modifier = Modifier.padding(20.dp)
                    )
                    HeaderButton(
                        "Notif",
                        modifier = Modifier.padding(20.dp)
                    ) {
                        AppleScriptNotif.showNotification("Title", "text")
                    }
                }
            }
        }
    }
}