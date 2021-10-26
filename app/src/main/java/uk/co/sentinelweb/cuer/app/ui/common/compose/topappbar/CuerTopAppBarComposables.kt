package uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.app.R

object CuerTopAppBarComposables {
    @Composable
    fun CuerAppBar(
        text: String,
        backgroundColor: Color = MaterialTheme.colors.primarySurface,
        contentColor: Color = contentColorFor(backgroundColor),
        actions: List<Action> = listOf(),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.h4
                )
            },
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            actions = { Actions(actions) }
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
    object Settings : CuerMenuItem(R.string.menu_settings, R.drawable.ic_menu_settings_black)
}

data class Action(
    val item: CuerMenuItem,
    val action: () -> Unit,
)