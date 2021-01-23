package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class PlaylistStatsOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
) : OrchestratorContract<PlaylistStatDomain> {
    override val updates: Flow<Pair<OrchestratorContract.Operation, PlaylistStatDomain>>
        get() = playlistDatabaseRepository.playlistStatFlow

    suspend override fun load(platformId: String, options: OrchestratorContract.Options): PlaylistStatDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun load(domain: PlaylistStatDomain, options: OrchestratorContract.Options): PlaylistStatDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun load(id: Long, options: OrchestratorContract.Options): PlaylistStatDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun loadList(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): List<PlaylistStatDomain>? {
        TODO("Not yet implemented")
    }

    suspend override fun save(domain: PlaylistStatDomain, options: OrchestratorContract.Options): PlaylistStatDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun save(domains: List<PlaylistStatDomain>, options: OrchestratorContract.Options): PlaylistStatDomain? {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): Int {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: PlaylistStatDomain, options: OrchestratorContract.Options): Boolean {
        TODO("Not yet implemented")
    }
}