package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.domain.MediaDomain

data class PlaylistModel constructor(
    val items: List<PlaylistItemModel>
) {
    data class PlaylistItemModel constructor(
        val url: String,
        val type: MediaDomain.MediaType,
        val title: String,
        val length: String,
        val positon: String
    )
}