package uk.co.sentinelweb.cuer.app.ui.browse

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event.CategoryClicked
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.util.wrapper.log.AndroidLogWrapper
import kotlin.math.max

@Composable
fun BrowseUi(view: BrowseMviView) {
    val model = remember { view.observableModel }
    BrowseView(model, view)
}

@Composable
private fun BrowseView(model: BrowseContract.View.Model, view: BrowseMviView) {
    CuerTheme {
        Surface {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.page_margin))
            ) {
                Log.d("BrowseView", model.categories.toString())
                model.categories.forEach {
                    Category(it, view)
                }
            }
        }
    }
}

@Composable
private fun Category(model: BrowseContract.View.CategoryModel, view: BrowseMviView) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = model.title,
            style = MaterialTheme.typography.h4
        )
        model.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.body1
            )
        }
        Divider()
        StaggeredGrid(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
        ) {
            model.subCategories.forEach {
                SubCategory(it, view)
            }
        }
    }
}

@Composable
fun SubCategory(
    subCategory: BrowseContract.View.CategoryModel,
    view: BrowseMviView,
) {
//    val (selected, onSelected) = remember { mutableStateOf(false) }
    // val topicChipTransitionState = topicChipTransition(selected)

    Surface(
        modifier = Modifier.padding(4.dp),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
//            .copy(
//            topStart = CornerSize(
//                topicChipTransitionState.cornerRadius
//            )
//        )
    ) {
        Row(modifier = Modifier
            .clickable(onClick = { view.dispatch(CategoryClicked(subCategory.id)) })) {
            Box {
                subCategory.thumbNailUrl?.let {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp)
                    )

//                    Image(
//                        painter = rememberGlidePainter(
//                            request = subCategory.thumbNailUrl,
//                            previewPlaceholder = R.drawable.ic_image_placeholder
//                        ),
//                        contentDescription = subCategory.title,
//                        modifier = Modifier
//                            .size(width = 72.dp, height = 72.dp)
//                            .aspectRatio(1f)
//                    )
                } ?: Icon(
                    painter = painterResource(R.drawable.ic_baseline_category_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .padding(start = 24.dp)
                        .align(Alignment.Center)
                )
            }
            Column {
                Text(
                    text = subCategory.title,
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
                        Icon(
                            painter = painterResource(R.drawable.ic_platform_youtube_24_black),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(12.dp)
                        )
                        Text(
                            text = subCategory.videoCount.toString(),
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(start = 8.dp)
                        )
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


@Preview(name = "Top level")
@Composable
private fun BrowsePreview() {
    BrowseView(BrowseModelMapper(AndroidLogWrapper()).map(BrowseTestData.state), BrowseMviView(AndroidLogWrapper()))
}