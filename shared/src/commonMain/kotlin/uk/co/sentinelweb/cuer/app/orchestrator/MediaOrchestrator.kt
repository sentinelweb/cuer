package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

class MediaOrchestrator constructor(
    private val mediaDatabaseRepository: MediaDatabaseRepository,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<MediaDomain>, KoinComponent {
    private val mediaMemoryRepository: PlaylistMemoryRepository.MediaMemoryRepository by inject()

    override val updates: Flow<Triple<Operation, Source, MediaDomain>>
        get() = mediaDatabaseRepository.updates
            .map { it.first to LOCAL then it.second }

    suspend override fun loadById(id: GUID, options: Options): MediaDomain? =
        when (options.source) {
            MEMORY -> throw NotImplementedException()
            LOCAL -> mediaDatabaseRepository
                .load(id, options.flat)
                .forceDatabaseSuccess()

            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)// no platform id available
        }

    suspend override fun loadList(filter: Filter, options: Options): List<MediaDomain> =
        when (options.source) {
            MEMORY -> throw NotImplementedException()
            LOCAL -> mediaDatabaseRepository
                .loadList(filter, options.flat)
                .allowDatabaseListResultEmpty()

            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> when (filter) {
                is PlatformIdListFilter ->
                    ytInteractor
                        .videos(filter.ids, listOf(ID, SNIPPET, CONTENT_DETAILS, LIVE_BROADCAST_DETAILS))
                        .forceNetListResultNotEmpty("Youtube ${filter.ids} does not exist")

                else -> throw InvalidOperationException(this::class, filter, options)
            }
        }

    suspend override fun loadByPlatformId(platformId: String, options: Options): MediaDomain? =
        when (options.source) {
            MEMORY -> throw NotImplementedException()
            LOCAL -> mediaDatabaseRepository
                .loadList(PlatformIdListFilter(listOf(platformId)), options.flat)
                .takeIf { it.isSuccessful && it.data?.size == 1 }
                ?.data
                ?.get(0)

            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM ->
                ytInteractor
                    .videos(listOf(platformId), listOf(ID, SNIPPET, CONTENT_DETAILS, LIVE_BROADCAST_DETAILS))
                    .forceNetListResultNotEmpty("Youtube $platformId does not exist")
                    .get(0)
        }

    suspend override fun loadByDomain(domain: MediaDomain, options: Options): MediaDomain? {
        throw NotImplementedException()
    }

    suspend override fun save(domain: MediaDomain, options: Options): MediaDomain =
        when (options.source) {
            MEMORY -> mediaMemoryRepository.save(domain, options)
            LOCAL -> mediaDatabaseRepository
                .save(domain, options.flat, options.emit)
                .forceDatabaseSuccessNotNull("Save failed $domain")

            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw NotImplementedException()
        }


    suspend override fun save(domains: List<MediaDomain>, options: Options): List<MediaDomain> =
        when (options.source) {
            MEMORY -> mediaMemoryRepository.save(domains, options)
            LOCAL -> mediaDatabaseRepository
                .save(domains, options.flat, options.emit)
                .forceDatabaseSuccessNotNull("Save failed ${domains.map { it.platformId }}")

            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw NotImplementedException()
        }

    override suspend fun count(filter: Filter, options: Options): Int =
        when (options.source) {
            MEMORY -> mediaMemoryRepository.count(filter, options)
            LOCAL ->
                mediaDatabaseRepository.count(filter)
                    .forceDatabaseSuccessNotNull("Count failed $filter")

            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }


    override suspend fun delete(domain: MediaDomain, options: Options): Boolean {
        throw NotImplementedException()
    }

    override suspend fun update(update: UpdateDomain<MediaDomain>, options: Options): MediaDomain =
        when (options.source) {
            MEMORY -> throw NotImplementedException()
            LOCAL -> mediaDatabaseRepository
                .update(update, options.flat, options.emit)
                .forceDatabaseSuccessNotNull("Update failed: ${update}")

            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw NotImplementedException()
        }
}
