package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.painterResource
import uk.co.sentinelweb.cuer.app.ui.common.compose.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerMenuItem
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedAppBarComposables.CuerSharedAppBar
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.AppFilesUiModel
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.shared.generated.resources.*

object FilesComposeables {

    // todo use scaffold
    @Composable
    fun FileBrowserAppWrapperUi(appModelObservable: Flow<AppFilesUiModel>, viewModel: FilesViewModel) {
        val appFileUiState = appModelObservable.collectAsState(initial = AppFilesUiModel.Initial)
        CuerSharedTheme {
            Surface {
                Box(contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 60.dp)
                    ) {
                        Box(contentAlignment = Alignment.TopStart) {
                            CuerSharedAppBar(
                                title = "Files",
                                subTitle = appFileUiState.value.subTitle,
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                onUp = { viewModel.onUpClick() },
                                actions = listOf(
                                    Action(CuerMenuItem.Help, { }),
                                    Action(CuerMenuItem.Settings, { }),
                                ),
                                modifier = Modifier
                                    .height(56.dp)
                            )
                        }
//                        SharedThemeView()
                        FilesUi(interactions = viewModel)
                    }
                    if (appFileUiState.value.loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
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
    fun FilesUi(interactions: FilesContract.Interactions) {
        val state = interactions.modelObservable.collectAsState(initial = FilesModel.blankModel())
        FilesView(state.value, interactions)
    }

    @Composable
    private fun FilesView(model: FilesModel, view: FilesContract.Interactions) {
        Column(
            modifier = Modifier.padding(8.dp)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .fillMaxHeight()
        ) {
            model.list.children.forEach {
                Row(modifier = Modifier.clickable { view.onClickFolder(it) }) {
                    val icon = if (it.title.equals("..")) Res.drawable.ic_up else Res.drawable.ic_folder
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = it.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp))
                }
            }
            model.list.playlist.items.forEach {
                Row(modifier = Modifier.clickable {
                    view.onClickFile(it)
                }) {
                    val icon = when (it.media.mediaType) {
                        VIDEO -> Res.drawable.ic_video
                        AUDIO -> Res.drawable.ic_mic
                        WEB -> Res.drawable.ic_browse // not valid here
                        FILE -> Res.drawable.ic_file
                    }
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(text = it.media.title ?: "No title",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
