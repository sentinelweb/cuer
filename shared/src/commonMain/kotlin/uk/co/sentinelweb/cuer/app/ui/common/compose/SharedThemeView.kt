package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

object SharedThemeView {

    @Composable
    fun View() {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer).shadow(4.dp).padding(8.dp)) {
                Text(
                    "SharedTheme",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text("Colors",
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge)

            ColorRow(MaterialTheme.colorScheme.primary, "primary")
            ColorRow(MaterialTheme.colorScheme.onPrimary, "onPrimary")
            ColorRow(MaterialTheme.colorScheme.secondary, "secondary")
            ColorRow(MaterialTheme.colorScheme.onSecondary, "onSecondary")
            ColorRow(MaterialTheme.colorScheme.tertiary, "tertiary")
            ColorRow(MaterialTheme.colorScheme.onTertiary, "onTertiary")
            ColorRow(MaterialTheme.colorScheme.primaryContainer, "primaryContainer")
            ColorRow(MaterialTheme.colorScheme.onPrimaryContainer, "onPrimaryContainer")
            ColorRow(MaterialTheme.colorScheme.surface, "surface")
            ColorRow(MaterialTheme.colorScheme.onSurface, "onSurface")
            ColorRow(MaterialTheme.colorScheme.background, "background")
            ColorRow(MaterialTheme.colorScheme.onBackground, "onBackground")

            Text("Type",
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge)

            TypeRow(MaterialTheme.typography.displayLarge, "displayLarge")
            TypeRow(MaterialTheme.typography.displayMedium, "displayMedium")
            TypeRow(MaterialTheme.typography.displaySmall, "displaySmall")
            TypeRow(MaterialTheme.typography.headlineLarge, "headlineLarge")
            TypeRow(MaterialTheme.typography.headlineMedium, "headlineMedium")
            TypeRow(MaterialTheme.typography.headlineSmall, "headlineSmall")
            TypeRow(MaterialTheme.typography.titleLarge, "titleLarge")
            TypeRow(MaterialTheme.typography.titleMedium, "titleMedium")
            TypeRow(MaterialTheme.typography.titleSmall, "titleSmall")
            TypeRow(MaterialTheme.typography.bodyLarge, "bodyLarge")
            TypeRow(MaterialTheme.typography.bodyMedium, "bodyMedium")
            TypeRow(MaterialTheme.typography.bodySmall, "bodySmall")
            TypeRow(MaterialTheme.typography.labelLarge, "labelLarge")
            TypeRow(MaterialTheme.typography.labelMedium, "labelMedium")
            TypeRow(MaterialTheme.typography.labelSmall, "labelSmall")

        }
    }

    @Composable
    private fun TypeRow(textStyle: TextStyle, text: String) {
        Text(
            text,
            color = Color.Black,
            style = textStyle,
        )
    }

    @Composable
    private fun ColorRow(color: Color, name: String) {
        Row(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .background(color)
                    .border(1.dp, Color.Black)
                    .size(48.dp)
                    .padding(4.dp)
            )

            Text(
                name,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,

                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp)
            )
        }
    }

}
