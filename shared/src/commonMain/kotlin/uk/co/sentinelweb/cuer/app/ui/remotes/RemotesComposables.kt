package uk.co.sentinelweb.cuer.app.ui.remotes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.rememberAsyncImagePainter
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import uk.co.sentinelweb.cuer.app.ui.common.compose.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerMenuItem
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedAppBarComposables
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.views.HeaderButton
import uk.co.sentinelweb.cuer.app.ui.common.compose.views.deleteSwipeResources
import uk.co.sentinelweb.cuer.app.ui.common.compose.views.editSwipeResources
import uk.co.sentinelweb.cuer.app.ui.common.compose.views.swipeToDismiss
import uk.co.sentinelweb.cuer.app.ui.common.mapper.ImageEnumMapper
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model.Companion.Initial
import uk.co.sentinelweb.cuer.remote.server.ServerState
import uk.co.sentinelweb.cuer.shared.generated.resources.*

object RemotesComposables {

    @Composable
    fun RemotesAppUi(view: RemotesContract.View) {
        val model = view.modelObservable.collectAsState(initial = Initial)
        RemotesViewApp(model.value, view as BaseMviView<Model, Event>)
    }

    @Composable
    fun RemotesDesktopUi(view: RemotesContract.View) {
        val model = view.modelObservable.collectAsState(initial = Initial)
        CuerSharedTheme {
            Surface {
                RemotesScreen(model.value, view as BaseMviView<Model, Event>)
            }
        }
    }

    @Composable
    fun RemotesViewApp(model: Model, view: BaseMviView<Model, Event>) {
        CuerSharedTheme {
            Surface {
                Box(contentAlignment = Alignment.TopStart) {
                    CuerSharedAppBarComposables.CuerSharedAppBar(
                        title = model.title,
                        backgroundColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
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
                    RemotesScreen(model, view)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun RemotesScreen(
        model: Model,
        view: BaseMviView<Model, Event>
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 128.dp)
        ) {
            Header(model, view)
            LazyColumn(
                modifier = Modifier
                    .height(400.dp), // fixme need to calc height for device (or make scrollable)
                contentPadding = PaddingValues(top = 4.dp)
            ) {
                items(model.remoteNodes) { remote ->
                    swipeToDismiss(
                        editSwipeResources {
                            view.dispatch(OnActionPingNodeClicked(remote.domain))
                        },
                        deleteSwipeResources {
                            view.dispatch(OnActionDelete(remote.domain))
                        }
                    ) { RemoteRow(remote, view) }
                }
            }
        }
    }

    @Composable
    private fun Header(
        model: Model,
        view: BaseMviView<Model, Event>
    ) {
        Box(modifier = Modifier.height(160.dp)) {
            model.imageUrl
                ?.also { url ->
                    Image(
                        //painter = rememberAsyncImagePainter(model.imageUrl),
                        painter = painterResource(Res.drawable.remotes),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .wrapContentHeight(),
                        contentScale = ContentScale.Crop
                    )
                }
            model.wifiState
                .takeIf { it.isConnected }
                ?.run {
                    Text(
                        text = if (!isObscured) "SSID: $ssid" else "SSID hidden",
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .clickable {
                                if (isObscured) {
                                    view.dispatch(OnActionObscuredPermClicked)
                                }
                            },
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
                    Res.drawable.ic_play,
                    enabled = model.wifiState.isConnected
                ) { view.dispatch(OnActionStartServerClicked) }

                ServerState.STARTED -> HeaderButton(
                    "Stop",
                    Res.drawable.ic_stop
                ) { view.dispatch(OnActionStopServerClicked) }
            }
            if (model.serverState == ServerState.STARTED) {
                HeaderButton(
                    "Ping",
                    Res.drawable.ic_ping
                ) { view.dispatch(OnActionPingMulticastClicked) }
            }
            HeaderButton(
                "Config",
                Res.drawable.ic_menu_settings
            ) { view.dispatch(OnActionConfigClicked) }
        }
        model.address?.also {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 8.dp,
                    bottom = 0.dp
                )
            )
        }
        model.localNode.apply {
            Text(
                text = "$hostname : $deviceType : ${authType}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 8.dp
                )
            )
        }
    }


    @Composable
    private fun RemoteRow(remote: RemotesContract.View.RemoteNodeModel, view: BaseMviView<Model, Event>) {
        var expanded by remember { mutableStateOf(false) }
        val contentColor = remote.domain.isAvailable
            .takeIf { it }
            ?.let { MaterialTheme.colorScheme.onSurface }
            ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(ImageEnumMapper.map(remote.deviceType)),
                        tint = contentColor,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                    )
                    Column { // todo use textview
                        Text(
                            text = remote.title,
                            color = contentColor,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .padding(start = 8.dp),
                        )
                        Text(
                            text = remote.address,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Text(
                            text = "${remote.deviceType} : ${remote.device} : ${remote.authType}",
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // overflow button and dropdown
                Box(
                    modifier = Modifier.wrapContentWidth(align = Alignment.End),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_more_vert),
                        tint = Color.Gray,
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
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.menu_ping)) },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            onClick = {
                                expanded = dispatchAndClose(view, OnActionPingNodeClicked(remote.domain))
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.menu_delete)) },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            onClick = {
                                expanded = dispatchAndClose(view, OnActionDelete(remote.domain))
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.menu_sendto)) },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            onClick = {
                                expanded = dispatchAndClose(view, OnActionSendTo(remote.domain))
                            })
//                        Divider()
//                    DropdownMenuItem(onClick = {
//                        expanded = dispatchAndClose(view, OnActionSync(remote.domain))
//                    }) {
//                        Text("Sync")
//                    }
//                        DropdownMenuItem(onClick = {
//                            expanded = dispatchAndClose(view, OnActionPlaylists(remote.domain))
//                        }) {
//                            Text(stringResource(R.string.playlists_title))
//                        }
//                        DropdownMenuItem(onClick = {
//                            expanded = dispatchAndClose(view, OnActionFolders(remote.domain))
//                        }) {
//                            Text(stringResource(R.string.folders))
//                        }
//                        DropdownMenuItem(onClick = {
//                            expanded = dispatchAndClose(view, OnActionCuerConnect(remote.domain))
//                        }) {
//                            Text(stringResource(R.string.remotes_cuer_connect))
//                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                HeaderButton(
                    stringResource(Res.string.rm_playlists),
                    Res.drawable.ic_playlists
                ) { view.dispatch(OnActionPlaylists(remote.domain)) }

                HeaderButton(
                    stringResource(Res.string.rm_folders),
                    Res.drawable.ic_folder_open
                ) { view.dispatch(OnActionFolders(remote.domain)) }

                HeaderButton(
                    stringResource(Res.string.rm_cuer_connect),
                    Res.drawable.ic_cuer_cast_connected
                ) { view.dispatch(OnActionCuerConnect(remote.domain)) }
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
