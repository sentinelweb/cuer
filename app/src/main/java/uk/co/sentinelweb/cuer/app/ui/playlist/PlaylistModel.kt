package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemModel

data class PlaylistModel constructor(
    val items: List<PlaylistItemModel>
) {
    data class PlaylistItemModel constructor(
        val url: String,
        val type: MediaDomain.MediaTypeDomain,
        val title: String,
        val length: String,
        val positon: String
    ) : ItemModel(
        "id",
        title,
        url,
        false,
        R.drawable.ic_play_black
    )
}