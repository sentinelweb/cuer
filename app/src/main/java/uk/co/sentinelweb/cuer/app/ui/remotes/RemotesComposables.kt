package uk.co.sentinelweb.cuer.app.ui.remotes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.google.accompanist.glide.rememberGlidePainter
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.compose.Const.PREVIEW_LOG_WRAPPER
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.HeaderButton
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerMenuItem
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerTopAppBarComposables
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain.DeviceType.ANDROID
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.remote.server.ServerState.*

object RemotesComposables {
    val log: LogWrapper by lazy { GlobalContext.get().get<LogWrapper>().apply { tag(this) } }

    @Composable
    fun RemotesUi(view: RemotesMviViewProxy) {
        RemotesView(view.observableModel, view)
    }

    // todo use scaffold
    @Composable
    fun RemotesView(model: Model, view: BaseMviView<Model, Event>) {
        CuerTheme {
            Surface {
                Box(contentAlignment = Alignment.TopStart) {
                    CuerTopAppBarComposables.CuerAppBar(
                        text = model.title,
                        backgroundColor = Color.Transparent,
                        onUp = { view.dispatch(OnUpClicked) },
                        actions = listOf(
                            Action(CuerMenuItem.Help, { view.dispatch(OnActionHelpClicked) }),
                            Action(CuerMenuItem.Search, { view.dispatch(OnActionSearchClicked) }),
                            Action(CuerMenuItem.PasteAdd, { view.dispatch(OnActionPasteAdd) }),
                            Action(CuerMenuItem.Settings, { view.dispatch(OnActionSettingsClicked) }),
                        ),
                        modifier = Modifier
                            .zIndex(1f)
                            .height(56.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.surface)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 128.dp)
                    ) {
                        Box(modifier = Modifier.height(160.dp)) {
                            model.imageUrl
                                ?.also { url ->
                                    Image(
                                        painter = rememberGlidePainter(request = url, fadeIn = true),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .wrapContentHeight(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            Text(
                                text = model.wifiState.run { "$connected $ssid $ip" },
                                color = colorResource(R.color.white),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.padding(
                                start = dimensionResource(R.dimen.app_bar_header_margin_start),
                                top = 16.dp,
                            )
                        ) {
                            when (model.serverState) {
                                STOPPED, INITIAL -> HeaderButton(
                                    if (model.wifiState.connected) "Start" else "No WiFi",
                                    R.drawable.ic_play,
                                    enabled = model.wifiState.connected
                                ) { view.dispatch(OnActionStartServerClicked) }

                                STARTED -> HeaderButton("Stop", R.drawable.ic_stop) { view.dispatch(OnActionStopServerClicked) }
                            }
                            if (model.serverState == STARTED) {
                                HeaderButton("Ping", R.drawable.ic_ping) { view.dispatch(OnActionPingMulticastClicked) }
                            }
                            HeaderButton("Config", R.drawable.ic_menu_settings) { view.dispatch(OnActionConfigClicked) }
                        }
                        model.address?.also {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.h3,
                                modifier = Modifier.padding(
                                    start = dimensionResource(R.dimen.app_bar_header_margin_start),
                                    top = 8.dp,
                                    bottom = 0.dp
                                )
                            )
                        }
                        model.localNode.apply {
                            Text(
                                text = "$hostname : $deviceType : ${authType}",
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(start = dimensionResource(R.dimen.app_bar_header_margin_start), top = 8.dp)
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.height(300.dp),
                            contentPadding = PaddingValues(top = 4.dp)
                        ) {
                            items(model.remoteNodes) { remote ->
                                RemoteRow(remote, view)
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    private fun RemoteRow(remote: RemotesContract.View.NodeModel, view: BaseMviView<Model, Event>) {
        val expanded = remember { mutableStateOf(false) }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    //.clickable { expanded.value = !expanded.value }
                    .background(MaterialTheme.colors.surface),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_wifi_tethering),
                    tint = MaterialTheme.colors.onSurface,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).padding(4.dp)
                )
                Column { // todo use textview
                    Text(
                        text = remote.title,
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Text(
                        text = remote.address,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = "${remote.device} : ${remote.deviceType} : ${remote.authType}",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            // overflow button and dropdown
            Box(
                modifier = Modifier.wrapContentWidth(align = Alignment.End),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    tint = colorResource(R.color.grey_500),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).padding(8.dp).clickable { expanded.value = !expanded.value },
                )
                DropdownMenu(
                    expanded = expanded.value,
                    modifier = Modifier.width(200.dp),
                    onDismissRequest = { expanded.value = false }
                ) {
                    DropdownMenuItem(onClick = { view.dispatch(OnActionPingNodeClicked(remote.domain as RemoteNodeDomain)) }) {
                        Text("Ping")
                    }
                    DropdownMenuItem(onClick = { /* Handle refresh! */ }) {
                        Text("Connect")
                    }
                    DropdownMenuItem(onClick = { /* Handle send feedback! */ }) {
                        Text("Sync")
                    }
                    Divider()
                    DropdownMenuItem(onClick = { /* Handle refresh! */ }) {
                        Text("Play")
                    }
                    DropdownMenuItem(onClick = { /* Handle settings! */ }) {
                        Text("Playlists")
                    }
                }
            }
        }
    }
}


@Preview(name = "Top level")
@Composable
@ExperimentalAnimationApi
private fun RemotesPreview() {
    val modelMapper = RemotesModelMapper(GlobalContext.get().get(), PREVIEW_LOG_WRAPPER)
    val localNode = LocalNodeDomain(
        id = null, ipAddress = "x.x.x.x", port = 1234, hostname = "hostname", deviceType = ANDROID,
    )
    val view = object : BaseMviView<Model, Event>() {}
    RemotesComposables.RemotesView(
        modelMapper.map(RemotesContract.MviStore.State(localNode = localNode, wifiState = WifiStateProvider.WifiState())),
        view
    )
}
