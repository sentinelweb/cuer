package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import uk.co.sentinelweb.cuer.app.ui.common.compose.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerMenuItem.*
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedAppBarComposables.CuerSharedAppBar
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.colorTransparentYellow
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.ListItemType.*
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Model.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Alpha
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Time
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.shared.generated.resources.*

object FilesComposeables {

    // todo use scaffold
    @Composable
    fun FileBrowserAppUi(modelObservable: Flow<FilesContract.Model>, viewModel: FilesContract.ViewModel) {
        val model = modelObservable.collectAsState(initial = Initial)
        CuerSharedTheme {
            Surface {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 60.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.TopStart,
                        modifier = Modifier
                            .height(60.dp)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.header_files_smoke),
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxSize()
                                .height(60.dp)
                                .wrapContentHeight(),
                            contentScale = ContentScale.Crop
                        )
                        CuerSharedAppBar(
                            title = stringResource(Res.string.files_title),
                            subTitle = model.value.nodeName + (model.value.filePath ?: ""),
                            contentColor = Color.White,
                            backgroundColor = colorTransparentYellow,
                            onUp = { viewModel.onUpClick() },
                            actions = listOf(
                                Action(SortAlpha, { viewModel.onSort(Alpha) }),
                                Action(SortTime, { viewModel.onSort(Time) }),
                                Action(item = Reload, action = { viewModel.onRefreshClick() }),
                            ),
                            modifier = Modifier.fillMaxSize().align(Alignment.CenterStart)
                        )
                    }
                    FilesView(model = model.value, viewModel = viewModel)
                }
            }
        }
    }

    @Composable
    fun FileBrowserDesktopUi(viewModel: FilesContract.ViewModel) {
        val model = viewModel.modelObservable.collectAsState(initial = Initial)
        CuerSharedTheme {
            Surface {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.TopStart,
                        modifier = Modifier
                            .height(60.dp)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.header_files_smoke),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight(),
                        )
                        CuerSharedAppBar(
                            title = stringResource(Res.string.files_title) + ": " + (model.value.nodeName ?: "No host"),
                            subTitle = model.value.filePath,
                            backgroundColor = colorTransparentYellow,
                            contentColor = Color.White,
                            onUp = null,
                            actions = listOf(
                                Action(item = SortAlpha, action = { viewModel.onSort(Alpha) }),
                                Action(item = SortTime, action = { viewModel.onSort(Time) }),
                                Action(item = Reload, action = { viewModel.onRefreshClick() }),
                            ),
                            modifier = Modifier
                        )
                    }
                    FilesView(model = model.value, viewModel = viewModel)
                }
            }
        }
    }

    @Composable
    private fun FilesView(model: FilesContract.Model, viewModel: FilesContract.ViewModel) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.TopStart)
            ) {
                model.upListItem?.also { ListRow(viewModel, it.first, it.second) }
                model.list?.forEach {
                    ListRow(viewModel, it.first, it.second)
                }
            }
            if (model.loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }

    @Composable
    private fun ListRow(
        viewModel: FilesContract.ViewModel,
        listItem: FilesContract.ListItem,
        domain: Domain
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    when (domain) {
                        is PlaylistItemDomain -> viewModel.onClickFile(domain)
                        is PlaylistDomain -> viewModel.onClickFolder(domain)
                        else -> Unit
                    }
                }
                .fillMaxWidth()
        ) {
            val icon = when (listItem.type) {
                VIDEO -> Res.drawable.ic_video
                AUDIO -> Res.drawable.ic_mic
                WEB -> Res.drawable.ic_browse // not valid here
                FILE -> Res.drawable.ic_file
                FOLDER -> Res.drawable.ic_folder
                UP -> Res.drawable.ic_up
            }
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(16.dp)
            )
            Column(
                Modifier.fillMaxWidth()
                    .padding(8.dp)
                    .align(CenterVertically)
            ) {
                Text(
                    text = "${listItem.title} ${listItem.season ?: ""} ${listItem.ext ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(4.dp)
                )
                if (listItem.timeSince != null) {
                    Text(
                        text = listItem.timeSince,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}
