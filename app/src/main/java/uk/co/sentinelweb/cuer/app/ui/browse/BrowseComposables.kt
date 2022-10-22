package uk.co.sentinelweb.cuer.app.ui.browse

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.Order.A_TO_Z
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.Order.CATEGORIES
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.*
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event.OnCategoryClicked
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerBrowseTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.image.NetworkImage
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerMenuItem
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerTopAppBarComposables
import uk.co.sentinelweb.cuer.app.util.wrapper.log.AndroidLogWrapper
import kotlin.math.max

object BrowseComposables {

    //private val log: LogWrapper = getKoin().get()

//    init {
//        log.tag(this)
//    }

    @Composable
    fun BrowseUi(view: BrowseMviView) {
        BrowseView(view.observableModel, view)
    }

    @Composable
    fun BrowseView(model: Model, view: BaseMviView<Model, Event>) {
        CuerBrowseTheme {
            Surface {
                Column {
                    CuerTopAppBarComposables.CuerAppBar(
                        text = model.title,
                        onUp = { view.dispatch(Event.OnUpClicked) },
                        //backgroundColor = Color.White// todo dark theme make color?
                        actions = listOf(
                            Action(CuerMenuItem.Search,
                                { view.dispatch(Event.OnActionSearchClicked) }),
                            when (model.order) {
                                CATEGORIES -> Action(CuerMenuItem.SortAlpha,
                                    { view.dispatch(Event.OnSetOrder(A_TO_Z)) }
                                )
                                A_TO_Z -> Action(CuerMenuItem.SortCategory,
                                    { view.dispatch(Event.OnSetOrder(CATEGORIES)) }
                                )
                            },
                            Action(CuerMenuItem.Settings,
                                { view.dispatch(Event.OnActionSettingsClicked) }),
                        )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.secondaryVariant)
                            .verticalScroll(rememberScrollState())
                            .padding(top = dimensionResource(R.dimen.page_margin), bottom = 128.dp)
                    ) {
                        if (model.isRoot) {
                            if (model.recent != null) {
                                CategoryWithTitle(model.recent!!, view, 1)
                            }
                            model.categories.forEach {
                                CategoryWithTitle(it, view, 3)
                            }
                        } else {
                            CategoryGrid(8, model.categories, view)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CategoryWithTitle(model: CategoryModel, view: BaseMviView<Model, Event>, rows: Int) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = model.title,
                style = MaterialTheme.typography.h4,
                color = Color.White,
                modifier = Modifier
                    .padding(start = dimensionResource(R.dimen.page_margin))
                    .clickable(onClick = { view.dispatch(OnCategoryClicked(model)) })

            )
            model.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body1,
                    color = Color.White
                )
            }
            Divider(
                color = Color.White, modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(start = dimensionResource(R.dimen.page_margin))
            )
            CategoryGrid(rows, model.subCategories, view)
        }
    }

    @Composable
    private fun CategoryGrid(
        rows: Int,
        list: List<CategoryModel>,
        view: BaseMviView<Model, Event>,
    ) {
        StaggeredGrid(
            rows = rows,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = dimensionResource(R.dimen.page_margin))
                .padding(bottom = dimensionResource(R.dimen.page_margin))
        ) {
            list.forEach {
                CatChip(it, view)
            }
        }
    }

    @Composable
    fun CatChip(
        category: CategoryModel,
        view: BaseMviView<Model, Event>,
    ) {
        Surface(
            modifier = Modifier.padding(4.dp),
            elevation = 4.dp,
            shape = MaterialTheme.shapes.medium
        ) {

            Row(
                modifier = Modifier
                    .clickable(onClick = { view.dispatch(OnCategoryClicked(category)) })
            ) {
                Box {
                    category.thumbNailUrl?.let {
                        NetworkImage(
                            url = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(width = 72.dp, height = 72.dp)
                                .aspectRatio(1f)
                        )
                    } ?: Icon(
                        painter = painterResource(R.drawable.ic_category),
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .padding(start = 24.dp)
                            .align(Alignment.Center)
                    )
                }
                Column {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            if (category.subCount > 0) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_tree_24),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .size(16.dp)
                                )
                                Text(
                                    text = category.subCount.toString(),
                                    style = MaterialTheme.typography.caption,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            } else if (category.isPlaylist) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_playlist_black),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .size(16.dp)
                                )
                                if (category.existingPlaylist != null) {
                                    Text(
                                        text = category.existingPlaylist!!.currentIndex.toString(),
                                        style = MaterialTheme.typography.caption,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    // taken from OWL
    @Composable
    private fun StaggeredGrid(
        modifier: Modifier = Modifier,
        rows: Int = 3,
        content: @Composable () -> Unit,
    ) {
        Layout(
            content = content,
            modifier = modifier
        ) { measurables, constraints ->
            val rowWidths = IntArray(rows) { 0 } // Keep track of the width of each row
            val rowHeights = IntArray(rows) { 0 } // Keep track of the height of each row

            // Don't constrain child views further, measure them with given constraints
            val placeables = measurables.mapIndexed { index, measurable ->
                val placeable = measurable.measure(constraints)

                // Track the width and max height of each row
                val row = index % rows
                rowWidths[row] += placeable.width
                rowHeights[row] = max(rowHeights[row], placeable.height)

                placeable
            }

            // Grid's width is the widest row
            val width = rowWidths.maxOrNull()?.coerceIn(constraints.minWidth, constraints.maxWidth)
                ?: constraints.minWidth
            // Grid's height is the sum of each row
            val height = rowHeights.sum().coerceIn(constraints.minHeight, constraints.maxHeight)

            // y co-ord of each row
            val rowY = IntArray(rows) { 0 }
            for (i in 1 until rows) {
                rowY[i] = rowY[i - 1] + rowHeights[i - 1]
            }
            layout(width, height) {
                // x co-ord we have placed up to, per row
                val rowX = IntArray(rows) { 0 }
                placeables.forEachIndexed { index, placeable ->
                    val row = index % rows
                    placeable.place(
                        x = rowX[row],
                        y = rowY[row]
                    )
                    rowX[row] += placeable.width
                }
            }
        }
    }

}

class TestStrings : BrowseContract.Strings {
    override val allCatsTitle = "All cats"
    override val recent = "Recent"
    override val errorNoPlaylistConfigured = "Error no playlist"
    override fun errorNoCatWithID(id: Long) = "Error no cat"
}

@Preview(name = "Top level")
@Composable
private fun BrowsePreview() {
    val browseModelMapper = BrowseModelMapper(TestStrings(), AndroidLogWrapper())
    val view = object : BaseMviView<Model, Event>() {}
    BrowseComposables.BrowseView(
        browseModelMapper.map(BrowseTestData.previewState),
        view
    )
}