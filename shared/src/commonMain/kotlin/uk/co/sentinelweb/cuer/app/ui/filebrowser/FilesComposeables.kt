package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import uk.co.sentinelweb.cuer.app.ui.common.compose.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerMenuItem.*
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedAppBarComposables.CuerSharedAppBar
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.CustomSnackbar
import uk.co.sentinelweb.cuer.app.ui.common.compose.colorTransparentYellow
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Label
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Label.None
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.ListItemType.*
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Model.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Alpha
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Time
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.shared.generated.resources.*

object FilesComposeables {

    // todo use scaffold - move up to app
    @Composable
    fun FileBrowserAppUi(viewModel: FilesContract.ViewModel) {
        val model = viewModel.modelObservable.collectAsState(initial = Initial)
        val snackbarHostState = remember { SnackbarHostState() }
        val label = viewModel.labels.collectAsState(initial = None)

        LaunchedEffect(label.value) {
            when (label.value) {
                is Label.ErrorMessage -> snackbarHostState.showSnackbar(
                    message = (label.value as Label.ErrorMessage).message,
                    actionLabel = "DISMISS",
                )

                else -> Unit
            }
        }
        CuerSharedTheme {
            Scaffold(
                topBar = {
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
                            overflowActions = listOf(
                                Action(Help, { }),
                                Action(Settings, { }),
                            ),
                            modifier = Modifier.fillMaxSize().align(Alignment.CenterStart)
                        )
                    }
                },
                snackbarHost = {
                    SnackbarHost(snackbarHostState, modifier = Modifier.padding(8.dp, bottom = 80.dp)) { data ->
                        CustomSnackbar(snackbarData = data)
                    }
                },

                ) { padding ->
                Box(modifier = Modifier.padding(top=padding.calculateTopPadding(), bottom = 68.dp)) {
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
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.header_files_smoke),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
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

    @OptIn(ExperimentalLayoutApi::class)
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
                    text = listItem.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(4.dp)
                )

                if (listItem.timeSince != null) {
                    Row(Modifier.fillMaxWidth()) {
                        val text = "${listItem.season ?: ""} ${listItem.ext ?: ""}".trim()
                        if (text.isNotBlank()) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically)
                            )
                        }
                        Text(
                            text = listItem.timeSince,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(2.dp).align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}
