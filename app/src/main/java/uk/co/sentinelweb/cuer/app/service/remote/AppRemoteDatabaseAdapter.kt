package uk.co.sentinelweb.cuer.app.service.remote

import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter

class AppRemoteDatabaseAdapter constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository
) : RemoteDatabaseAdapter {
    override suspend fun getPlaylists(): List<PlaylistDomain> =
        playlistDatabaseRepository
            .loadList(OrchestratorContract.AllFilter(), flat = true)
            .takeIf { it.isSuccessful }
            ?.data
            ?: listOf()

    override suspend fun getPlaylist(id: Long): PlaylistDomain? =
        playlistDatabaseRepository.load(id, flat = false)
            .takeIf { it.isSuccessful }
            ?.data

    override suspend fun getPlaylistItem(id: Long): PlaylistItemDomain? =
        playlistDatabaseRepository.loadPlaylistItem(id)
            .takeIf { it.isSuccessful }
            ?.data
}