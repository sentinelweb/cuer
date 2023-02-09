package uk.co.sentinelweb.cuer.app.service.remote

import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.usecase.AddLinkUsecase
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
// import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter

class AppRemoteDatabaseAdapter constructor(
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val addLinkUsecase: AddLinkUsecase
) : RemoteDatabaseAdapter {
    override suspend fun getPlaylists(): List<PlaylistDomain> =
        playlistOrchestrator
            .loadList(AllFilter, LOCAL.flatOptions())

    override suspend fun getPlaylist(id: OrchestratorContract.Identifier<GUID>): PlaylistDomain? =
        playlistOrchestrator
            .loadById(id.id, LOCAL.deepOptions())

    override suspend fun getPlaylistItem(id: OrchestratorContract.Identifier<GUID>): PlaylistItemDomain? =
        playlistItemOrchestrator
            .loadById(id.id, LOCAL.flatOptions())

    override suspend fun scanUrl(url: String): Domain = addLinkUsecase.scanUrl(url)

    override suspend fun commitPlaylistItem(item: PlaylistItemDomain) = addLinkUsecase.commitPlaylistItem(item)
}

// fixme remove when :remote module supports 1.7.20
interface RemoteDatabaseAdapter {

    suspend fun getPlaylists(): List<PlaylistDomain>
    suspend fun getPlaylist(id: OrchestratorContract.Identifier<GUID>): PlaylistDomain?
    suspend fun getPlaylistItem(id: OrchestratorContract.Identifier<GUID>): PlaylistItemDomain?
    suspend fun scanUrl(url: String): Domain?
    suspend fun commitPlaylistItem(item: PlaylistItemDomain): PlaylistItemDomain
}