package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistModelMapper constructor() {

    fun map(domain: PlaylistDomain): PlaylistModel = PlaylistModel(
        domain.items.mapIndexed { index, item ->
            map(item, index)
        }
    )

    private fun map(it: PlaylistItemDomain, index: Int): PlaylistModel.PlaylistItemModel {
        val media = it.media
        return map(media, index)
    }

    private fun map(media: MediaDomain, index: Int): PlaylistModel.PlaylistItemModel {
        val top = media.title ?: "No title"
        val bottom = media.url
        return PlaylistModel.PlaylistItemModel(
            media.id.toString(),
            index,
            bottom,
            media.mediaType,
            top,
            media.duration?.let { "${(it / 1000)}s" } ?: "-",
            media.positon?.let { "${(it / 1000)}s" } ?: "-",
            media.thumbNail?.url
        )
    }
}
