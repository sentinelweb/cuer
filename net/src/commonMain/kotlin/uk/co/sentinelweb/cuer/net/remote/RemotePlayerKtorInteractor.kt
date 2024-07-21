package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.client.RequestFailureException
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerCommandMessage
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerStatusMessage

internal class RemotePlayerKtorInteractor(
    private val service: RemotePlayerService,
    private val log: LogWrapper
) : RemotePlayerInteractor {

    init {
        log.tag(this)
    }

    override suspend fun playerCommand(
        locator: Locator,
        message: PlayerCommandMessage
    ): NetResult<PlayerStatusMessage> =
        try {

            NetResult.Data(service.executeCommand(locator, message).payload as PlayerStatusMessage)
        } catch (failure: RequestFailureException) {
            log.e("player command failed", failure)
            NetResult.HttpError(failure)
        } catch (e: Exception) {
            NetResult.Error(e)
        }

    override suspend fun playerSessionStatus(locator: Locator): NetResult<PlayerStatusMessage> =
        try {
            NetResult.Data(service.executeGetPlayerStatus(locator).payload as PlayerStatusMessage)
        } catch (failure: RequestFailureException) {
            log.e("player status failed", failure)
            NetResult.HttpError(failure)
        } catch (e: Exception) {
            NetResult.Error(e)
        }

    override suspend fun getPlayerConfig(locator: Locator): NetResult<PlayerNodeDomain> =
        try {
            NetResult.Data(service.executeGetConfig(locator).payload[0] as PlayerNodeDomain)
        } catch (failure: RequestFailureException) {
            log.e("player config failed", failure)
            NetResult.HttpError(failure)
        } catch (e: Exception) {
            NetResult.Error(e)
        }

    override suspend fun launchPlayerVideo(
        locator: Locator,
        item: PlaylistItemDomain,
        screenIndex: Int
    ): NetResult<Boolean> =
        try {
            service.executeLaunchVideo(locator, item, screenIndex)
            NetResult.Data(true)
        } catch (failure: RequestFailureException) {
            log.e("player launch failed", failure)
            NetResult.HttpError(failure)
        } catch (e: Exception) {
            NetResult.Error(e)
        }
}
