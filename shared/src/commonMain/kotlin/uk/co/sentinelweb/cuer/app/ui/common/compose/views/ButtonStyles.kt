package uk.co.sentinelweb.cuer.app.ui.common.compose.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private lateinit var outlineButtonColors: ButtonColors
private lateinit var solidButtonColors: ButtonColors

@Composable
fun initButtonColors() {
    outlineButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = Color.LightGray,
        disabledContentColor = Color.DarkGray
    )

    solidButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        disabledContainerColor = Color.LightGray,
        disabledContentColor = Color.DarkGray
    )
}

@Composable fun outlineBorderColor(enabled: Boolean) = if (enabled) MaterialTheme.colorScheme.onSurface else Color.LightGray
@Composable fun outlineContentColor(enabled: Boolean) = if (enabled) MaterialTheme.colorScheme.onSurface else Color.LightGray

@Composable fun solidBorderColor(enabled: Boolean) = if (enabled) MaterialTheme.colorScheme.primary else Color.LightGray
@Composable fun solidContentColor(enabled: Boolean) = if (enabled) MaterialTheme.colorScheme.primary else Color.LightGray

@Composable
fun cuerOutlineButtonStroke(enabled: Boolean = true) = BorderStroke(1.dp, outlineBorderColor(enabled))

val cuerOutlineButtonStrokEnabler = @Composable { e: Boolean -> cuerOutlineButtonStroke(e) }

@Composable
fun cuerSolidButtonStroke(enabled: Boolean = true) = BorderStroke(0.dp, solidBorderColor(enabled))
val cuerSolidButtonStrokeEnabler = @Composable { e: Boolean -> cuerSolidButtonStroke(e) }

//@Composable
//private fun containerColor(enabled: Boolean) = if (enabled) MaterialTheme.colorScheme.onSurface else Color.LightGray


@Composable
fun HeaderButton(
    text: String,
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    colors: ButtonColors = outlineButtonColors,
    border: @Composable (Boolean) -> BorderStroke = cuerOutlineButtonStrokEnabler,
    enabled: Boolean = true,
    action: () -> Unit
) {
    Button(
        onClick = { action() },
        modifier = modifier
            .padding(end = 8.dp)
            .shadow(if (enabled) 4.dp else 0.dp, shape = MaterialTheme.shapes.small),

        border = border(enabled),
        colors = colors,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            tint = if (enabled) colors.contentColor else colors.disabledContentColor,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(4.dp, end = 0.dp)
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 8.dp, start = 8.dp),
            color = if (enabled) colors.contentColor else colors.disabledContentColor
        )
    }
}

@Composable
fun HeaderButtonSolid(
    text: String,
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    action: () -> Unit
) {
    HeaderButton(text, icon, modifier, solidButtonColors, cuerSolidButtonStrokeEnabler, enabled, action)
}
