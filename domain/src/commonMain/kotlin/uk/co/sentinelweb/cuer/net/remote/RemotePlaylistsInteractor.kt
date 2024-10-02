package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.NetResult

interface RemotePlaylistsInteractor {
    suspend fun getRemotePlaylists(node: RemoteNodeDomain): NetResult<List<PlaylistDomain>>

    suspend fun getRemotePlaylist(id: OrchestratorContract.Identifier<GUID>): NetResult<PlaylistDomain>
}