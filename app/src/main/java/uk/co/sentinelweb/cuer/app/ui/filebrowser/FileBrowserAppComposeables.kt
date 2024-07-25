package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerMenuItem
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerTopAppBarComposables
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FileBrowserContract.AppFilesUiModel

object FileBrowserAppComposeables {

    // todo use scaffold
    @Composable
    fun FileBrowserAppWrapperUi(appModelObservable: Flow<AppFilesUiModel>, viewModel: FileBrowserViewModel) {
        val appFileUiState = appModelObservable.collectAsState(initial = AppFilesUiModel(loading = false))
        CuerTheme {
            Surface {
                Box(contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 60.dp)
                    ) {
                        Box(contentAlignment = Alignment.TopStart) {
                            CuerTopAppBarComposables.CuerAppBar(
                                text = "Files",
                                backgroundColor = MaterialTheme.colors.primary,
                                onUp = { viewModel.onUpClick() },
                                actions = listOf(
                                    Action(CuerMenuItem.Help, { }),
                                    Action(CuerMenuItem.Settings, { }),
                                ),
                                modifier = Modifier
                                    .height(56.dp)
                            )
                        }
                        FilesComposeables.FilesUi(interactions = viewModel)
                    }
                    if (appFileUiState.value.loading) {
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
