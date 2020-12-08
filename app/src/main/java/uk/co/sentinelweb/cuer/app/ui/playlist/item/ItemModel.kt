package uk.co.sentinelweb.cuer.app.ui.playlist.item

import androidx.annotation.DrawableRes

open class ItemModel(
    open val id: Long?,
    val topText: String,
    val bottomText: String,
    val checkIcon: Boolean,
    open @DrawableRes val iconRes: Int? = null,
    open val thumbNailUrl: String?
)
