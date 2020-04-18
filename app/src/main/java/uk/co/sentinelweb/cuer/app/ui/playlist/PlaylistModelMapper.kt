package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistModelMapper constructor() {
    fun map(domain: PlaylistDomain): PlaylistModel = PlaylistModel(
        domain.items.map {
            PlaylistModel.PlaylistItemModel(
                it.media.url.toString(),
                it.media.type,
                it.media.title,
                "${(it.media.lengthMs / 1000)}s",
                "${(it.media.positonMs / 1000)}s"
            )
        }
    )
}
