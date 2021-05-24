package uk.co.sentinelweb.cuer.app.ui.search.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.accompanist.glide.rememberGlidePainter
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.search.SearchInputText
import uk.co.sentinelweb.cuer.domain.ImageDomain


@Composable
fun SearchImageView(viewModel: SearchImageViewModel) {
    SearchImageParametersUi(
        search = viewModel.searchState,
        results = viewModel.resultsState,
        textChange = viewModel::onSearchTextChange,
        onSearch = viewModel::onSearch,
        onSelectImage = viewModel::onImageSelected
    )
}

@Composable
fun SearchImageParametersUi(
    search: SearchImageContract.SearchModel,
    results: SearchImageContract.ResultsModel,
    textChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectImage: (ImageDomain) -> Unit
) {
    CuerTheme {
        Surface {
            Column {
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
