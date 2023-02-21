package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun cuerOutlineButtonColors() = buttonColors(
    backgroundColor = MaterialTheme.colors.surface,
    contentColor = MaterialTheme.colors.onSurface,
    disabledBackgroundColor = MaterialTheme.colors.surface,
    disabledContentColor = Color.Gray
)

@Composable
fun cuerSolidButtonColors() = buttonColors(
    backgroundColor = MaterialTheme.colors.primary,
    contentColor = Color.White,
    disabledBackgroundColor = MaterialTheme.colors.primaryVariant,
    disabledContentColor = Color.Gray
)

@Composable
fun cuerOutlineButtonStroke(enabled: Boolean = true) = BorderStroke(1.dp, cuerOutlineButtonColors().contentColor(enabled).value)
val cuerOutlineButtonStrokEnabler = @Composable { e: Boolean -> cuerOutlineButtonStroke(e) }

@Composable
fun cuerSolidButtonStroke(enabled: Boolean = true) = BorderStroke(0.dp, cuerSolidButtonColors().backgroundColor(enabled).value)
val cuerSolidButtonStrokeEnabler = @Composable { e: Boolean -> cuerSolidButtonStroke(e) }

@Composable
fun cuerNoOutlineButtonStroke(enabled: Boolean = true) = BorderStroke(0.dp, cuerOutlineButtonColors().backgroundColor(enabled).value)

@Composable
fun HeaderButton(
    text: String,
    icon: Int,
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
        Icon(
            painter = painterResource(icon),
            tint = colors.contentColor(enabled).value,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.button,
            modifier = Modifier.padding(start = 4.dp),
            color = colors.contentColor(enabled).value,
        )
    }
}

@Composable
fun HeaderButtonSolid(
    text: String,
    icon: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    action: () -> Unit
) {
    HeaderButton(text, icon, modifier, cuerSolidButtonColors(), cuerSolidButtonStrokeEnabler, enabled, action)
}

