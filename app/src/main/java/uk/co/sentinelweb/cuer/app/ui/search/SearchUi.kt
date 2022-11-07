package uk.co.sentinelweb.cuer.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.elevation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Companion.PLAYLIST_SELECT_MODEL
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonColors
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonStroke
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain

@Composable
fun SearchView(viewModel: SearchViewModel) {
    SearchParametersUi(
        model = viewModel.model,
        textChange = viewModel::onSearchTextChange,
        localOrRemoteClick = viewModel::switchLocalOrRemote,
        submit = viewModel::onSubmit,
        watchedClick = viewModel::onWatchedClick,
        newClick = viewModel::onNewClick,
        liveClick = viewModel::onLiveClick,
        clearRelatedClick = viewModel::onClearRelated,
        clearDatesClick = viewModel::onClearDates,
        selectDatesClick = viewModel::onSelectDates,
        selectOrderClick = viewModel::onSelectOrder,
        playlistSelect = viewModel::onPlaylistSelect
    )
}

@Composable
fun SearchParametersUi(
    model: SearchContract.Model,
    textChange: (String) -> Unit,
    localOrRemoteClick: () -> Unit,
    watchedClick: (Boolean) -> Unit,
    newClick: (Boolean) -> Unit,
    liveClick: (Boolean) -> Unit,
    playlistSelect: (ChipModel) -> Unit,
    clearRelatedClick: () -> Unit,
    clearDatesClick: () -> Unit,
    selectDatesClick: () -> Unit,
    selectOrderClick: () -> Unit,
    submit: () -> Unit
) {
    CuerTheme {
        Surface {
            Column(
                modifier = Modifier
                    .height(dimensionResource(R.dimen.search_height))
                    .padding(dimensionResource(R.dimen.page_margin))
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row {
                        Icon(
                            painter = painterResource(model.icon),
                            tint = MaterialTheme.colors.onSurface,
                            contentDescription = stringResource(id = R.string.menu_search),
                            modifier = Modifier.padding(12.dp).size(24.dp)
                        )
                        Text(
                            text = model.type + " " + stringResource(id = R.string.search_title),
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier
                                .padding(8.dp)
                        )
                    }
                    Button(
                        onClick = localOrRemoteClick,
                        modifier = Modifier
                            .padding(2.dp)
                            .align(Alignment.TopEnd),
                        border = cuerOutlineButtonStroke(),
                        colors = cuerOutlineButtonColors(),
                        elevation = elevation(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(model.otherIcon),
                            tint = MaterialTheme.colors.onSurface,
                            contentDescription = stringResource(id = R.string.menu_search),
                            modifier = Modifier.padding(end = 4.dp).size(24.dp)
                        )
                        Text(
                            text = model.otherType,
                            style = MaterialTheme.typography.button
                        )
                    }
                }
                Divider()
                SearchTextEntryInput(
                    text = model.text,
                    textChange = textChange,
                    submit = submit
                )
                val modifier = Modifier.weight(1f)
                if (model.isLocal) {
                    SearchLocal(model.localParams, playlistSelect, modifier)
                } else {
                    SearchRemote(
                        model.remoteParams,
                        liveClick,
                        clearRelatedClick,
                        clearDatesClick,
                        selectDatesClick,
                        selectOrderClick,
                        modifier
                    )
                }
                Button(
                    onClick = submit,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.End),
                    border = cuerOutlineButtonStroke(),
                    colors = cuerOutlineButtonColors(),
                    elevation = elevation(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        tint = MaterialTheme.colors.onSurface,
                        contentDescription = stringResource(id = R.string.menu_search),
                        modifier = Modifier.padding(end = 4.dp).size(24.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.search_title),
                        style = MaterialTheme.typography.button
                    )
                }
            }
        }
    }
}

