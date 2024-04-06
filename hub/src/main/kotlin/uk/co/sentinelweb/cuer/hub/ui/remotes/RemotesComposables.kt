package uk.co.sentinelweb.cuer.hub.ui.remotes

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesModelMapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.hub.ui.common.button.HeaderButton
import uk.co.sentinelweb.cuer.hub.ui.common.image.ImageFromUrl
import uk.co.sentinelweb.cuer.remote.server.ServerState

object RemotesComposables {
    val log: LogWrapper by lazy { GlobalContext.get().get<LogWrapper>().apply { tag(this) } }

    @Composable
    fun RemotesUi(coordinator: RemotesUiCoordinator) {
        val state = remember { mutableStateOf(RemotesModelMapper.blankModel()) }
        coordinator.observeModel { newState -> state.value = newState }
        RemotesView(state.value, coordinator)
    }

    @Composable
    fun RemotesView(
        model: RemotesContract.View.Model,
        view: BaseMviView<RemotesContract.View.Model, RemotesContract.View.Event>
    ) {
        log.d("RemotesView")
        Column {
            Header(model, view)
        }
    }

    @Composable
    private fun Header(
        model: RemotesContract.View.Model,
        view: BaseMviView<RemotesContract.View.Model, RemotesContract.View.Event>
    ) {
        Box(modifier = Modifier.height(160.dp)) {
            model.imageUrl
                ?.also { url ->
                    ImageFromUrl(url)
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
                ) { view.dispatch(RemotesContract.View.Event.OnActionStartServerClicked) }

                ServerState.STARTED -> HeaderButton("Stop") { view.dispatch(RemotesContract.View.Event.OnActionStopServerClicked) }
            }
            if (model.serverState == ServerState.STARTED) {
                HeaderButton("Ping") { view.dispatch(RemotesContract.View.Event.OnActionPingMulticastClicked) }
            }
            HeaderButton("Config") { view.dispatch(RemotesContract.View.Event.OnActionConfigClicked) }
        }
        model.address?.also {
            Text(
                text = it,
                style = MaterialTheme.typography.h3,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 8.dp,
                    bottom = 0.dp
                )
            )
        }
    }
}