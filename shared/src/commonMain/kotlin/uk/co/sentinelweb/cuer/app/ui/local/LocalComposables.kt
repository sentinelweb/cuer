package uk.co.sentinelweb.cuer.app.ui.local

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.jetbrains.compose.resources.painterResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedAppBarComposables.CuerSharedAppBar
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.views.HeaderButton
import uk.co.sentinelweb.cuer.app.ui.common.compose.views.HeaderButtonSolid
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore.State
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model.Companion.Initial
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain.AuthConfig.*
import uk.co.sentinelweb.cuer.domain.ext.deserialiseLocalNode
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.shared.generated.resources.*
import uk.co.sentinelweb.cuer.shared.generated.resources.Res
import uk.co.sentinelweb.cuer.shared.generated.resources.header_local_node
import uk.co.sentinelweb.cuer.shared.generated.resources.ic_clear
import uk.co.sentinelweb.cuer.shared.generated.resources.ic_tick

object LocalComposables : KoinComponent {

    val log: LogWrapper by inject<LogWrapper>()

    @Composable
    fun LocalAppUi(view: LocalContract.View) {
        val model = view.modelObservable.collectAsState(initial = Initial)
        LocalView(model.value, view as BaseMviView<Model, Event>)
    }

    @Composable
    fun LocalDesktopUi(view: LocalContract.View) {
        val model = view.modelObservable.collectAsState(initial = Initial)
        LocalView(model.value, view as BaseMviView<Model, Event>)

    }

    // todo use scaffold
    @Composable
    fun LocalView(model: Model, view: BaseMviView<Model, Event>) {
        CuerSharedTheme {
            Surface {
                Box(contentAlignment = Alignment.TopStart) {
                    CuerSharedAppBar(
                        title = model.title,
                        backgroundColor = Color.Transparent,
                        contentColor = Color.White,
                        onUp = { view.dispatch(Event.OnUpClicked) },
                        modifier = Modifier
                            .zIndex(1f)
                            .height(56.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 128.dp)
                    ) {
                        Row(modifier = Modifier.height(160.dp)) {
                            model.imageUrl
                                ?.also { url ->
                                    Image(
                                        painter = painterResource(Res.drawable.header_local_node),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .wrapContentHeight(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                        }
                        var localNode by rememberSaveable(
                            model,
                            saver = stateSaverLocalDomain()
                        ) { mutableStateOf(model.localNodeDomain) }

                        Row(
                            modifier = Modifier.padding(
                                start = 8.dp,
                                top = 16.dp,
                            )
                        ) {
                            HeaderButtonSolid("Save", Res.drawable.ic_tick) {
                                view.dispatch(
                                    Event.OnActionSaveClicked(
                                        localNode
                                    )
                                )
                            }
                        }

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
                            },
                            label = { Text("Port") },
                            modifier = Modifier.padding(16.dp)
                        )
                        // auth config
                        var authDropdownExpanded by remember { mutableStateOf(false) }
                        HeaderButton(
                            text = localNode.authConfig::class.simpleName!!,
                            icon = Res.drawable.ic_login,
                            modifier = Modifier.padding(start = 16.dp)
                        ) { authDropdownExpanded = true }

                        DropdownMenu(
                            expanded = authDropdownExpanded,
                            onDismissRequest = { authDropdownExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "Open") },
                                onClick = {
                                    localNode = localNode.copy(authConfig = Open)
                                    authDropdownExpanded = false
                                })

                            DropdownMenuItem(
                                text = { Text(text = "Confirm") },
                                onClick = {
                                    localNode = localNode.copy(authConfig = Confirm)
                                    authDropdownExpanded = false
                                })

                            DropdownMenuItem(
                                text = { Text(text = "Password") },
                                onClick = {
                                    localNode = localNode.copy(authConfig = Username("", ""))
                                    authDropdownExpanded = false
                                })
                        }
                        when (localNode.authConfig) {
                            Open -> Unit
                            Confirm -> Unit
                            is Username -> {

                                TextField(
                                    value = (localNode.authConfig as Username).username,
                                    onValueChange = {
                                        localNode =
                                            localNode.copy(authConfig = (localNode.authConfig as Username).copy(username = it))
                                    },
                                    label = { Text("Username") },
                                    modifier = Modifier.padding(16.dp)
                                )

                                TextField(
                                    value = (localNode.authConfig as Username).password,
                                    onValueChange = {
                                        localNode =
                                            localNode.copy(authConfig = (localNode.authConfig as Username).copy(password = it))
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
                                        icon = Res.drawable.ic_add,
                                        modifier = Modifier.padding(start = 16.dp)
                                    ) {
                                        localNode =
                                            localNode.copy(
                                                wifiAutoConnectSSIDs = localNode.wifiAutoConnectSSIDs
                                                    .plus(model.wifiState.ssid!!)
                                            )
                                    }
                                }
                                localNode.wifiAutoConnectSSIDs.forEach {
                                    HeaderButton(
                                        text = it,
                                        icon = Res.drawable.ic_clear,
                                        modifier = Modifier.padding(start = 16.dp)
                                    ) {
                                        localNode =
                                            localNode.copy(wifiAutoConnectSSIDs = localNode.wifiAutoConnectSSIDs.filter { ssid -> ssid != it })
                                    }
                                }
                            }
                        }
                        Row(modifier = Modifier.height(200.dp)) {
                            Text(
                                text = model.localNodeDomain.id?.id?.value ?: "No ID",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                modifier = Modifier.padding(8.dp)
                            )
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

//@Preview(name = "Top level")
//@Composable
//@ExperimentalAnimationApi
//private fun LocalPreview() {
//    val modelMapper = LocalModelMapper(GlobalContext.get().get(), PREVIEW_LOG_WRAPPER)
//    val view = object : BaseMviView<Model, Event>() {}
//    LocalComposables.LocalView(
//        modelMapper.map(State(wifiState = WifiStateProvider.WifiState())),
//        view
//    )
//}
