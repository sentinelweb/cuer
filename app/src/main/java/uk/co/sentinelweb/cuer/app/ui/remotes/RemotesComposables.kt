package uk.co.sentinelweb.cuer.app.ui.remotes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.google.accompanist.glide.rememberGlidePainter
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.compose.Const.PREVIEW_LOG_WRAPPER
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonColors
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonStroke
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerMenuItem
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerTopAppBarComposables
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.ServerState.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

object RemotesComposables {
    val log: LogWrapper by lazy { GlobalContext.get().get<LogWrapper>().apply { tag(RemotesComposables@ this) } }

    @Composable
    fun RemotesUi(view: RemotesMviViewProxy) {
        RemotesView(view.observableModel, view.observableLoading, view)
    }

    // todo use scaffold
    @Composable
    fun RemotesView(model: Model, loading: Boolean, view: BaseMviView<Model, Event>) {
        CuerTheme {
            Surface {
                Box(contentAlignment = Alignment.Center) {
                    Column {
                        CuerTopAppBarComposables.CuerAppBar(
                            text = model.title,
                            onUp = { view.dispatch(OnUpClicked) },
                            actions = listOf(
                                Action(CuerMenuItem.Help,
                                    { view.dispatch(OnActionHelpClicked) }),
                                Action(CuerMenuItem.Search, { view.dispatch(OnActionSearchClicked) }),
                                Action(CuerMenuItem.PasteAdd, { view.dispatch(OnActionPasteAdd) }),
                                Action(CuerMenuItem.Settings, { view.dispatch(OnActionSettingsClicked) }),
                            )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colors.surface)
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 128.dp)
                        ) {

                            Row(
                                modifier = Modifier
                                    .height(160.dp)
                            ) {
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
                            Row(
                                modifier = Modifier.padding(
                                    start = dimensionResource(R.dimen.app_bar_header_margin_start),
                                    top = 16.dp,
                                )
                            ) {
                                when (model.serverState) {
                                    STOPPED, INITIAL -> HeaderButton("Start", R.drawable.ic_play) { view.dispatch(OnActionStartServerClicked) }
                                    STARTED -> HeaderButton("Stop", R.drawable.ic_stop) { view.dispatch(OnActionStopServerClicked) }
                                }
                                if (model.serverState == STARTED) {
                                    HeaderButton("Ping", R.drawable.ic_ping) { view.dispatch(OnActionPingClicked) }
                                }
                                HeaderButton("Config", R.drawable.ic_menu_settings) { OnActionConfigClicked }
                            }
                            model.address?.also {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.h3,
                                    modifier = Modifier.padding(
                                        start = dimensionResource(R.dimen.app_bar_header_margin_start),
                                        top = 16.dp,
                                        bottom = 16.dp
                                    )
                                )
                            }
                            LazyColumn(
                                modifier = Modifier.height(300.dp),//fillMaxHeight(0.5f),
                                contentPadding = PaddingValues(top = 4.dp)
                            ) {
                                items(model.remoteNodes) { remote ->

                                    RemoteRow(remote)
                                }
                            }
                        }
                    }
                    if (loading) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.primary),
                            strokeWidth = 8.dp,
                            modifier = Modifier
                                .width(64.dp)
                                .height(64.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RemoteRow(remote: RemotesContract.View.NodeModel) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 8.dp
                )
                .background(MaterialTheme.colors.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_wifi_tethering),
                tint = MaterialTheme.colors.onSurface,
                contentDescription = null,
                modifier = Modifier.size(48.dp).padding(4.dp)
            )
            Column { // todo use textview
                Text(
                    text = remote.device,
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = remote.address,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

    }

    @Composable
    private fun HeaderButton(text: String, icon: Int, action: () -> Unit) {
        Button(
            onClick = { action() },
            modifier = Modifier
                .padding(end = 16.dp),
            border = cuerOutlineButtonStroke(),
            colors = cuerOutlineButtonColors(),
            elevation = ButtonDefaults.elevation(0.dp),
        ) {
            Icon(
                painter = painterResource(icon),
                tint = MaterialTheme.colors.onSurface,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.button,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

}


@Preview(name = "Top level")
@Composable
@ExperimentalAnimationApi
private fun BrowsePreview() {
    val modelMapper = RemotesModelMapper(GlobalContext.get().get(), PREVIEW_LOG_WRAPPER)
    val view = object : BaseMviView<Model, Event>() {}
    RemotesComposables.RemotesView(
        modelMapper.map(RemotesContract.MviStore.State()),
        false,
        view
    )
}
//
//@Preview(name = "chip")
//@Composable
//@ExperimentalAnimationApi
//private fun ChipPreview() {
//    val model = CategoryModel(
//        id = 1,
//        title = "title",
//        description = "null",
//        thumbNailUrl = "https://cuer-275020.web.app/images/headers/Socrates.jpg",
//        existingPlaylist = null,
//        forceItem = false,
//        isPlaylist = false,
//        subCategories = emptyList(),
//        subCount = 4
//    )
//    val view = object : BaseMviView<Model, Event>() {}
//    BrowseComposables.CatChip(model, 1, view)
//}