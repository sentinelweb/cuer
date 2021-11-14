package uk.co.sentinelweb.cuer.app.ui.search.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.glide.rememberGlidePainter
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.search.Chip
import uk.co.sentinelweb.cuer.app.ui.search.SearchInputText
import uk.co.sentinelweb.cuer.domain.ImageDomain


@Composable
fun SearchImageView(viewModel: SearchImageViewModel) {
    SearchImageParametersUi(
        search = viewModel.searchState,
        results = viewModel.resultsState,
        textChange = viewModel::onSearchTextChange,
        onSearch = viewModel::onSearch,
        onSelectImage = viewModel::onImageSelected,
        onLibraryClick = viewModel::onLibraryClick,
        onClose = viewModel::onClose
    )
}

@Composable
fun SearchImageParametersUi(
    search: SearchImageContract.SearchModel,
    results: SearchImageContract.ResultsModel,
    textChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectImage: (ImageDomain) -> Unit,
    onLibraryClick: () -> Unit,
    onClose: () -> Unit
) {
    CuerTheme {
        Surface {
            Column {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Search Images",
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart)
                    )
                    Row(modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(
                            painterResource(id = R.drawable.ic_photo_library_24),
                            contentDescription = stringResource(id = R.string.clear),
                            modifier = Modifier
                                .width(48.dp)
                                .clickable { onLibraryClick() }
                                .padding(12.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(id = R.string.clear),
                            modifier = Modifier
                                .width(48.dp)
                                .clickable { onClose() }
                                .padding(12.dp)
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.6f),
                    contentPadding = PaddingValues(top = 4.dp)
                ) {
                    items(results.images) { imageData ->
                        Image(
                            painter = rememberGlidePainter(
                                request = imageData.url,
                                fadeIn = true
                            ),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(2.dp)
                                .clickable { onSelectImage(imageData) }
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
                Divider()
                Box {
                    SearchInputText(search.term, textChange, onSearch)
                    if (search.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 48.dp)
                                .align(Alignment.CenterEnd)
                                .width(36.dp)
                                .height(36.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun SearchWindow() {
    CuerTheme {
        SearchImageParametersUi(
            SearchImageContract.SearchModel(term = "Search Term", loading = false),
            SearchImageContract.ResultsModel(listOf()),
            {}, {}, {}, {}, {}
        )
    }
}