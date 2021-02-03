package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

class MediaOrchestrator constructor(
    private val mediaDatabaseRepository: MediaDatabaseRepository,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<MediaDomain> {

    override val updates: Flow<Triple<Operation, Source, MediaDomain>>
        get() = throw OrchestratorContract.NotImplementedException()

    suspend override fun load(id: Long, options: Options): MediaDomain? {
        throw OrchestratorContract.NotImplementedException()
    }

    suspend override fun loadList(filter: Filter, options: Options): List<MediaDomain> =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL -> mediaDatabaseRepository.loadList(filter, options.flat)
                .forceDatabaseListResultNotEmpty("Media $filter does not exist")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> when (filter) {
                is PlatformIdListFilter ->
                    ytInteractor.videos(filter.ids, listOf(ID, SNIPPET, CONTENT_DETAILS, LIVE_BROADCAST_DETAILS))
                        .forceNetListResultNotEmpty("Youtube ${filter.ids} does not exist")
                else -> throw InvalidOperationException(this::class, filter, options)
            }
        }


    suspend override fun load(platformId: String, options: Options): MediaDomain? =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL -> TODO()
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM ->
                ytInteractor.videos(listOf(platformId), listOf(ID, SNIPPET, CONTENT_DETAILS, LIVE_BROADCAST_DETAILS))
                    .forceNetListResultNotEmpty("Youtube $platformId does not exist")
                    .get(0)
        }

    suspend override fun load(domain: MediaDomain, options: Options): MediaDomain? {
        throw NotImplementedException()
    }

    suspend override fun save(domain: MediaDomain, options: Options): MediaDomain =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL -> mediaDatabaseRepository.save(domain, options.flat)
                .forceDatabaseSuccessNotNull("Save failed $domain")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> TODO()
        }


    suspend override fun save(domains: List<MediaDomain>, options: Options): List<MediaDomain> {
        throw NotImplementedException()
    }

    override suspend fun count(filter: Filter, options: Options): Int {
        throw NotImplementedException()
    }

    override suspend fun delete(domain: MediaDomain, options: Options): Boolean {
        throw NotImplementedException()
    }


}