@Composable
fun SearchRemote(
    model: SearchContract.RemoteModel,
    liveClick: (Boolean) -> Unit,
    clearRelatedClick: () -> Unit,
    clearDateRangeClick: () -> Unit,
    selectDatesClick: () -> Unit,
    selectOrderClick: () -> Unit,
    modifier: Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        if (model.relatedTo != null) {
            Row(modifier = Modifier.padding(8.dp)) {
                val spacing = Modifier.padding(horizontal = 4.dp)
                val text = spacing.align(Alignment.CenterVertically)
                Icon(
                    imageVector = Icons.Default.Clear,
                    tint = MaterialTheme.colors.onSurface,
                    contentDescription = stringResource(id = R.string.clear),
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp)
                        .clickable { clearRelatedClick() }
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = "Like ${model.relatedTo}",
                    style = MaterialTheme.typography.body2,
                    modifier = text
                )
            }
        } else {
            Row(modifier = Modifier.padding(8.dp)) {
                val spacing = Modifier.padding(horizontal = 4.dp)
                val text = spacing.align(Alignment.CenterVertically)
                var dateSelectionText = stringResource(id = R.string.search_select_dates)
                if (model.fromDate != null || model.toDate != null) {
                    dateSelectionText = "${model.fromDate} - ${model.toDate}"
                    Icon(
                        imageVector = Icons.Default.Clear,
                        tint = MaterialTheme.colors.onSurface,
                        contentDescription = stringResource(id = R.string.clear),
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .clickable { clearDateRangeClick() }
                            .align(Alignment.CenterVertically)
                    )
                }
                Button(
                    onClick = selectDatesClick,
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(shape = MaterialTheme.shapes.small),
                    border = cuerOutlineButtonStroke(),
                    colors = cuerOutlineButtonColors()
                ) {
                    Text(
                        text = dateSelectionText,
                        style = MaterialTheme.typography.body2,
                        modifier = text
                    )
                }
                Button(
                    onClick = selectOrderClick,
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(shape = MaterialTheme.shapes.small),
                    border = cuerOutlineButtonStroke(),
                    colors = cuerOutlineButtonColors(),
                    elevation = elevation(0.dp)
                ) {
                    Text(
                        text = "Order: ${model.order.name}",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }

            Row(modifier = Modifier.padding(8.dp)) {
                val spacing = Modifier.padding(horizontal = 4.dp)
                val text = spacing.align(Alignment.CenterVertically)
                Checkbox(
                    checked = model.isLive,
                    onCheckedChange = liveClick,
                    modifier = spacing
                )
                Text(
                    text = "Live",
                    style = MaterialTheme.typography.body2,
                    modifier = text
                )
            }
        }
    }
}


@Composable
private fun SearchLocal(
    model: SearchContract.LocalModel,
    playlistSelect: (ChipModel) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
//                    Row(modifier = Modifier.padding(8.dp)) {
//                        val spacing = Modifier.padding(horizontal = 4.dp)
//                        val text = spacing.align(Alignment.CenterVertically)
//                        Checkbox(
//                            checked = model.localParams.isWatched,
//                            onCheckedChange = watchedChange,
//                            modifier = spacing
//                        )
//                        Text(
//                            text = "Watched",
//                            style = MaterialTheme.typography.body2,
//                            modifier = text
//                        )
//                        Checkbox(
//                            checked = model.localParams.isNew,
//                            onCheckedChange = newChange,
//                            modifier = spacing
//                        )
//                        Text(
//                            text = "New",
//                            style = MaterialTheme.typography.body2,
//                            modifier = text
//                        )
//                        Checkbox(
//                            checked = model.localParams.isLive,
//                            onCheckedChange = liveChange,
//                            modifier = spacing
//                        )
//                        Text(
//                            text = "Live",
//                            style = MaterialTheme.typography.body2,
//                            modifier = text
//                        )
//                    }
        Row(
            modifier = Modifier
                .padding(8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            model.playlists.forEach {
                Chip(model = it, onClick = playlistSelect)
            }
        }
    }
}

