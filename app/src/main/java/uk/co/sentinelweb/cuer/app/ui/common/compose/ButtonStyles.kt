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
fun cuerOutlineButtonStroke() = BorderStroke(1.dp, MaterialTheme.colors.onSurface)

@Composable
fun cuerSolidButtonStroke() = BorderStroke(0.dp, MaterialTheme.colors.primary)

@Composable
fun cuerNoOutlineButtonStroke() = BorderStroke(0.dp, MaterialTheme.colors.onSurface)

@Composable
fun HeaderButton(
    text: String,
    icon: Int,
    modifier: Modifier = Modifier,
    colors: ButtonColors = cuerOutlineButtonColors(),
    border: BorderStroke = cuerOutlineButtonStroke(),
    action: () -> Unit
) {
    Button(
        onClick = { action() },
        modifier = modifier
            .padding(end = 16.dp),
        border = border,
        colors = colors,
        elevation = ButtonDefaults.elevation(0.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            tint = colors.contentColor(true).value,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.button,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun HeaderButtonSolid(
    text: String,
    icon: Int,
    modifier: Modifier = Modifier,
    action: () -> Unit
) {
    HeaderButton(text, icon, modifier, cuerSolidButtonColors(), cuerSolidButtonStroke(), action)
}

