package uk.co.sentinelweb.cuer.app.ui.search.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
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
        model = viewModel.modelState,
        textChange = viewModel::onSearchTextChange,
        onSearch = viewModel::onSearch,
        onSelectImage = viewModel::onImageSelected
    )
}

@Composable
fun SearchImageParametersUi(
    model: SearchImageContract.Model,
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
                    items(model.images) { imageData ->
                        Image(
                            painter = rememberGlidePainter(
                                request = imageData.url,
                                fadeIn = true
                            ),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(2.dp)
                                .clickable { onSelectImage(imageData) }
                                .fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Divider()
                Box {
                    SearchInputText(model.term, textChange, onSearch)

                }
            }
        }
    }
}
