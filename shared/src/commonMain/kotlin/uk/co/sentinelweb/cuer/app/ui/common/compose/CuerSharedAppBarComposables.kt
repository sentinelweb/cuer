package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import uk.co.sentinelweb.cuer.shared.generated.resources.*

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
        overflowActions: List<Action>? = null,
        onUp: (() -> Unit)? = null,
    ) {
        TopAppBar(
            title = {
                Box(modifier = modifier.fillMaxHeight()) {
                    Column(modifier = modifier.align(Alignment.CenterStart)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            color = contentColor,
                        )
                        if (subTitle != null) {
                            Text(
                                text = subTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                color = contentColor,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                if (onUp != null) {
                    IconButton(onClick = { onUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            tint = contentColor,
                            contentDescription = stringResource(Res.string.up)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundColor,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor,
            ),
            actions = { Actions(actions = actions, contentColor = contentColor, overflow = overflowActions) },
            modifier = modifier
        )
    }

    @Composable
    private fun Actions(
        actions: List<Action>,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
        overflow: List<Action>? = null,
    ) {
        actions.forEach { Action(it, contentColor) }
        overflow?.also {
            TopAppBarOverflowMenu(it, contentColor)
        }
    }

    @Composable
    private fun Action(
        action: Action,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(
            painter = painterResource(action.item.icon),
            contentDescription = null,
            tint = contentColor,
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
    object Reload : CuerMenuItem(Res.string.menu_refresh, Res.drawable.ic_refresh)

    object SortAlpha : CuerMenuItem(Res.string.menu_sort_alpha, Res.drawable.ic_sort_by_alpha)
    object SortCategory : CuerMenuItem(Res.string.menu_sort_category, Res.drawable.ic_category)
    object SortTime : CuerMenuItem(Res.string.menu_sort_time, Res.drawable.ic_sort_time)

    object Folders : CuerMenuItem(Res.string.menu_folders, Res.drawable.ic_folder)
    object ThemeTest : CuerMenuItem(Res.string.menu_theme_test, Res.drawable.ic_edit)
    object LocalConfig : CuerMenuItem(Res.string.menu_local_config, Res.drawable.ic_settings_ethernet)
}

data class Action(
    val item: CuerMenuItem,
    val action: () -> Unit,
)

@Composable
fun TopAppBarOverflowMenu(
    actions: List<Action> = listOf(),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    var expanded by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(0.dp) }

    IconButton(onClick = { expanded = true },
        modifier = Modifier.onGloballyPositioned { coordinates -> menuOffset = coordinates.size.width.dp }
    ) {
        Icon(Icons.Filled.MoreVert, contentDescription = "Overflow Menu")
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        offset = DpOffset(menuOffset.value.dp, 0.dp),
        modifier = Modifier
    ) {
        actions.forEach { action ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(action.item.icon),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier
                            .clickable {
                                expanded = false
                                action.action()
                            }
                            .size(48.dp)
                            .padding(12.dp)
                    )
                },
                text = { Text(stringResource(action.item.label)) },
                onClick = {
                    expanded = false
                    action.action()
                },
            )
        }
    }
}
