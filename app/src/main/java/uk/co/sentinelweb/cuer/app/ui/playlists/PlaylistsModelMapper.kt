package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModel
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistsModelMapper constructor() {

    fun map(domains: List<PlaylistDomain>): PlaylistsModel = PlaylistsModel(
        domains.mapIndexed { index, pl ->
            ItemModel(
                pl.id!!,
                index,
                pl.title,
                (if (pl.starred) " * " else " ") + (if (pl.default) " D " else " ") + pl.mode,
                false,
                (pl.thumb ?: pl.image)?.url
            )
        }
    )

}