@Composable
fun Chip(model: ChipModel, onClick: (ChipModel) -> Unit) {
    Button(
        onClick = { onClick(model) },
        modifier = Modifier
            .padding(2.dp)
            .clip(shape = MaterialTheme.shapes.small),
        border = cuerOutlineButtonStroke(),
        colors = cuerOutlineButtonColors(),
        elevation = elevation(0.dp)
    ) {
        if (model.type != ChipModel.Type.PLAYLIST_SELECT) {
            Icon(
                imageVector = Icons.Default.Clear,
                tint = MaterialTheme.colors.onSurface,
                contentDescription = stringResource(id = R.string.clear),
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = model.text,
            style = MaterialTheme.typography.button
        )
    }
}

@Composable
fun SearchTextEntryInput(
    text: String?,
    textChange: (String) -> Unit,
    submit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            modifier
                .padding(top = 16.dp)
        ) {
            SearchInputText(
                text = text,
                onTextChange = textChange,
                onImeAction = submit
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchInputText(
    text: String?,
    onTextChange: (String) -> Unit,
    onImeAction: () -> Unit = {}
) {
    Box {
        val keyboardController = LocalSoftwareKeyboardController.current
        TextField(
            value = text ?: "",
            onValueChange = onTextChange,
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onImeAction()
                keyboardController?.hide()
            }),
            textStyle = MaterialTheme.typography.body1,
            modifier = Modifier.fillMaxWidth()
        )
        if (text.isNullOrEmpty().not()) {
            Icon(
                painterResource(R.drawable.ic_backspace),
                tint = MaterialTheme.colors.onSurface,
                contentDescription = stringResource(id = R.string.clear),
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .padding(12.dp)
                    .clickable { onTextChange("") }
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

val searchParams = SearchContract.Model(
    type = "Local",
    icon = R.drawable.ic_portrait,
    otherType = "YouTube",
    otherIcon = R.drawable.ic_platform_youtube,
    text = "philosophy",
    isLocal = true,
    localParams = SearchContract.LocalModel(
        isWatched = false,
        isLive = false,
        isNew = false,
        playlists = listOf(
            PLAYLIST_SELECT_MODEL,
            ChipModel(PLAYLIST, "philosophy"),
            ChipModel(PLAYLIST, "music"),
            ChipModel(PLAYLIST, "doco"),
        )
    ),
    remoteParams = SearchContract.RemoteModel(
        platform = PlatformDomain.YOUTUBE,
        isLive = false,
        channelPlatformId = null,
        relatedTo = null,
        fromDate = null,
        toDate = null,
        order = SearchRemoteDomain.Order.RELEVANCE
    )
)

@Preview
@Composable
fun PreviewLocalUi() {
    SearchParametersUi(searchParams, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
}

@Preview
@Composable
fun PreviewRemoteUi() {
    SearchParametersUi(searchParams.copy(
        type = "YouTube",
        icon = R.drawable.ic_platform_youtube,
        otherType = "Local",
        otherIcon = R.drawable.ic_portrait,
        isLocal = false
    ), {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
}

@Preview
@Composable
fun PreviewRemoteRelatedlUi() {
    SearchParametersUi(
        searchParams.copy(
            type = "YouTube",
            icon = R.drawable.ic_platform_youtube,
            otherType = "Local",
            otherIcon = R.drawable.ic_portrait,
            text = null,
            isLocal = false,
            remoteParams = searchParams.remoteParams.copy(
                relatedTo = "Is Baurillard the god? [trgkdk34&]"
            )
        ), {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
}

@Preview
@Composable
fun PreviewChipSelect() {
    CuerTheme {
        Chip(PLAYLIST_SELECT_MODEL, { m -> })
    }
}

@Preview
@Composable
fun PreviewChipSelected() {
    CuerTheme {
        Chip(ChipModel(PLAYLIST, "philosophy"), { m -> })
    }
}
