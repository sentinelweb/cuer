package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class PlaylistItemOrchestrator constructor(
    private val playlistItemDatabaseRepository: PlaylistItemDatabaseRepository,
    private val log: LogWrapper
) : OrchestratorContract<PlaylistItemDomain>, KoinComponent {
    // to stop a circular referecnce
    private val playlistItemMemoryRepository: PlaylistMemoryRepository.PlayListItemMemoryRepository by inject()

    override val updates: Flow<Triple<Operation, Source, PlaylistItemDomain>>
        get() = playlistItemDatabaseRepository.updates
            .map { it.first to LOCAL then it.second }

    suspend override fun loadById(id: Long, options: Options): PlaylistItemDomain? =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL -> (playlistItemDatabaseRepository.load(id, options.flat)
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
            LOCAL -> playlistItemDatabaseRepository.loadList(filter, options.flat)
                .allowDatabaseListResultEmpty()

            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, filter, options)
        }

    suspend override fun loadByPlatformId(platformId: String, options: Options): PlaylistItemDomain? {
        throw InvalidOperationException(this::class, null, options)
    }

    suspend override fun loadByDomain(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain? =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL -> domain.id?.let {
                playlistItemDatabaseRepository.load(it, options.flat)
                    .forceDatabaseSuccess()
            }

            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain =
        when (options.source) {
            MEMORY -> playlistItemMemoryRepository.save(domain, options)
            LOCAL -> playlistItemDatabaseRepository.save(domain, emit = options.emit, flat = options.flat)
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

            LOCAL -> playlistItemDatabaseRepository.save(domains, flat = options.flat, emit = options.emit)
                .forceDatabaseSuccessNotNull("Save failed $domains")

            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun count(filter: Filter, options: Options): Int =
        when (options.source) {
            MEMORY -> playlistItemMemoryRepository.count(filter, options)
            LOCAL -> playlistItemDatabaseRepository.count(filter)
                .forceDatabaseSuccessNotNull("Count failed $filter")

            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun delete(domain: PlaylistItemDomain, options: Options): Boolean =
        when (options.source) {
            MEMORY -> playlistItemMemoryRepository.delete(domain, options)
            LOCAL -> playlistItemDatabaseRepository.delete(domain, options.emit)
                .forceDatabaseSuccessNotNull("Delete failed $domain")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun update(update: UpdateDomain<PlaylistItemDomain>, options: Options): PlaylistItemDomain? {
        TODO("Not yet implemented")
    }
}