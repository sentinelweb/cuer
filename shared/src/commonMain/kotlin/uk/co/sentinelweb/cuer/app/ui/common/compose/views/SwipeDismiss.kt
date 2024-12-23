package uk.co.sentinelweb.cuer.app.ui.common.compose.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.End
import androidx.compose.foundation.layout.Arrangement.Start
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import uk.co.sentinelweb.cuer.app.ui.common.compose.colorDelete
import uk.co.sentinelweb.cuer.app.ui.common.compose.colorEdit
import uk.co.sentinelweb.cuer.app.ui.common.compose.colorMove
import uk.co.sentinelweb.cuer.shared.generated.resources.*
import uk.co.sentinelweb.cuer.shared.generated.resources.Res
import uk.co.sentinelweb.cuer.shared.generated.resources.ic_edit
import uk.co.sentinelweb.cuer.shared.generated.resources.ic_move
import uk.co.sentinelweb.cuer.shared.generated.resources.menu_edit

data class SwipDismissResources(
    val color: Color,
    val icon: Painter,
    val text: String,
    val action: () -> Unit
)

@ExperimentalMaterialApi
@Composable
fun swipeToDismiss(toEnd: SwipDismissResources, toStart: SwipDismissResources, content: @Composable () -> Unit) {
    val dismissState = rememberDismissState(
        initialValue = DismissValue.Default,
        confirmStateChange = {
            when (it) {
                DismissValue.DismissedToEnd -> {
                    toEnd.action()
                    true
                }

                DismissValue.DismissedToStart -> {
                    toStart.action()
                    true
                }

                else -> false
            }
        }
    )
    if (dismissState.currentValue != DismissValue.Default) {
        LaunchedEffect(Unit) {
            dismissState.reset()
        }
    }
    SwipeToDismiss(
        state = dismissState,
        background = {
            val direction = dismissState.dismissDirection
            val isDismissing = dismissState.dismissDirection != null
            if (direction == DismissDirection.StartToEnd) {
                SwipeDismissBackground(toEnd, isDismissing, true)
            } else {
                SwipeDismissBackground(toStart, isDismissing, false)
            }
        },
        /**** Dismiss Content */
        /**** Dismiss Content */
        dismissContent = {
            content()
        },
        /*** Set Direction to dismiss */
        /*** Set Direction to dismiss */
        directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
    )
}

@Composable
fun deleteSwipeResources(action: () -> Unit) = SwipDismissResources(
    color = colorDelete,
    icon = painterResource(Res.drawable.ic_delete),
    text = stringResource(Res.string.menu_delete),
    action = action
)

@Composable
fun editSwipeResources(action: () -> Unit) = SwipDismissResources(
    color = colorEdit,
    icon = painterResource(Res.drawable.ic_edit),
    text = stringResource(Res.string.menu_edit),
    action = action
)

@Composable
fun moveSwipeResources(action: () -> Unit) = SwipDismissResources(
    color = colorMove,
    icon = painterResource(Res.drawable.ic_move),
    text = stringResource(Res.string.menu_move),
    action = action
)

@Composable
fun SwipeDismissBackground(
    resources: SwipDismissResources,
    isDismissing: Boolean,
    startToEnd: Boolean
) {
    val arrangement: Arrangement.Horizontal = if (startToEnd) End else Start
    val alignment: Alignment = if (startToEnd) CenterStart else CenterEnd
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDismissing) resources.color else Color.Transparent)
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = arrangement,
            modifier = Modifier.align(alignment)
        ) {
            Icon(
                painter = resources.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )
            Text(
                text = resources.text,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = 8.dp)

            )
        }
    }
}
