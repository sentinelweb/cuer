package uk.co.sentinelweb.cuer.domain.mappers

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistAndItemMapper {
    fun map(playlist: PlaylistDomain?, item: PlaylistItemDomain) = PlaylistAndItemDomain(
        item = item,
        playlistId = playlist?.id ?: item.playlistId,
        playlistTitle = playlist?.title
    )

    fun map(playlistId: OrchestratorContract.Identifier<GUID>, item: PlaylistItemDomain) = PlaylistAndItemDomain(
        item = item,
        playlistId = playlistId,
        playlistTitle = null
    )

    fun map(item: PlaylistItemDomain) = PlaylistAndItemDomain(item = item)

}