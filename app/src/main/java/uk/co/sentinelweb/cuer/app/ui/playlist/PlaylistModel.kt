package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModel
import uk.co.sentinelweb.cuer.domain.MediaDomain

data class PlaylistModel constructor(
    val items: List<PlaylistItemModel>
) {
    data class PlaylistItemModel constructor(
        override val id: Long?,
        val index: Int,
        val url: String,
        val type: MediaDomain.MediaTypeDomain,
        val title: String,
        val length: String,
        val positon: String,
        override val thumbNailUrl: String?
    ) : ItemModel(
        id,
        title,
        url,
        false,
        0,
        thumbNailUrl
    )
}