package uk.co.sentinelweb.cuer.remote.server.player

import kotlinx.datetime.Clock
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL_NETWORK
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.locator
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerStatusMessage

class PlayerSessionMessageMapper(
    private val guidCreator: GuidCreator,
    private val localRepository: LocalRepository,
) {
    fun map(session: PlayerSession): PlayerStatusMessage =
        PlayerStatusMessage(
            id = session.id,
            item = PlaylistItemDomain(
                // fixme remove id gen when item is stored in PlayerSession
                id = OrchestratorContract.Identifier(
                    guidCreator.create(),
                    LOCAL_NETWORK,
                    localRepository.localNode.locator()
                ),
                media = session.media!!, // fixme remove !!
                dateAdded = Clock.System.now(),
                order = 0,
                playlistId = null
            ),
            liveOffset = session.liveOffset ?: 0,
            playbackState = session.playbackState ?: PlayerStateDomain.UNKNOWN,
            volume = session.volume,
            volumeMax = session.volumeMax
        )
}
