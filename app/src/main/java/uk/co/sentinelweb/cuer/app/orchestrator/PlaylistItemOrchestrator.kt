package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistItemOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val log: LogWrapper
) : OrchestratorContract<PlaylistItemDomain> {

    override val updates: Flow<Pair<OrchestratorContract.Operation, PlaylistItemDomain>>
        get() = playlistDatabaseRepository.playlistItemFlow

    suspend override fun load(id: Long, options: OrchestratorContract.Options): PlaylistItemDomain? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> TODO()
            OrchestratorContract.Source.LOCAL -> (playlistDatabaseRepository.loadPlaylistItem(id)
                .takeIf { it.isSuccessful }
                ?: throw OrchestratorContract.DoesNotExistException("PlaylistItemDomain($id)"))
                .data

            OrchestratorContract.Source.LOCAL_NETWORK -> TODO()
            OrchestratorContract.Source.REMOTE -> TODO()
            OrchestratorContract.Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, null, options)
        }

    suspend override fun loadList(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): List<PlaylistItemDomain>? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> TODO()
            OrchestratorContract.Source.LOCAL -> playlistDatabaseRepository.loadPlaylistItems(filter)
                .takeIf { it.isSuccessful && (it.data?.size ?: 0) > 0 }
                ?.data
            OrchestratorContract.Source.LOCAL_NETWORK -> TODO()
            OrchestratorContract.Source.REMOTE -> TODO()
            OrchestratorContract.Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, filter, options)
        }

    suspend override fun load(platformId: String, options: OrchestratorContract.Options): PlaylistItemDomain? {
        throw OrchestratorContract.InvalidOperationException(this::class, null, options)
    }

    suspend override fun load(domain: PlaylistItemDomain, options: OrchestratorContract.Options): PlaylistItemDomain? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> TODO()
            OrchestratorContract.Source.LOCAL -> domain.id?.let {
                playlistDatabaseRepository.loadPlaylistItem(it)
                    .takeIf { it.isSuccessful }
                    ?.data
            }
                ?: throw OrchestratorContract.DoesNotExistException("PlaylistItemDomain($domain?.id)")

            OrchestratorContract.Source.LOCAL_NETWORK -> TODO()
            OrchestratorContract.Source.REMOTE -> TODO()
            OrchestratorContract.Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domain: PlaylistItemDomain, options: OrchestratorContract.Options): PlaylistItemDomain? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> TODO()
            OrchestratorContract.Source.LOCAL -> playlistDatabaseRepository.savePlaylistItem(domain, options.flat)
                .takeIf { it.isSuccessful }
                ?.data
            OrchestratorContract.Source.LOCAL_NETWORK -> TODO()
            OrchestratorContract.Source.REMOTE -> TODO()
            OrchestratorContract.Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domains: List<PlaylistItemDomain>, options: OrchestratorContract.Options): PlaylistItemDomain? {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): Int {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: PlaylistItemDomain, options: OrchestratorContract.Options): Boolean =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> TODO()
            OrchestratorContract.Source.LOCAL -> playlistDatabaseRepository.delete(domain, options.emit)
                .takeIf { it.isSuccessful }
                ?.data ?: throw IllegalStateException("Delete result is null")
            OrchestratorContract.Source.LOCAL_NETWORK -> TODO()
            OrchestratorContract.Source.REMOTE -> TODO()
            OrchestratorContract.Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, null, options)
        }

}