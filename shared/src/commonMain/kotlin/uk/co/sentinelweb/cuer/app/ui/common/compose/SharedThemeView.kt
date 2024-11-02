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
import uk.co.sentinelweb.cuer.app.ui.common.compose.views.HeaderButton
import uk.co.sentinelweb.cuer.app.ui.common.compose.views.HeaderButtonSolid
import uk.co.sentinelweb.cuer.shared.generated.resources.Res
import uk.co.sentinelweb.cuer.shared.generated.resources.ic_login

object SharedThemeView {

    @Composable
    fun View() {
        CuerSharedTheme {
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary)
                        .shadow(4.dp)
                        .padding(8.dp)
                ) {
                    Text(
                        "SharedTheme",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Text(
                    "Buttons",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )

                HeaderButton(
                    text = "Header Button",
                    icon = Res.drawable.ic_login,
                ) { }

                HeaderButtonSolid(
                    text = "Header Button Solid",
                    icon = Res.drawable.ic_login,
                ) { }

                HeaderButton(
                    text = "Header Button disabled",
                    enabled = false,
                    icon = Res.drawable.ic_login,
                ) { }

                HeaderButtonSolid(
                    text = "Header Button Solid disabled",
                    enabled = false,
                    icon = Res.drawable.ic_login,
                ) { }

                Text(
                    "Colors",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )

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

                Text(
                    "Type",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )

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
    }

    @Composable
    private fun TypeRow(textStyle: TextStyle, text: String) {
        Text(
            text,
            color = MaterialTheme.colorScheme.onSurface,
            style = textStyle,
        )
    }

    @Composable
    private fun ColorRow(color: Color, name: String) {
        Row(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .background(color)
                    .border(1.dp, color = MaterialTheme.colorScheme.onSurface)
                    .size(48.dp)
                    .padding(4.dp)
            )

            Text(
                name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,

                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp)
            )
        }
    }

}
