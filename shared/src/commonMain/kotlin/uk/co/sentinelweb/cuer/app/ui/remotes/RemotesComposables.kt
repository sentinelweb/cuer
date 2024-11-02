package uk.co.sentinelweb.cuer.app.ui.remotes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import uk.co.sentinelweb.cuer.app.ui.common.compose.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerMenuItem
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedAppBarComposables.CuerSharedAppBar
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.colorTransparentBlack
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
                Box(contentAlignment = Alignment.TopStart) {
                    CuerSharedAppBar(
                        title = stringResource(Res.string.rm_title),
                        backgroundColor = colorTransparentBlack,
                        contentColor = Color.White,
                        modifier = Modifier
                            .zIndex(1f)
                    )
                    RemotesScreen(model.value, view as BaseMviView<Model, Event>)
                }
            }
        }
    }

    @Composable
    fun RemotesViewApp(model: Model, view: BaseMviView<Model, Event>) {
        CuerSharedTheme {
            Surface {
                Box(contentAlignment = Alignment.TopStart) {
                    CuerSharedAppBar(
                        title = model.title,
                        backgroundColor = Color.Transparent,
                        contentColor = Color.White,
                        onUp = { view.dispatch(OnUpClicked) },
                        actions = listOf(
                            Action(CuerMenuItem.Help, { view.dispatch(OnActionHelpClicked) }),
                            Action(CuerMenuItem.Search, { view.dispatch(OnActionSearchClicked) }),
                            Action(CuerMenuItem.PasteAdd, { view.dispatch(OnActionPasteAdd) }),
                            Action(CuerMenuItem.Settings, { view.dispatch(OnActionSettingsClicked) }),
                            Action(CuerMenuItem.LocalConfig, { view.dispatch(OnActionConfigClicked) }),
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
            Column(
                modifier = Modifier, // fixme need to calc height for device (or make scrollable)
            ) {
                model.remoteNodes.forEach { remote ->
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
        Box(modifier = Modifier.height(220.dp)) {
            Image(
                painter = painterResource(Res.drawable.header_remotes),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
            ) {
                RowLocalNodeInfo(model.localNode, Color.White)
                model.wifiState
                    .takeIf { it.isConnected }
                    ?.run {
                        Text(
                            text = if (!isObscured) "SSID: $ssid" else "SSID hidden",
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    if (isObscured) {
                                        view.dispatch(OnActionObscuredPermClicked)
                                    }
                                },
                        )
                    }
                RowLocalNodeButtons(model, view)
                Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.Gray))
            }
        }
    }

    @Composable
    private fun RowLocalNodeButtons(
        model: Model,
        view: BaseMviView<Model, Event>
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
        }
    }


    @Composable
    private fun RemoteRow(remote: RemotesContract.View.RemoteNodeModel, view: BaseMviView<Model, Event>) {
        var expanded by remember { mutableStateOf(false) }
        var editAddress by remember { mutableStateOf(false) }

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
                contentAlignment = Alignment.CenterStart,
            ) {
                if (editAddress) {
                    IpAddressPortInput(
                        remote.address,
                        onSubmit = {
                            view.dispatch(OnActionEditAddress(remote.domain, it))
                            editAddress = false
                        }
                    )
                } else {
                    RowRemoteNodeInfo(remote, contentColor)
                }
                // overflow button and dropdown
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .wrapContentWidth(align = Alignment.End),
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

                        DropdownMenuItem(
                            text = { Text("Edit Address") },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            onClick = {
                                editAddress = !editAddress
                                expanded = false
                            })

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text("Sync") },
                            onClick = {
                                expanded = dispatchAndClose(view, OnActionSync(remote.domain))
                            })
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

    @Composable
    private fun RowRemoteNodeInfo(
        remote: RemotesContract.View.RemoteNodeModel,
        contentColor: Color
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
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }

    @Composable
    private fun RowLocalNodeInfo(
        remote: RemotesContract.View.LocalNodeModel,
        contentColor: Color
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
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }

    fun isValidIpAndPort(input: String): Boolean {
        val regex =
            Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):([0-9]{1,5})\$")
        return regex.matches(input) && input.split(":")[1].toIntOrNull()?.let { it in 0..65535 } == true
    }

    @Composable
    fun IpAddressPortInput(initial: String, onSubmit: (String) -> Unit) {
        var textFieldState by remember { mutableStateOf(initial) }
        var errorState by remember { mutableStateOf(false) }

        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp)
                    .align(Alignment.Start)
            ) {
                OutlinedTextField(
                    value = textFieldState,
                    onValueChange = {
                        textFieldState = it
                        errorState = !isValidIpAndPort(it)
                    },
                    label = { Text("Enter IP:Port") },
                    isError = errorState,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValidIpAndPort(textFieldState)) {
                                onSubmit(textFieldState)
                            } else {
                                errorState = true
                            }
                        }
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(0.7f),
                )
                Button(
                    onClick = {
                        if (isValidIpAndPort(textFieldState)) {
                            onSubmit(textFieldState)
                        } else {
                            errorState = true
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .size(width = 72.dp, height = 48.dp)
                        .align(CenterVertically)
                        .padding(start = 8.dp),
                    enabled = !errorState
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_tick),
                        tint = Color.White,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                    )
//                    Text("OK")
                }
            }
            if (errorState) {
                Text(
                    "Invalid IP address or port",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
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
