package uk.co.sentinelweb.cuer.hub.ui.remotes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import uk.co.sentinelweb.cuer.hub.ui.common.image.ImageFromUrl

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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(model.title) },
                    actions = {
                        IconButton(onClick = { view.dispatch(RemotesContract.View.Event.OnActionSettingsClicked) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
            content = {
                Header(model, view)
            }
        )
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
    }
}