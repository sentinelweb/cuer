package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import uk.co.sentinelweb.cuer.shared.generated.resources.Res
import uk.co.sentinelweb.cuer.shared.generated.resources.didactgothic_regular
import uk.co.sentinelweb.cuer.shared.generated.resources.montserrat_variable_font_wght

private val AppShapes = Shapes(
    extraSmall = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
    small = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
    medium = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
    large = CutCornerShape(topStart = 32.dp, bottomEnd = 32.dp),
    extraLarge = CutCornerShape(topStart = 32.dp, bottomEnd = 32.dp)
)

private val cuerLightColors = lightColorScheme(
    primary = Color( 0xFFe53935)
)

private val cuerDarkColors = darkColorScheme(
    primary = Color( 0xFFe53935),
)

val colorDelete = Color( 0xFFe53935)
val colorEdit = Color( 0xFFe53935)
val colorMove = Color( 0xFFe53935)


@Composable
internal fun CuerSharedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {

    val Montserrat = FontFamily(
        //Font(Res.font.montserrat_light)
        Font(Res.font.montserrat_variable_font_wght),
    )

    val Didact = FontFamily(
        Font(Res.font.didactgothic_regular)
    )

    val CuerTypography = Typography(
        displayLarge = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 48.sp
        ),
        displayMedium = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp
        ),
        displaySmall = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        ),
        titleLarge = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        bodySmall = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        ),
        labelLarge = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Light,
            fontSize = 16.sp
        ),
        labelMedium = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Light,
            fontSize = 14.sp
        ),
        labelSmall = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp
        ),
    )

    MaterialTheme(
        typography = CuerTypography,
        shapes = AppShapes,
        colorScheme = if (darkTheme) cuerDarkColors else cuerLightColors,
        content = {
            Surface(content = content)
        }
    )
}

@Composable
fun SharedThemeView() {
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
