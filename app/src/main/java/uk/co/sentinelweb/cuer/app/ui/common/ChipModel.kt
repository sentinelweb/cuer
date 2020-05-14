package uk.co.sentinelweb.cuer.app.ui.common

import androidx.annotation.DrawableRes

sealed class ChipModel constructor(
    val type: Type,
    val text: String,
    val id: String?,
    val checked: Boolean = false,
    @DrawableRes val icon: Int = 0
) {
    enum class Type {
        TAG, CHANNEL, KEYWORD
    }
}