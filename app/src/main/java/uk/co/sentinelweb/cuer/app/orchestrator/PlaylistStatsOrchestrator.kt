package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class PlaylistStatsOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
) : OrchestratorContract<PlaylistStatDomain> {
    override val updates: Flow<Triple<Operation, Source, PlaylistStatDomain>>
        get() = playlistDatabaseRepository.playlistStatFlow
            .map { it.first to Source.LOCAL then it.second }

    suspend override fun load(platformId: String, options: Options): PlaylistStatDomain? {
        throw NotImplementedException()
    }

    suspend override fun load(domain: PlaylistStatDomain, options: Options): PlaylistStatDomain? {
        throw NotImplementedException()
    }

    suspend override fun load(id: Long, options: Options): PlaylistStatDomain? {
        throw NotImplementedException()
    }

    suspend override fun loadList(filter: Filter, options: Options): List<PlaylistStatDomain> {
        throw NotImplementedException()
    }

    suspend override fun save(domain: PlaylistStatDomain, options: Options): PlaylistStatDomain {
        throw NotImplementedException()
    }

    suspend override fun save(domains: List<PlaylistStatDomain>, options: Options): List<PlaylistStatDomain> {
        throw NotImplementedException()
    }

    override suspend fun count(filter: Filter, options: Options): Int {
        throw NotImplementedException()
    }

    override suspend fun delete(domain: PlaylistStatDomain, options: Options): Boolean {
        throw NotImplementedException()
    }
}