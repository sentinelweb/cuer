package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.MemoryRepository
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class PlaylistItemOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val playlistItemMemoryRepository: MemoryRepository<PlaylistItemDomain>,
    private val log: LogWrapper
) : OrchestratorContract<PlaylistItemDomain> {

    override val updates: Flow<Triple<Operation, Source, PlaylistItemDomain>>
        get() = playlistDatabaseRepository.playlistItemFlow
            .map { it.first to LOCAL then it.second }

    suspend override fun load(id: Long, options: Options): PlaylistItemDomain? =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL -> (playlistDatabaseRepository.loadPlaylistItem(id)
                .takeIf { it.isSuccessful }
                ?: throw DoesNotExistException("PlaylistItemDomain($id)"))
                .data

            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    suspend override fun loadList(filter: Filter, options: Options): List<PlaylistItemDomain> =
        when (options.source) {
            MEMORY -> playlistItemMemoryRepository.loadList(filter, options)
            LOCAL -> playlistDatabaseRepository.loadPlaylistItems(filter)
                .allowDatabaseListResultEmpty()
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, filter, options)
        }

    suspend override fun load(platformId: String, options: Options): PlaylistItemDomain? {
        throw InvalidOperationException(this::class, null, options)
    }

    suspend override fun load(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain? =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL -> domain.id?.let {
                playlistDatabaseRepository.loadPlaylistItem(it)
                    .forceDatabaseSuccess()
            }

            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain =
        when (options.source) {
            MEMORY -> playlistItemMemoryRepository.save(domain, options)
            LOCAL -> playlistDatabaseRepository.savePlaylistItem(domain, options.emit, options.flat)
                .forceDatabaseSuccessNotNull("Save failed $domain")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domains: List<PlaylistItemDomain>, options: Options): List<PlaylistItemDomain> =
        when (options.source) {
            MEMORY -> domains.map {
                playlistItemMemoryRepository.save(it, options)
            }
            LOCAL -> playlistDatabaseRepository.savePlaylistItems(domains, options.emit)
                .forceDatabaseSuccessNotNull("Save failed $domains")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }


    override suspend fun count(filter: Filter, options: Options): Int =
        when (options.source) {
            MEMORY -> playlistItemMemoryRepository.count(filter, options)
            LOCAL -> playlistDatabaseRepository.count(filter)
                .forceDatabaseSuccessNotNull("Count failed $filter")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun delete(domain: PlaylistItemDomain, options: Options): Boolean =
        when (options.source) {
            MEMORY -> playlistItemMemoryRepository.delete(domain, options)
            LOCAL -> playlistDatabaseRepository.delete(domain, options.emit)
                .forceDatabaseSuccessNotNull("Delete failed $domain")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun update(update: UpdateDomain<PlaylistItemDomain>, options: Options): PlaylistItemDomain? {
        TODO("Not yet implemented")
    }

}