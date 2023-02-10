package uk.co.sentinelweb.cuer.remote.server.database

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface RemoteDatabaseAdapter {

    suspend fun getPlaylists(): List<PlaylistDomain>
    suspend fun getPlaylist(id: Identifier<GUID>): PlaylistDomain?
    suspend fun getPlaylistItem(id: Identifier<GUID>): PlaylistItemDomain?
    suspend fun scanUrl(url: String): Domain?
    suspend fun commitPlaylistItem(item: PlaylistItemDomain): PlaylistItemDomain
}