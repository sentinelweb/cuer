package uk.co.sentinelweb.cuer.remote.server.player

import kotlinx.datetime.Clock
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayerSessionMessageMapper {
    fun map(session: PlayerSession): PlayerSessionContract.PlayerStatusMessage =
        PlayerSessionContract.PlayerStatusMessage(
            id = session.id,
            item = PlaylistItemDomain(
                media = session.media!!, // fixme remove !!
                dateAdded = Clock.System.now(),
                order = 0, playlistId = null
            ),
            liveOffset = session.liveOffset ?: 0,
            playbackState = session.playbackState ?: PlayerStateDomain.UNKNOWN
        )
}