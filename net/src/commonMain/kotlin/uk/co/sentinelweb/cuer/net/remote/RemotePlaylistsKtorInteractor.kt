package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.toLocalNetworkIdentifier
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.remote.server.locator

internal class RemotePlaylistsKtorInteractor(
    val remotePlaylistsService: RemotePlaylistsService
) : RemotePlaylistsInteractor {

    override suspend fun getRemotePlaylists(node: RemoteNodeDomain): List<PlaylistDomain> {
        val locator = node.locator()
        return (remotePlaylistsService.getPlaylists(node).payload as List<PlaylistDomain>)
            // todo rewrite all ids to be local network
            .map { playlist -> playlist.copy(id = playlist.id?.id?.toLocalNetworkIdentifier(locator)) }
    }

    override suspend fun getRemotePlaylist(id: OrchestratorContract.Identifier<GUID>): PlaylistDomain {
        val locator = id.locator ?: throw IllegalArgumentException("No locator")

        return (remotePlaylistsService.getPlaylist(id).payload as List<PlaylistDomain>)
            .first()
            // todo rewrite all ids to be local network
            .let { playlist -> playlist.copy(id = playlist.id?.id?.toLocalNetworkIdentifier(locator)) }
    }
}