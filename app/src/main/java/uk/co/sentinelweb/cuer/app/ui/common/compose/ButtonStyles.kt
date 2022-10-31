package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun cuerOutlineButtonColors() = buttonColors(
    backgroundColor = MaterialTheme.colors.surface,
    contentColor = MaterialTheme.colors.onSurface,
    disabledBackgroundColor = MaterialTheme.colors.surface,
    disabledContentColor = Color.Gray
)

@Composable
fun cuerOutlineButtonStroke() = BorderStroke(1.dp, MaterialTheme.colors.onSurface)