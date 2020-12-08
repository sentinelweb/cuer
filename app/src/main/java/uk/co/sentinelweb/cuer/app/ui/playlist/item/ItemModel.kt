package uk.co.sentinelweb.cuer.app.ui.playlist.item

import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel

open class ItemModel(
    override val id: Long,
    val topText: String,
    val bottomText: String,
    val checkIcon: Boolean,
    open @DrawableRes val iconRes: Int? = null,
    open val thumbNailUrl: String?
) : ItemBaseModel(id)
