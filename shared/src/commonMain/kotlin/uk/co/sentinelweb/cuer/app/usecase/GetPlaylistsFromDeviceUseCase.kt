package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.remote.RemotePlaylistsInteractor

class GetPlaylistsFromDeviceUseCase(
    private val interactor: RemotePlaylistsInteractor
) {

    suspend operator fun invoke(remote: RemoteNodeDomain): List<PlaylistDomain> {
        return interactor.getRemotePlaylists(remote)
    }
}