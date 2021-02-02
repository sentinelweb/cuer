package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistItemOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val log: LogWrapper
) : OrchestratorContract<PlaylistItemDomain> {

    override val updates: Flow<Triple<Operation, Source, PlaylistItemDomain>>
        get() = playlistDatabaseRepository.playlistItemFlow
            .map { it.first to Source.LOCAL then it.second }

    suspend override fun load(id: Long, options: OrchestratorContract.Options): PlaylistItemDomain? =
        when (options.source) {
            Source.MEMORY -> TODO()
            Source.LOCAL -> (playlistDatabaseRepository.loadPlaylistItem(id)
                .takeIf { it.isSuccessful }
                ?: throw OrchestratorContract.DoesNotExistException("PlaylistItemDomain($id)"))
                .data

            Source.LOCAL_NETWORK -> TODO()
            Source.REMOTE -> TODO()
            Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, null, options)
        }

    suspend override fun loadList(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): List<PlaylistItemDomain> =
        when (options.source) {
            Source.MEMORY -> TODO()
            Source.LOCAL -> playlistDatabaseRepository.loadPlaylistItems(filter)
                .forceDatabaseListResultNotEmpty("Playlist item $filter does not exist")
            Source.LOCAL_NETWORK -> TODO()
            Source.REMOTE -> TODO()
            Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, filter, options)
        }

    suspend override fun load(platformId: String, options: OrchestratorContract.Options): PlaylistItemDomain? {
        throw OrchestratorContract.InvalidOperationException(this::class, null, options)
    }

    suspend override fun load(domain: PlaylistItemDomain, options: OrchestratorContract.Options): PlaylistItemDomain? =
        when (options.source) {
            Source.MEMORY -> TODO()
            Source.LOCAL -> domain.id?.let {
                playlistDatabaseRepository.loadPlaylistItem(it)
                    .allowDatabaseFail()
            }

            Source.LOCAL_NETWORK -> TODO()
            Source.REMOTE -> TODO()
            Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domain: PlaylistItemDomain, options: OrchestratorContract.Options): PlaylistItemDomain =
        when (options.source) {
            Source.MEMORY -> TODO()
            Source.LOCAL -> playlistDatabaseRepository.savePlaylistItem(domain, options.flat)
                .forceDatabaseSuccess("Svae failed $domain")
            Source.LOCAL_NETWORK -> TODO()
            Source.REMOTE -> TODO()
            Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domains: List<PlaylistItemDomain>, options: OrchestratorContract.Options): List<PlaylistItemDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): Int {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: PlaylistItemDomain, options: OrchestratorContract.Options): Boolean =
        when (options.source) {
            Source.MEMORY -> TODO()
            Source.LOCAL -> playlistDatabaseRepository.delete(domain, options.emit)
                .takeIf { it.isSuccessful }
                ?.data ?: throw IllegalStateException("Delete result is null")
            Source.LOCAL_NETWORK -> TODO()
            Source.REMOTE -> TODO()
            Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, null, options)
        }

}