package uk.co.sentinelweb.cuer.app.ui.search.image

//import com.google.accompanist.glide.rememberGlidePainter
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.material.Divider
//import androidx.compose.material.Surface
//import androidx.compose.material.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
//import uk.co.sentinelweb.cuer.app.ui.search.SearchInputText


//@Composable
//fun SearchImageView(viewModel: SearchImageViewModel) {
//    SearchImageParametersUi(
//        model = viewModel.model,
//        textChange = viewModel::onSearchTextChange,
//        onSearch = viewModel::onSearch,
//        onSelectImage = viewModel::onImageSelected
//    )
//}
//
//@Composable
//fun SearchImageParametersUi(
//    model: SearchImageContract.Model,
//    textChange: (String) -> Unit,
//    onSearch: () -> Unit,
//    onSelectImage: (Int) -> Unit
//) {
//    CuerTheme {
//        Surface {
//            Column {
//                LazyColumn(
//                    modifier = Modifier.weight(1f),
//                    contentPadding = PaddingValues(top = 4.dp)
//                ) {
//                    itemsIndexed(model.images) { index, imageData ->
//                        Image(
//                            painter = rememberGlidePainter(
//                                request = imageData.url,
//                                fadeIn = true
//                            ),
//                            contentDescription = "",
//                            modifier = Modifier
//                                .padding(2.dp)
//                                .clickable { onSelectImage(index) },
//                            contentScale = ContentScale.Fit
//                        )
//                        Text(text = imageData.url)
//                    }
//                }
//                Divider()
//                SearchInputText(model.term, textChange, onSearch)
//            }
//        }
//    }
//}
