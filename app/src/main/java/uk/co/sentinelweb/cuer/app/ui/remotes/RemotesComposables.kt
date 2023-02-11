package uk.co.sentinelweb.cuer.app.ui.remotes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.compose.Const.PREVIEW_LOG_WRAPPER
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerMenuItem
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerTopAppBarComposables
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model

object RemotesComposables {

    @Composable
    fun RemotesUi(view: RemotesMviViewProxy) {
        RemotesView(view.observableModel, view.observableLoading, view)
    }

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
                                .padding(top = dimensionResource(R.dimen.page_margin), bottom = 128.dp)
                        ) {
                            // todo content here
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

}


@Preview(name = "Top level")
@Composable
@ExperimentalAnimationApi
private fun BrowsePreview() {
    val modelMapper = RemotesModelMapper(GlobalContext.get().get(), PREVIEW_LOG_WRAPPER)
    val view = object : BaseMviView<Model, Event>() {}
    RemotesComposables.RemotesView(
        modelMapper.map(
            RemotesContract.MviStore.State(
                nodes = listOf()
            )
        ),
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