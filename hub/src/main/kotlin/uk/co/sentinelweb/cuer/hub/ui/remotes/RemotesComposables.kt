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
import loadSVG
import org.koin.core.context.GlobalContext
import toImageBitmap
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesModelMapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.hub.ui.common.button.HeaderButton
import uk.co.sentinelweb.cuer.hub.ui.common.image.ImageFromUrl
import uk.co.sentinelweb.cuer.remote.server.ServerState

object RemotesComposables {

    val log: LogWrapper by lazy { GlobalContext.get().get<LogWrapper>().apply { tag(this) } }

    @Composable
    fun RemotesUi(coordinator: RemotesUiCoordinator) {
        val state = coordinator.modelObservable
            .collectAsState(initial = RemotesModelMapper.blankModel())
        RemotesView(state.value, coordinator)
    }

    @Composable
    fun RemotesView(
        model: Model,
        view: BaseMviView<Model, Event>
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
        view: BaseMviView<Model, Event>
    ) {
        Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
            model.imageUrl
                ?.also { url ->
                    ImageFromUrl(url, Modifier.fillMaxWidth())
                }
            model.wifiState.ssid?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body2,
                    color = Color.White,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 8.dp,
                        bottom = 0.dp,
                    )
                )
            }
        }
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
            )
        ) {
            when (model.serverState) {
                ServerState.STOPPED, ServerState.INITIAL -> HeaderButton(
                    if (model.wifiState.isConnected) "Start" else "No WiFi",
//                    R.drawable.ic_play, // icon
                    enabled = model.wifiState.isConnected
                ) { view.dispatch(Event.OnActionStartServerClicked) }

                ServerState.STARTED -> HeaderButton("Stop") { view.dispatch(Event.OnActionStopServerClicked) }
            }
            if (model.serverState == ServerState.STARTED) {
                HeaderButton("Ping") { view.dispatch(Event.OnActionPingMulticastClicked) }
            }
            HeaderButton("Config") { view.dispatch(Event.OnActionConfigClicked) }
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
                Image(
                    bitmap = loadSVG("drawable/ic_wifi_tethering.svg", Color.Blue, 48)
                        .toImageBitmap(),
                    contentDescription = "SVG Icon",
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