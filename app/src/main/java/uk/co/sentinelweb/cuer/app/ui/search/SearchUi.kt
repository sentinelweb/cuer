package uk.co.sentinelweb.cuer.app.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme

@Composable
fun SearchView(viewModel: SearchViewModel) {
    SearchParametersUi(
        searchState = viewModel.searchState,
        textChange = viewModel::onSearchTextChange,
        submit = viewModel::onSubmit
    )
}

@Composable
fun SearchParametersUi(
    searchState: SearchContract.State,
    textChange: (String) -> Unit,
    submit: () -> Unit
) {
    CuerTheme {
        Surface {
            Column(
                modifier = Modifier
                    .height(dimensionResource(R.dimen.search_height))
                    .padding(dimensionResource(R.dimen.page_margin))
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.h3,
//                    fontFamily = Didact
                )
                Divider()
                SearchTextEntryInput(
                    text = searchState.text,
                    textChange = textChange,
                    submit = submit
                )
                Text(
                    text = searchState.text,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.body1,
//                    fontFamily = Montserrat
                )
                Button(
                    onClick = submit,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.End)
                        .clip(shape = MaterialTheme.shapes.small)
                ) {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.button,
//                        fontFamily = Montserrat
                    )
                }
            }
        }
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
                Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
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
 * Styled [TextField] for inputting a [TodoItem].
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
    modifier: Modifier = Modifier,
    onImeAction: () -> Unit = {}
) {
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
        modifier = modifier
    )
}

@Preview
@Composable
fun PreviewSearchParametersUi() {
    SearchParametersUi(SearchContract.State(text = "philosophy"), {}, {})
}