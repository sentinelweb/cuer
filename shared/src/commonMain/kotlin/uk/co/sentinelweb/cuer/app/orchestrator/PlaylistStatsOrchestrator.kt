package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class PlaylistStatsOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
) : OrchestratorContract<PlaylistStatDomain> {
    override val updates: Flow<Triple<Operation, Source, PlaylistStatDomain>>
        get() = TODO()

    suspend override fun loadByPlatformId(platformId: String, options: Options): PlaylistStatDomain? {
        throw NotImplementedException()
    }

    suspend override fun loadByDomain(domain: PlaylistStatDomain, options: Options): PlaylistStatDomain? {
        throw NotImplementedException()
    }

    suspend override fun loadById(id: GUID, options: Options): PlaylistStatDomain? {
        throw NotImplementedException()
    }

    suspend override fun loadList(filter: Filter, options: Options): List<PlaylistStatDomain> =
        when (options.source) {
            Source.MEMORY -> throw NotImplementedException()
            Source.LOCAL -> playlistDatabaseRepository.loadStatsList(filter)
                .allowDatabaseListResultEmpty()

            Source.LOCAL_NETWORK -> throw NotImplementedException()
            Source.REMOTE -> throw NotImplementedException()
            Source.PLATFORM -> throw InvalidOperationException(this::class, filter, options)
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

    override suspend fun delete(id: GUID, options: Options): Boolean {
        throw NotImplementedException()
    }

    override suspend fun delete(domain: PlaylistStatDomain, options: Options): Boolean {
        throw NotImplementedException()
    }

    override suspend fun update(update: UpdateDomain<PlaylistStatDomain>, options: Options): PlaylistStatDomain? {
        throw NotImplementedException()
    }
}