package uk.co.sentinelweb.cuer.hub.ui.common.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.hub.ui.common.image.ImageSvg

// todo copied from app file of the same name - merge to a library
@Composable
fun cuerOutlineButtonColors() = ButtonDefaults.buttonColors(
    backgroundColor = MaterialTheme.colors.surface,
    contentColor = MaterialTheme.colors.onSurface,
    disabledBackgroundColor = MaterialTheme.colors.surface,
    disabledContentColor = Color.Gray
)

@Composable
fun cuerSolidButtonColors() = ButtonDefaults.buttonColors(
    backgroundColor = MaterialTheme.colors.primary,
    contentColor = Color.White,
    disabledBackgroundColor = MaterialTheme.colors.primaryVariant,
    disabledContentColor = Color.Gray
)

@Composable
fun cuerOutlineButtonStroke(enabled: Boolean = true) =
    BorderStroke(1.dp, cuerOutlineButtonColors().contentColor(enabled).value)

val cuerOutlineButtonStrokEnabler = @Composable { e: Boolean -> cuerOutlineButtonStroke(e) }

@Composable
fun cuerSolidButtonStroke(enabled: Boolean = true) =
    BorderStroke(0.dp, cuerSolidButtonColors().backgroundColor(enabled).value)

val cuerSolidButtonStrokeEnabler = @Composable { e: Boolean -> cuerSolidButtonStroke(e) }

@Composable
fun cuerNoOutlineButtonStroke(enabled: Boolean = true) =
    BorderStroke(0.dp, cuerOutlineButtonColors().backgroundColor(enabled).value)

@Composable
fun HeaderButton(
    text: String,
    icon: String,
    modifier: Modifier = Modifier,
    colors: ButtonColors = cuerOutlineButtonColors(),
    border: @Composable (Boolean) -> BorderStroke = cuerOutlineButtonStrokEnabler,
    enabled: Boolean = true,
    action: () -> Unit
) {
    Button(
        onClick = { action() },
        modifier = modifier
            .padding(end = 16.dp),
        border = border(enabled),
        colors = colors,
        enabled = enabled,
        elevation = ButtonDefaults.elevation(0.dp),
    ) {

        ImageSvg(icon)

        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.button,
            modifier = Modifier.padding(start = 4.dp),
            color = colors.contentColor(enabled).value,
        )
    }
}
