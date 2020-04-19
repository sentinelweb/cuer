package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistModelMapper constructor() {
    fun map(domain: PlaylistDomain): PlaylistModel = PlaylistModel(
        domain.items.map {
            map(it)
        }
    )

    fun map(it: PlaylistItemDomain): PlaylistModel.PlaylistItemModel {
        val media = it.media
        return map(media)
    }

    fun map(media: MediaDomain): PlaylistModel.PlaylistItemModel {
        val top = media.title ?: media.url
        val bottom = if (media.title == null) "" else media.url
        return PlaylistModel.PlaylistItemModel(
            bottom,
            media.mediaType,
            top,
            media.duration?.let { "${(it / 1000)}s" } ?: "-",
            media.positon?.let { "${(it / 1000)}s" } ?: "-"
        )
    }
}
