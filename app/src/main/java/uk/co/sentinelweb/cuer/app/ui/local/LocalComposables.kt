package uk.co.sentinelweb.cuer.app.ui.local

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
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
import uk.co.sentinelweb.cuer.app.ui.common.compose.HeaderButtonSolid
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerTopAppBarComposables.CuerAppBar
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore.State
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain.AuthConfig.*
import uk.co.sentinelweb.cuer.domain.ext.deserialiseLocalNode
import uk.co.sentinelweb.cuer.domain.ext.serialise

object LocalComposables {

    val log: LogWrapper by lazy {
        GlobalContext.get().get<LogWrapper>()
            .apply { tag = "LocalComposables" }
//            .apply { log.d("init") }
    }

    @Composable
    fun RemotesUi(view: LocalMviViewProxy) {
        RemotesView(view.observableModel, view)
    }

    // todo use scaffold
    @Composable
    fun RemotesView(model: Model, view: BaseMviView<Model, Event>) {
        CuerTheme {
            Surface {
                Box(contentAlignment = Alignment.TopStart) {
                    CuerAppBar(
                        text = model.title,
                        backgroundColor = colorResource(id = R.color.black_transparent_background),
                        onUp = { view.dispatch(Event.OnUpClicked) },
//                        actions = listOf(
//                            Action(CuerMenuItem.Help,
//                                { view.dispatch(OnActionHelpClicked) }),
//                            Action(CuerMenuItem.Search, { view.dispatch(OnActionSearchClicked) }),
//                            Action(CuerMenuItem.PasteAdd, { view.dispatch(OnActionPasteAdd) }),
//                            Action(CuerMenuItem.Settings, { view.dispatch(OnActionSettingsClicked) }),
//                        ),
                        modifier = Modifier.zIndex(1f).height(56.dp)
                    )
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
                        }
                        var localNode by rememberSaveable(model, saver = stateSaverLocalDomain()) { mutableStateOf(model.localNodeDomain) }
                        log.d(localNode.serialise())
                        Row(
                            modifier = Modifier.padding(
                                start = dimensionResource(R.dimen.app_bar_header_margin_start),
                                top = 16.dp,
                            )
                        ) {
                            HeaderButtonSolid("Save", R.drawable.ic_tick) { view.dispatch(Event.OnActionSaveClicked(localNode)) }
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
                            icon = R.drawable.ic_login,
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
                                        localNode = localNode.copy(authConfig = (localNode.authConfig as Username).copy(username = it))
                                    },
                                    label = { Text("Username") },
                                    modifier = Modifier.padding(16.dp)
                                )

                                TextField(
                                    value = (localNode.authConfig as Username).password,
                                    onValueChange = {
                                        localNode = localNode.copy(authConfig = (localNode.authConfig as Username).copy(password = it))
                                    },
                                    label = { Text("Password") },
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        // wificonfig
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = localNode.wifiAutoNotify,
                                onCheckedChange = { localNode = localNode.copy(wifiAutoNotify = it) }
                            )
                            Text(text = "Wifi Auto Notify")
                        }
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
                                        icon = R.drawable.ic_add,
                                        modifier = Modifier.padding(start = 16.dp)
                                    ) {
                                        localNode =
                                            localNode.copy(wifiAutoConnectSSIDs = localNode.wifiAutoConnectSSIDs.plus(model.wifiState.ssid!!))
                                    }
                                }
                                localNode.wifiAutoConnectSSIDs.forEach {
                                    HeaderButton(
                                        text = it,
                                        icon = R.drawable.ic_clear,
                                        modifier = Modifier.padding(start = 16.dp)
                                    ) {
                                        localNode =
                                            localNode.copy(wifiAutoConnectSSIDs = localNode.wifiAutoConnectSSIDs.filter { ssid -> ssid != it })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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

@Preview(name = "Top level")
@Composable
@ExperimentalAnimationApi
private fun RemotesPreview() {
    val modelMapper = LocalModelMapper(GlobalContext.get().get(), PREVIEW_LOG_WRAPPER)
    val view = object : BaseMviView<Model, Event>() {}
    LocalComposables.RemotesView(
        modelMapper.map(State(wifiState = WifiStateProvider.WifiState())),
        view
    )
}
