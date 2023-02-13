package uk.co.sentinelweb.cuer.app.ui.local

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.google.accompanist.glide.rememberGlidePainter
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.app.ui.common.compose.Const.PREVIEW_LOG_WRAPPER
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerTopAppBarComposables.CuerAppBar
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore.State
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

object LocalComposables {
    val log: LogWrapper by lazy { GlobalContext.get().get<LogWrapper>().apply { tag(this) } }

    @Composable
    fun RemotesUi(view: LocalMviViewProxy) {
        RemotesView(view.observableModel, view.observableLoading, view)
    }

    // todo use scaffold
    @Composable
    fun RemotesView(model: Model, loading: Boolean, view: BaseMviView<Model, Event>) {
        CuerTheme {
            Surface {
                Box(contentAlignment = Alignment.TopStart) {
                    CuerAppBar(
                        text = model.title,
                        backgroundColor = MaterialTheme.colors.primary,
                        onUp = { view.dispatch(Event.OnUpClicked) },
//                        actions = listOf(
//                            Action(CuerMenuItem.Help,
//                                { view.dispatch(OnActionHelpClicked) }),
//                            Action(CuerMenuItem.Search, { view.dispatch(OnActionSearchClicked) }),
//                            Action(CuerMenuItem.PasteAdd, { view.dispatch(OnActionPasteAdd) }),
//                            Action(CuerMenuItem.Settings, { view.dispatch(OnActionSettingsClicked) }),
//                        ),
                        modifier = Modifier
                            .height(56.dp)
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
                        var hostnameText by rememberSaveable { mutableStateOf(model.localNode?.hostname ?: "") }

                        TextField(
                            value = hostnameText,
                            onValueChange = { hostnameText = it },
                            label = { Text("Hostname") }
                        )
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
    val modelMapper = LocalModelMapper(GlobalContext.get().get(), PREVIEW_LOG_WRAPPER)
    val view = object : BaseMviView<Model, Event>() {}
    LocalComposables.RemotesView(
        modelMapper.map(State()),
        false,
        view
    )
}