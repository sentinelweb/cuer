package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import uk.co.sentinelweb.cuer.shared.generated.resources.Res
import uk.co.sentinelweb.cuer.shared.generated.resources.didactgothic_regular
import uk.co.sentinelweb.cuer.shared.generated.resources.montserrat_light

private val AppShapes = Shapes(
    extraSmall = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
    small = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
    medium = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
    large = CutCornerShape(topStart = 32.dp, bottomEnd = 32.dp),
    extraLarge = CutCornerShape(topStart = 32.dp, bottomEnd = 32.dp)
)

private val lightColors = lightColorScheme(
    primary = "#e53935".toColor(),
)
private val darkColors = darkColorScheme(
    primary = "#e53935".toColor(),
)

@Composable
internal fun CuerSharedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {

    val Montserrat = FontFamily(Font(Res.font.montserrat_light))

    val Didact = FontFamily(Font(Res.font.didactgothic_regular))

    val CuerTypography = Typography(
        headlineLarge = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.SemiBold,
            fontSize = 36.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp
        ),
        titleLarge = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        ),
        titleMedium = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        ),
        titleSmall = TextStyle(
            fontFamily = Didact,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        ),
        bodySmall = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp
        ),
    )

    MaterialTheme(
        typography = CuerTypography,
        shapes = AppShapes,
        colorScheme = if (darkTheme) darkColors else lightColors,
        content = {
            println("invoke theme")
            Surface(content = content)
        }
    )
}
