package uk.co.sentinelweb.cuer.app.ui.common.itemlist.item

import androidx.annotation.DrawableRes

// todo make generic
open class ItemModel(
    val id: String?,
    val topText: String,
    val bottomText: String,
    val checkIcon: Boolean,
    @DrawableRes val iconRes: Int
)
