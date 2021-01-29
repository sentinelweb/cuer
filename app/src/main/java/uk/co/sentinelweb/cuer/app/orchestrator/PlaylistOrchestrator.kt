package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class PlaylistOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<PlaylistDomain> {

    override val updates: Flow<Pair<OrchestratorContract.Operation, PlaylistDomain>>
        get() = playlistDatabaseRepository.playlistFlow

    suspend override fun load(id: Long, options: OrchestratorContract.Options): PlaylistDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun loadList(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): List<PlaylistDomain>? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> TODO()
            OrchestratorContract.Source.LOCAL -> playlistDatabaseRepository.loadList(filter)
                .takeIf { it.isSuccessful && (it.data?.size ?: 0) > 0 }
                ?.data
            OrchestratorContract.Source.LOCAL_NETWORK -> TODO()
            OrchestratorContract.Source.REMOTE -> TODO()
            OrchestratorContract.Source.PLATFORM -> throw OrchestratorContract.InvalidOperationException(this::class, filter, options)
        }

    suspend override fun load(platformId: String, options: OrchestratorContract.Options): PlaylistDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun load(domain: PlaylistDomain, options: OrchestratorContract.Options): PlaylistDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun save(domain: PlaylistDomain, options: OrchestratorContract.Options): PlaylistDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun save(domains: List<PlaylistDomain>, options: OrchestratorContract.Options): PlaylistDomain? {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): Int {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: PlaylistDomain, options: OrchestratorContract.Options): Boolean {
        TODO("Not yet implemented")
    }

}