package uk.co.sentinelweb.cuer.app.ui.local

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
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
                        //var localNode by rememberSaveable(saver = stateSaverLocalDomain()) { mutableStateOf(model.localNodeDomain) }
                        var localNode by remember { mutableStateOf(model.localNodeDomain) }
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
                        var ddExpanded by remember { mutableStateOf(false) }
                        val authType = localNode.authType
                        HeaderButton(authType::class.simpleName!!, icon = R.drawable.ic_login) { ddExpanded = true }
                        DropdownMenu(
                            expanded = ddExpanded,
                            onDismissRequest = { ddExpanded = false },
                        ) {
                            DropdownMenuItem(onClick = {
                                localNode = localNode.copy(authType = Open)
                                ddExpanded = false
                            }) { Text(text = "Open") }

                            DropdownMenuItem(onClick = {
                                localNode = localNode.copy(authType = Confirm)
                                ddExpanded = false
                            }) { Text(text = "Confirm") }

                            DropdownMenuItem(onClick = {
                                localNode = localNode.copy(authType = Username("", ""))
                                ddExpanded = false
                            }) { Text(text = "Password") }
                        }
                        when (authType) {
                            Open -> Unit
                            Confirm -> Unit
                            is Username -> {

                                TextField(
                                    value = (localNode.authType as Username).username,
                                    onValueChange = { localNode = localNode.copy(authType = (localNode.authType as Username).copy(username = it)) },
                                    label = { Text("Username") },
                                    modifier = Modifier.padding(16.dp)
                                )

                                TextField(
                                    value = (localNode.authType as Username).password,
                                    onValueChange = { localNode = localNode.copy(authType = (localNode.authType as Username).copy(password = it)) },
                                    label = { Text("Password") },
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun stateSaverLocalDomain() = Saver<MutableState<LocalNodeDomain>, String>(
        save = { state -> state.value.serialise() },
        restore = { value ->
            @Suppress("UNCHECKED_CAST")
            mutableStateOf((deserialiseLocalNode(value as String)))
        }
    )

}


@Preview(name = "Top level")
@Composable
@ExperimentalAnimationApi
private fun RemotesPreview() {
    val modelMapper = LocalModelMapper(GlobalContext.get().get(), PREVIEW_LOG_WRAPPER)
    val view = object : BaseMviView<Model, Event>() {}
    LocalComposables.RemotesView(
        modelMapper.map(State()),
        view
    )
}