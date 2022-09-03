package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.db.update.MediaUpdateMapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightPlaylistItemDatabaseRepository(
    private val database: Database,
    private val mediaDatabaseRepository: SqldelightChannelDatabaseRepository,
    private val playlistItemMapper: PlaylistItemMapper,
    private val mediaUpdateMapper: MediaUpdateMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
) : PlaylistItemDatabaseRepository {

    init {
        log.tag(this)
    }

    val _updatesFlow = MutableSharedFlow<Pair<OrchestratorContract.Operation, PlaylistItemDomain>>()
    override val updates: Flow<Pair<OrchestratorContract.Operation, PlaylistItemDomain>>
        get() = _updatesFlow

    override val stats: Flow<Pair<OrchestratorContract.Operation, Nothing>>
        get() = TODO("Not yet implemented")

    override suspend fun save(
        domain: PlaylistItemDomain,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<PlaylistItemDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun save(
        domains: List<PlaylistItemDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<List<PlaylistItemDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<PlaylistItemDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun loadList(
        filter: OrchestratorContract.Filter?,
        flat: Boolean
    ): RepoResult<List<PlaylistItemDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadStatsList(filter: OrchestratorContract.Filter?): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter?): RepoResult<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: PlaylistItemDomain, emit: Boolean): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun update(
        update: UpdateDomain<PlaylistItemDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<PlaylistItemDomain> {
        TODO("Not yet implemented")
    }

}