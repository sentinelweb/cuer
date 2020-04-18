package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistModelMapper constructor() {
    fun map(domain: PlaylistDomain): PlaylistModel = PlaylistModel(
        domain.items.map {
            PlaylistModel.PlaylistItemModel(
                it.media.url.toString(),
                it.media.mediaType,
                it.media.title ?: "-",
                it.media.duration?.let { "${(it / 1000)}s" } ?: "-",
                it.media.positon?.let { "${(it / 1000)}s" } ?: "-"
            )
        }
    )
}
