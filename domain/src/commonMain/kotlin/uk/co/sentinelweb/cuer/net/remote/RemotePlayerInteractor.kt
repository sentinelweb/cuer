package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerMessage

interface RemotePlayerInteractor {
    suspend fun playerCommand(locator: Locator, message: PlayerMessage): NetResult<Boolean>

    suspend fun getPlayerConfig(locator: Locator): NetResult<PlayerNodeDomain>
}
