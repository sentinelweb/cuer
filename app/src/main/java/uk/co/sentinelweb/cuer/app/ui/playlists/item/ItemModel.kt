package uk.co.sentinelweb.cuer.app.ui.playlists.item

import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel

open class ItemModel(
    override val id: Long,
    val index: Int,
    val title: String,
    val data: String,
    val checkIcon: Boolean,
    val thumbNailUrl: String?
) : ItemBaseModel(id)
