package uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.app.R

object CuerTopAppBarComposables {
    @Composable
    fun CuerAppBar(
        title: String,
        subTitle: String? = null,
        modifier: Modifier = Modifier,
        backgroundColor: Color = MaterialTheme.colors.primarySurface,
        contentColor: Color = contentColorFor(backgroundColor),
        actions: List<Action> = listOf(),
        onUp: (() -> Unit)? = null,
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h5,
                        maxLines = 1,
                        color = Color.White,
                    )
                    if (subTitle != null) {
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.h6,
                            maxLines = 1,
                            color = Color.White,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
            navigationIcon = {
                if (onUp != null) {
                    IconButton(onClick = { onUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            tint = Color.White,
                            contentDescription = stringResource(id = R.string.up)

                        )
                    }
                }
            },
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            actions = { Actions(actions) },
            modifier = modifier
        )
    }

    @Composable
    private fun Actions(actions: List<Action>) {
        actions.forEach { Action(it) }
    }

    @Composable
    private fun Action(action: Action) {
        Icon(
            painter = painterResource(action.item.icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .clickable { action.action() }
                .size(48.dp)
                .padding(12.dp)
        )
    }
}

sealed class CuerMenuItem constructor(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    object Settings : CuerMenuItem(R.string.menu_settings, R.drawable.ic_menu_settings)
    object SortAlpha : CuerMenuItem(R.string.menu_sort_alpha, R.drawable.ic_sort_by_alpha)
    object SortCategory : CuerMenuItem(R.string.menu_sort_category, R.drawable.ic_category)
    object Search : CuerMenuItem(R.string.menu_search, R.drawable.ic_search)
    object PasteAdd : CuerMenuItem(R.string.menu_paste, R.drawable.ic_menu_paste_add)
    object Help : CuerMenuItem(R.string.help, R.drawable.ic_help)
}

data class Action(
    val item: CuerMenuItem,
    val action: () -> Unit,
)
