package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class PlaylistOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<PlaylistDomain>, KoinComponent {

    private val playlistMemoryRepository: PlaylistMemoryRepository by inject()

    override val updates: Flow<Triple<Operation, Source, PlaylistDomain>>
        get() = merge(
            playlistDatabaseRepository.updates
                .map { it.first to LOCAL then it.second },
            (playlistMemoryRepository.updates
                .map { it.first to MEMORY then it.second })
        )

    suspend override fun loadById(id: Long, options: Options): PlaylistDomain? = when (options.source) {
        MEMORY -> playlistMemoryRepository.load(id, options)
        LOCAL -> playlistDatabaseRepository.load(id, options.flat)
            .forceDatabaseSuccess()

        LOCAL_NETWORK -> throw NotImplementedException()
        REMOTE -> throw NotImplementedException()
        PLATFORM -> throw InvalidOperationException(this::class, null, options)
    }

    suspend override fun loadList(filter: Filter, options: Options): List<PlaylistDomain> =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.loadList(filter, options)
            LOCAL -> playlistDatabaseRepository.loadList(filter, options.flat)
                .allowDatabaseListResultEmpty()
            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, filter, options)
        }

    suspend override fun loadByPlatformId(platformId: String, options: Options): PlaylistDomain? =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.loadList(PlatformIdListFilter(listOf(platformId)), options)
                .firstOrNull()

            LOCAL -> playlistDatabaseRepository.loadList(PlatformIdListFilter(listOf(platformId)), options.flat)
                .allowDatabaseListResultEmpty()
                .firstOrNull()

            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> ytInteractor.playlist(platformId)
                .also { if (options.flat) throw NotImplementedException() }
                .forceNetSuccessNotNull("Youtube ${platformId} does not exist")
        }

    suspend override fun loadByDomain(domain: PlaylistDomain, options: Options): PlaylistDomain? {
        throw NotImplementedException()
    }

    suspend override fun save(domain: PlaylistDomain, options: Options): PlaylistDomain =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.save(domain, options)
            LOCAL ->
                playlistDatabaseRepository.save(domain, options.flat, options.emit)
                    .forceDatabaseSuccessNotNull("Save failed ${domain.id}")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domains: List<PlaylistDomain>, options: Options): List<PlaylistDomain> =
        when (options.source) {
            MEMORY -> throw NotImplementedException()
            LOCAL ->
                playlistDatabaseRepository.save(domains, options.flat, options.emit)
                    .forceDatabaseListResultNotEmpty("Save failed ${domains.map { it.id }}")
            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }


    override suspend fun count(filter: Filter, options: Options): Int =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.count(filter, options)
            LOCAL ->
                playlistDatabaseRepository.count(filter)
                    .forceDatabaseSuccessNotNull("Count failed $filter")
            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun delete(domain: PlaylistDomain, options: Options): Boolean =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.delete(domain, options)
            LOCAL ->
                playlistDatabaseRepository.delete(domain, options.emit)
                    .forceDatabaseSuccessNotNull("Delete failed ${domain.id}")
            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun update(update: UpdateDomain<PlaylistDomain>, options: Options): PlaylistDomain? {
        throw NotImplementedException()
    }

}