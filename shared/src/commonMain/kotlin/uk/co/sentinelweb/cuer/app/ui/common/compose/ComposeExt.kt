package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.ui.graphics.Color

fun String.toColor(): Color {
    val red = this.substring(1, 3).toInt(16)
    val green = this.substring(3, 5).toInt(16)
    val blue = this.substring(5, 7).toInt(16)
    return Color(red = red, green = green, blue = blue)
}
