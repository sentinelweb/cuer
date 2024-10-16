package uk.co.sentinelweb.cuer.remote.server.player

import rewriteIdsToSource
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL_NETWORK
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
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
            item = if (session.item?.id?.source == LOCAL_NETWORK) {
                session.item ?: error("No item")
            } else {
                session.item?.rewriteIdsToSource(LOCAL_NETWORK, localRepository.localNode.locator())
                    ?: error("No item")
            },
            liveOffset = session.liveOffset ?: 0,
            playbackState = session.playbackState ?: PlayerStateDomain.UNKNOWN,
            volume = session.volume,
            volumeMax = session.volumeMax,
            screen = session.screen
        )
}
