package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerCommandMessage
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerStatusMessage

interface RemotePlayerInteractor {
    suspend fun playerCommand(locator: Locator, message: PlayerCommandMessage): NetResult<Boolean>

    suspend fun getPlayerConfig(locator: Locator): NetResult<PlayerNodeDomain>

    suspend fun playerSessionStatus(locator: Locator): NetResult<PlayerStatusMessage>

    suspend fun launchPlayerVideo(locator: Locator, item: PlaylistItemDomain, screenIndex: Int): NetResult<Boolean>
}
