package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.remote.RemotePlaylistsInteractor

class GetPlaylistsFromDeviceUseCase(
    private val interactor: RemotePlaylistsInteractor
) {

    suspend fun getPlaylists(remote: RemoteNodeDomain): List<PlaylistDomain> {
        return interactor.getRemotePlaylists(remote)
    }

    suspend fun getPlaylist(playlistDomain: PlaylistDomain): PlaylistDomain {
        return interactor.getRemotePlaylist(playlistDomain.id ?: throw IllegalArgumentException("no id"))
    }
}