package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import uk.co.sentinelweb.cuer.shared.generated.resources.*
import uk.co.sentinelweb.cuer.shared.generated.resources.Res
import uk.co.sentinelweb.cuer.shared.generated.resources.menu_search
import uk.co.sentinelweb.cuer.shared.generated.resources.menu_settings
import uk.co.sentinelweb.cuer.shared.generated.resources.up

object CuerSharedAppBarComposables {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CuerSharedAppBar(
        title: String,
        subTitle: String? = null,
        modifier: Modifier = Modifier,
        backgroundColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = contentColorFor(backgroundColor),
        actions: List<Action> = listOf(),
        onUp: (() -> Unit)? = null,
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        color = Color.White,
                    )
                    if (subTitle != null) {
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.bodyMedium,
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
                            contentDescription = stringResource(Res.string.up)

                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundColor
            ),
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

sealed class CuerMenuItem(
    val label: StringResource,
    val icon: DrawableResource,
) {
    object Settings : CuerMenuItem(Res.string.menu_settings, Res.drawable.ic_menu_settings)
    object Search : CuerMenuItem(Res.string.menu_search, Res.drawable.ic_search)
    object PasteAdd : CuerMenuItem(Res.string.menu_paste, Res.drawable.ic_menu_paste_add)
    object Help : CuerMenuItem(Res.string.menu_help, Res.drawable.ic_help)
}

data class Action(
    val item: CuerMenuItem,
    val action: () -> Unit,
)
