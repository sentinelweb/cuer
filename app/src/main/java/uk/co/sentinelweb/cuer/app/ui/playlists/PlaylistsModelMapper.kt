package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class PlaylistsModelMapper constructor() {

    fun map(domains: Map<PlaylistDomain, PlaylistStatDomain?>, current: OrchestratorContract.Identifier<*>?): PlaylistsContract.Model =
        PlaylistsContract.Model(
            DEFAULT_HEADER_IMAGE,
            current,
            domains.keys.mapIndexed { index, pl ->
                ItemContract.Model(
                    pl.id!!,
                    index,
                    pl.title,
                    false,
                    (pl.thumb ?: pl.image)?.url,
                    count = domains[pl]?.itemCount ?: -1,
                    newItems = domains[pl]?.let { it.itemCount - it.watchedItemCount } ?: -1,
                    starred = pl.starred,
                    loopMode = pl.mode,
                    type = pl.type,
                    platform = pl.platform
                )
            }
        )

    companion object {
        const val DEFAULT_HEADER_IMAGE = "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg"
    }

}
