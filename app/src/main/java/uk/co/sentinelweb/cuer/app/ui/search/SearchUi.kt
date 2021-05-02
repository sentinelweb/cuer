package uk.co.sentinelweb.cuer.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Companion.PLAYLIST_SELECT_MODEL
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.domain.PlatformDomain

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
    submit: () -> Unit
) {

    CuerTheme {
        Surface {
            Column(
                modifier = Modifier
                    .height(dimensionResource(R.dimen.search_height))
                    .padding(dimensionResource(R.dimen.page_margin))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = model.type + " " + stringResource(id = R.string.search_title),
                        style = MaterialTheme.typography.h4
                    )
                    Button(
                        onClick = localOrRemoteClick,
                        modifier = Modifier
                            .padding(2.dp)
                            .align(Alignment.TopEnd)
                            .clip(shape = MaterialTheme.shapes.small)
                    ) {
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
                    SearchLocal(model.localParams, modifier, playlistSelect)
                } else {
                    SearchRemote(model.remoteParams, liveClick, clearRelatedClick, clearDatesClick, selectDatesClick, modifier)
                }
                Button(
                    onClick = submit,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.End)
                        .clip(shape = MaterialTheme.shapes.small)
                ) {
                    Text(
                        text = "Search",
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
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
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
                onClick = { selectDatesClick() },
                modifier = Modifier
                    .padding(2.dp)
                    .clip(shape = MaterialTheme.shapes.small)
            ) {
                Text(
                    text = dateSelectionText,
                    style = MaterialTheme.typography.body2,
                    modifier = text
                )
            }
        }

        if (model.relatedToPlatformId != null) {
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
                    text = model.relatedToPlatformId,
                    style = MaterialTheme.typography.body2,
                    modifier = text
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


@Composable
private fun SearchLocal(
    model: SearchContract.LocalModel,
    modifier: Modifier,
    playlistSelect: (ChipModel) -> Unit
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
            .clip(shape = MaterialTheme.shapes.small)
//            .background(colorResource(id = R.color.grey_400))
    ) {
        if (model.type != ChipModel.Type.PLAYLIST_SELECT) {
            Icon(
                imageVector = Icons.Default.Clear,
                tint = MaterialTheme.colors.onPrimary,
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

@Composable
fun SearchTextEntryInput(
    text: String,
    textChange: (String) -> Unit,
    submit: () -> Unit
//    iconsVisible: Boolean,
//    icon: TodoIcon,
//    onIconChange: (TodoIcon) -> Unit,
//    buttonSlot: @Composable () -> Unit
) {
    Column {
        Row(
            Modifier
                .padding(top = 16.dp)
        ) {
            TodoInputText(
                text = text,
                onTextChange = textChange,
                onImeAction = submit
            )

            Spacer(modifier = Modifier.width(8.dp))
            //Box(Modifier.align(Alignment.CenterVertically)) { buttonSlot() }
        }

//        if (iconsVisible) {
//            AnimatedIconRow(icon, onIconChange, Modifier.padding(top = 8.dp))
//        } else {
//            Spacer(modifier = Modifier.height(16.dp))
//        }

    }
}


/**
 * Styled [TextField] for inputting search text
 *
 * @param text (state) current text to display
 * @param onTextChange (event) request the text change state
 * @param modifier the modifier for this element
 * @param onImeAction (event) notify caller of [ImeAction.Done] events
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TodoInputText(
    text: String,
    onTextChange: (String) -> Unit,
    onImeAction: () -> Unit = {}
) {
    Box() {
        val keyboardController = LocalSoftwareKeyboardController.current
        TextField(
            value = text,
            onValueChange = onTextChange,
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onImeAction()
                keyboardController?.hideSoftwareKeyboard()
            }),
            textStyle = MaterialTheme.typography.body1,
            modifier = Modifier.fillMaxWidth()
        )
        Icon(
            imageVector = Icons.Default.Clear,
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

@Preview
@Composable
fun PreviewSearchParametersUi() {
    SearchParametersUi(SearchContract.Model(
        type = "Local",
        otherType = "Remote",
        text = "philosophy",
        isLocal = false,
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
            relatedToPlatformId = "dfddffdfasdf",
            fromDate = null,
            toDate = null,
        )
    ), {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
}