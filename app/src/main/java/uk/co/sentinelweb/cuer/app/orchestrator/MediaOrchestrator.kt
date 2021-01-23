package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

class MediaOrchestrator constructor(
    private val mediaDatabaseRepository: MediaDatabaseRepository,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<MediaDomain> {

    override val updates: Flow<Pair<OrchestratorContract.Operation, MediaDomain>>
        get() = TODO("Not yet implemented")

    suspend override fun load(id: Long, options: OrchestratorContract.Options): MediaDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun loadList(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): List<MediaDomain>? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> TODO()
            OrchestratorContract.Source.LOCAL -> TODO()
            OrchestratorContract.Source.LOCAL_NETWORK -> TODO()
            OrchestratorContract.Source.REMOTE -> TODO()
            OrchestratorContract.Source.PLATFORM -> when (filter) {
                is OrchestratorContract.PlatformIdListFilter ->
                    ytInteractor.videos(filter.ids, listOf(ID, SNIPPET, CONTENT_DETAILS, LIVE_BROADCAST_DETAILS))
                        .takeIf { it.isSuccessful && (it.data?.size ?: 0) > 0 }
                        ?.data
                else -> throw OrchestratorContract.InvalidOperationException(this::class, filter, options)
            }
        }


    suspend override fun load(platformId: String, options: OrchestratorContract.Options): MediaDomain? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> TODO()
            OrchestratorContract.Source.LOCAL -> TODO()
            OrchestratorContract.Source.LOCAL_NETWORK -> TODO()
            OrchestratorContract.Source.REMOTE -> TODO()
            OrchestratorContract.Source.PLATFORM ->
                ytInteractor.videos(listOf(platformId), listOf(ID, SNIPPET, CONTENT_DETAILS, LIVE_BROADCAST_DETAILS))
                    .takeIf { it.isSuccessful && (it.data?.size ?: 0) > 0 }
                    ?.data?.get(0)
                    ?: throw OrchestratorContract.DoesNotExistException("Youtube $platformId does not exist")
        }


    suspend override fun load(domain: MediaDomain, options: OrchestratorContract.Options): MediaDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun save(domain: MediaDomain, options: OrchestratorContract.Options): MediaDomain? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> TODO()
            OrchestratorContract.Source.LOCAL -> mediaDatabaseRepository.save(domain, options.flat)
                .takeIf { it.isSuccessful }
                ?.data
            OrchestratorContract.Source.LOCAL_NETWORK -> TODO()
            OrchestratorContract.Source.REMOTE -> TODO()
            OrchestratorContract.Source.PLATFORM -> TODO()
        }


    suspend override fun save(domains: List<MediaDomain>, options: OrchestratorContract.Options): MediaDomain? {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): Int {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: MediaDomain, options: OrchestratorContract.Options): Boolean {
        TODO("Not yet implemented")
    }


}