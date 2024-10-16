package uk.co.sentinelweb.cuer.hub.ui.remotes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import loadSVG
import org.koin.core.context.GlobalContext
import org.koin.java.KoinJavaComponent.getKoin
import toImageBitmap
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model.Companion.blankModel
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.hub.ui.common.button.HeaderButton
import uk.co.sentinelweb.cuer.hub.ui.common.image.ImageEnumMapper
import uk.co.sentinelweb.cuer.hub.ui.common.image.ImageFromUrl
import uk.co.sentinelweb.cuer.hub.ui.common.image.ImageSvg
import uk.co.sentinelweb.cuer.hub.ui.local.LocalComposables
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncherComposeables.ShowRemotesDialogIfNecessary
import uk.co.sentinelweb.cuer.remote.server.ServerState

object RemotesComposables {

    val log: LogWrapper by lazy { GlobalContext.get().get<LogWrapper>().apply { tag(this) } }

    @Composable
    fun RemotesUi(coordinator: RemotesUiCoordinator) {
        val state = coordinator.modelObservable.collectAsState(initial = blankModel())
        RemotesView(state.value, coordinator)
        ShowRemotesDialogIfNecessary(coordinator.remotesDialogLauncher)
    }

    @Composable
    fun RemotesView(
        model: Model,
        view: RemotesUiCoordinator
    ) {
        Column {
            Header(model, view)
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(top = 4.dp)
            ) {
                items(model.remoteNodes) { remote ->
                    RemoteRow(remote, view)
                }
            }
        }
    }

    @Composable
    private fun Header(
        model: Model,
        view: RemotesUiCoordinator
    ) {
        Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
            model.imageUrl
                ?.also { url ->
                    ImageFromUrl(url, modifier = Modifier.fillMaxWidth().background(Color.Black))
                }
            Column {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.h5,
                    color = Color.White,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 8.dp,
                        bottom = 0.dp,
                    ),

                    )
                model.wifiState.ssid?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.body2,
                        color = Color.White,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 8.dp,
                            bottom = 0.dp,
                        ),

                        )
                }
            }
        }
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
            )
        ) {
            var isDialogOpen by remember { mutableStateOf(false) }

            when (model.serverState) {
                ServerState.STOPPED, ServerState.INITIAL -> HeaderButton(
                    if (model.wifiState.isConnected) "Start" else "No WiFi",
                    "drawable/ic_play.svg", // icon
                    enabled = model.wifiState.isConnected
                ) { view.dispatch(Event.OnActionStartServerClicked) }

                ServerState.STARTED -> HeaderButton("Stop", "drawable/ic_stop.svg") {
                    view.dispatch(Event.OnActionStopServerClicked)
                }
            }
            if (model.serverState == ServerState.STARTED) {
                HeaderButton("Ping", "drawable/ic_ping.svg") {
                    view.dispatch(Event.OnActionPingMulticastClicked)
                }
            }
            HeaderButton("Config", "drawable/ic_menu_settings.svg") {
                //view.dispatch(Event.OnActionConfigClicked)
                isDialogOpen = true
            }
            LocalComposables.showDialog(
                isDialogOpen = isDialogOpen,
                coordinator = view.localCoordinator(),
                onClose = {
                    getKoin().get<CoroutineContextProvider>().mainScope.launch {
                        delay(50)
                        view.destroyLocalCoordinator()
                    }
                    isDialogOpen = false
                },
            )
        }
        model.address?.also {
            Text(
                text = it,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 8.dp,
                    bottom = 0.dp
                )
            )
        }

    }

    @Composable
    private fun RemoteRow(remote: RemoteNodeModel, view: BaseMviView<Model, Event>) {
        var expanded by remember { mutableStateOf(false) }
        val contentColor = remote.domain.isAvailable
            .takeIf { it }
            ?.let { MaterialTheme.colors.onSurface }
            ?: MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colors.surface),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ImageSvg(
                    ImageEnumMapper.map(remote.deviceType),
                    imageSize = 48,
                    modifier = Modifier
                        .padding(20.dp)
                        .width(48.dp)
                        .height(48.dp)
                )
                Column { // todo use textview
                    Text(
                        text = remote.title,
                        color = contentColor,
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier
                            .padding(start = 8.dp),
                    )
                    Text(
                        text = remote.address,
                        color = contentColor,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = "${remote.deviceType} : ${remote.device} : ${remote.authType}",
                        color = contentColor,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            // overflow button and dropdown
            Box(
                modifier = Modifier.wrapContentWidth(align = Alignment.End),
            ) {
                Image(
                    bitmap = loadSVG("drawable/ic_more_vert.svg", Color.Black, 48)
                        .toImageBitmap(),
                    // painter = painterResource(R.drawable.ic_more_vert),
                    // tint = colorResource(R.color.grey_500),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .clickable { expanded = !expanded },
                )
                DropdownMenu(
                    expanded = expanded,
                    modifier = Modifier.width(200.dp),
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        expanded = dispatchAndClose(view, Event.OnActionPingNodeClicked(remote.domain))
                    }) {
                        Text("Ping")
                    }
                    DropdownMenuItem(onClick = {
                        expanded = dispatchAndClose(view, Event.OnActionSync(remote.domain))
                    }) {
                        Text("Sync")
                    }
                    DropdownMenuItem(onClick = {
                        expanded = dispatchAndClose(view, Event.OnActionDelete(remote.domain))
                    }) {
                        Text("Delete")
                    }
                    DropdownMenuItem(onClick = {
                        expanded = dispatchAndClose(view, Event.OnActionSendTo(remote.domain))
                    }) {
                        Text("Send To ...")
                    }
                    Divider()
                    DropdownMenuItem(onClick = {
                        expanded = dispatchAndClose(view, Event.OnActionPlaylists(remote.domain))
                    }) {
                        Text("Playlists")
                    }
                }
            }
        }
    }

    private fun dispatchAndClose(
        view: BaseMviView<Model, Event>,
        event: Event
    ): Boolean {
        view.dispatch(event)
        return false
    }
}
