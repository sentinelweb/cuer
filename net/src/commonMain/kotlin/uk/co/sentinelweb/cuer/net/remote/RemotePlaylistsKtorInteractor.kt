package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.client.RequestFailureException
import uk.co.sentinelweb.cuer.net.ext.convertIdsLocalNetowrk
import uk.co.sentinelweb.cuer.remote.server.locator

internal class RemotePlaylistsKtorInteractor(
    val remotePlaylistsService: RemotePlaylistsService
) : RemotePlaylistsInteractor {

    override suspend fun getRemotePlaylists(node: RemoteNodeDomain): NetResult<List<PlaylistDomain>> = try {
        val locator = node.locator()
        (remotePlaylistsService.getPlaylists(node).payload as List<PlaylistDomain>)
            // todo rewrite all ids to be local network
            .map { playlist -> playlist.convertIdsLocalNetowrk(locator) }
            .let { NetResult.Data(it) }
    } catch (e: RequestFailureException) {
        NetResult.HttpError(e)
    } catch (e: Exception) {
        NetResult.Error(e)
    }

    override suspend fun getRemotePlaylist(id: OrchestratorContract.Identifier<GUID>): NetResult<PlaylistDomain> = try {
        val locator = id.locator ?: throw IllegalArgumentException("No locator")
        (remotePlaylistsService.getPlaylist(id).payload as List<PlaylistDomain>)
            .first()
            .convertIdsLocalNetowrk(locator)
            .let { NetResult.Data(it) }
    } catch (e: RequestFailureException) {
        NetResult.HttpError(e)
    } catch (e: Exception) {
        NetResult.Error(e)
    }
}