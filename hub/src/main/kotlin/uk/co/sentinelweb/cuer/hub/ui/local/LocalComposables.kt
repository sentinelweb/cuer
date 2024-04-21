package uk.co.sentinelweb.cuer.hub.ui.local


import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model.Companion.blankModel
import uk.co.sentinelweb.cuer.app.ui.local.LocalModelMapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain.AuthConfig.*
import uk.co.sentinelweb.cuer.domain.ext.deserialiseLocalNode
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.hub.ui.common.button.HeaderButton
import uk.co.sentinelweb.cuer.hub.ui.common.button.HeaderButtonSolid

object LocalComposables {


    val log: LogWrapper by lazy {
        GlobalContext.get().get<LogWrapper>()
            .apply { tag = "LocalComposables" }
    }

    @Composable
    fun showDialog(
        isDialogOpen: Boolean,
        coordinator: LocalUiCoordinator,
        onClose: () -> Unit,
    ) {
        if (isDialogOpen) {
            AlertDialog(
                onDismissRequest = onClose,
                title = { Text(text = "Local Node") },
                buttons = { },
//                buttons = {
//                    HeaderButton(
//                        text = "Close",
//                        icon = "drawable/ic_clear.svg",
//                        modifier = Modifier.padding(
//                            start = 8.dp,
//                        )
//                    ) {
//                        onClose()
//                    }
//                },
                text = {
                    LocalUi(coordinator, onClose)
                }
            )
        }
    }


    @Composable
    fun LocalUi(
        coordinator: LocalUiCoordinator,
        onClose: () -> Unit,
    ) {
        val state = coordinator.modelObservable.collectAsState(initial = blankModel())
        LocalView(state.value, coordinator, onClose)
    }

    @Composable
    fun LocalView(
        model: LocalContract.View.Model,
        view: BaseMviView<LocalContract.View.Model, LocalContract.View.Event>,
        onClose: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 128.dp)
        ) {
            Row(modifier = Modifier.height(160.dp)) {
                model.imageUrl
                    ?.also { url ->
                        //ImageFromUrl(url, modifier = Modifier.fillMaxWidth().background(Color.Black))
                    }
            }
            var localNode by rememberSaveable(
                model,
                saver = stateSaverLocalDomain()
            ) { mutableStateOf(model.localNodeDomain) }
            log.d(localNode.serialise())
            Row(
                modifier = Modifier.padding(
                    start = 8.dp,//dimensionResource(R.dimen.app_bar_header_margin_start)
                    top = 16.dp,
                )
            ) {
                HeaderButtonSolid(
                    "Save",
                    "drawable/ic_tick.svg"
                ) {
                    view.dispatch(LocalContract.View.Event.OnActionSaveClicked(localNode))
                    onClose()
                }
            }
            Text(
                text = model.localNodeDomain.id?.id?.value ?: "No ID",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(8.dp)
            )
            TextField(
                value = localNode.hostname ?: "",
                onValueChange = { localNode = localNode.copy(hostname = it) },
                label = { Text("Hostname") },
                modifier = Modifier.padding(16.dp)
            )

            TextField(
                value = localNode.port.toString(),
                onValueChange = {
                    val portValue = it.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.toInt() ?: 0
                    localNode = localNode.copy(port = portValue)
                    // portInt = portValue
                },
                label = { Text("Port") },
                modifier = Modifier.padding(16.dp)
            )
            // auth config
            var authDropdownExpanded by remember { mutableStateOf(false) }
            HeaderButton(
                text = localNode.authConfig::class.simpleName!!,
                icon = "drawable/ic_login.svg",
                modifier = Modifier.padding(start = 16.dp)
            ) { authDropdownExpanded = true }
            DropdownMenu(
                expanded = authDropdownExpanded,
                onDismissRequest = { authDropdownExpanded = false },
            ) {
                DropdownMenuItem(onClick = {
                    localNode = localNode.copy(authConfig = Open)
                    authDropdownExpanded = false
                }) { Text(text = "Open") }

                DropdownMenuItem(onClick = {
                    localNode = localNode.copy(authConfig = Confirm)
                    authDropdownExpanded = false
                }) { Text(text = "Confirm") }

                DropdownMenuItem(onClick = {
                    localNode = localNode.copy(authConfig = Username("", ""))
                    authDropdownExpanded = false
                }) { Text(text = "Password") }
            }
            when (localNode.authConfig) {
                Open -> Unit
                Confirm -> Unit
                is Username -> {

                    TextField(
                        value = (localNode.authConfig as Username).username,
                        onValueChange = {
                            localNode = localNode.copy(
                                authConfig = (localNode.authConfig as Username).copy(
                                    username = it
                                )
                            )
                        },
                        label = { Text("Username") },
                        modifier = Modifier.padding(16.dp)
                    )

                    TextField(
                        value = (localNode.authConfig as Username).password,
                        onValueChange = {
                            localNode = localNode.copy(
                                authConfig = (localNode.authConfig as Username).copy(
                                    password = it
                                )
                            )
                        },
                        label = { Text("Password") },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            // wificonfig
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = localNode.wifiAutoStart,
                    onCheckedChange = {
                        log.d("wifiAutoStart: $it")
                        localNode = localNode.copy(wifiAutoStart = it)
                    }
                )
                Text(text = "Wifi Auto Start")
            }
            if (localNode.wifiAutoStart) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    val showAdd = model.wifiState
                        .takeIf { it.isConnected && !it.isObscured }
                        ?.let { localNode.wifiAutoConnectSSIDs.contains(it.ssid).not() }
                        ?: false

                    if (showAdd) {
                        HeaderButton(
                            text = "Add",
                            icon = "drawable/ic_add.svg",
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            localNode =
                                localNode.copy(wifiAutoConnectSSIDs = localNode.wifiAutoConnectSSIDs.plus(model.wifiState.ssid!!))
                        }
                    }
                    localNode.wifiAutoConnectSSIDs.forEach {
                        HeaderButton(
                            text = it,
                            icon = "drawable/ic_clear.svg",
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            localNode =
                                localNode.copy(wifiAutoConnectSSIDs = localNode.wifiAutoConnectSSIDs.filter { ssid -> ssid != it })
                        }
                    }
                }
            }
            Row(modifier = Modifier.height(200.dp)) {}
        }
    }

    @Composable
    fun stateSaverLocalDomain() = Saver<MutableState<LocalNodeDomain>, String>(
        save = { state -> state.value.serialise() },
        restore = { value ->
            @Suppress("UNCHECKED_CAST")
            mutableStateOf((deserialiseLocalNode(value as String)))
        }
    )

    @Preview
    @Composable
    @ExperimentalAnimationApi
    fun RemotesPreview() {
        val modelMapper = LocalModelMapper(GlobalContext.get().get(), log)
        val view = object : BaseMviView<LocalContract.View.Model, LocalContract.View.Event>() {}
        LocalComposables.LocalView(
            modelMapper.map(LocalContract.MviStore.State(wifiState = WifiStateProvider.WifiState())),
            view,
            {}
        )
    }

}

