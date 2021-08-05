package uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
    ) {
        TopAppBar(
            title = { Text(text = text) },
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            actions = { Actions() }
        )
    }

    @Composable
    private fun Actions() {
        Action(CuerMenuItem.Settings)
    }

    @Composable
    private fun Action(item: CuerMenuItem) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .padding(start = 16.dp)
        )
    }
}

sealed class CuerMenuItem constructor(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,

    ) {
    object Settings : CuerMenuItem(R.string.menu_settings, R.drawable.ic_menu_settings_black)
}