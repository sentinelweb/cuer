package uk.co.sentinelweb.cuer.app.ui.common.dialog.appselect

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.imageloading.rememberDrawablePainter
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme

object AppSelectComposables {
    const val COLS = 3
    @Composable
    fun AppSelectView(apps: List<AppDetails>, onClick: (AppDetails) -> Unit) {
        CuerTheme {
            Surface {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(COLS),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(apps.size) { index ->
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { onClick(apps[index]) }
                        ) {
                            Column() {
                                Icon(
                                    painter = rememberDrawablePainter(drawable = apps[index].icon),
                                    contentDescription = apps[index].title.toString(),
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(48.dp)
                                        .align(CenterHorizontally)
                                )
                                Text(
                                    text = apps[index].title.toString(),
                                    style = MaterialTheme.typography.body1,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .wrapContentWidth()
                                        .align(CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
