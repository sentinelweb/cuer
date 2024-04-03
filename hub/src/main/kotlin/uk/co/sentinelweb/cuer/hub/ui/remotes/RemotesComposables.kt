package uk.co.sentinelweb.cuer.hub.ui.remotes

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesModelMapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

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
            }
        )
    }
}