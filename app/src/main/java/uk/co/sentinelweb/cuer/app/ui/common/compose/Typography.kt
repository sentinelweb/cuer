package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import uk.co.sentinelweb.cuer.app.R

val Montserrat = FontFamily(
    Font(R.font.montserrat)
)

val Didact = FontFamily(
    Font(R.font.didact_gothic)
)

// we should get these from MdcTheme - but its not working
val CuerTypography = Typography(
    h1 = TextStyle(
        fontFamily = Didact,
        fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp
    ),
    h2 = TextStyle(
        fontFamily = Didact,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp
    ),
    h3 = TextStyle(
        fontFamily = Didact,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp
    ),
    h4 = TextStyle(
        fontFamily = Didact,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    h5 = TextStyle(
        fontFamily = Didact,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    h6 = TextStyle(
        fontFamily = Didact,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = Didact,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = Didact,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    body1 = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = Montserrat,
        fontSize = 14.sp
    ),
    button = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    overline = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.W500,
        fontSize = 10.sp
    )
)