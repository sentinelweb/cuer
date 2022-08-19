package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightMediaDatabaseRepository (
    private val database: Database,
    private val imageDatabaseRepository: SqldelightImageDatabaseRepository,
    private val channelDatabaseRepository: SqldelightChannelDatabaseRepository,
    private val mediaMapper: MediaMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
) : MediaDatabaseRepository {

    init {
        log.tag(this)
    }

    val _updatesFlow = MutableSharedFlow<Pair<OrchestratorContract.Operation, MediaDomain>>()
    override val updates: Flow<Pair<OrchestratorContract.Operation,MediaDomain>>
        get() = _updatesFlow

    override val stats: Flow<Pair<OrchestratorContract.Operation, Nothing>>
        get() = TODO("Not yet implemented")

    override suspend fun save(domain: MediaDomain, flat: Boolean, emit: Boolean): RepoResult<MediaDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun save(domains: List<MediaDomain>, flat: Boolean, emit: Boolean): RepoResult<List<MediaDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<MediaDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun loadList(filter: OrchestratorContract.Filter?, flat: Boolean): RepoResult<List<MediaDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadStatsList(filter: OrchestratorContract.Filter?): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter?): RepoResult<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: MediaDomain, emit: Boolean): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun update(
        update: UpdateDomain<MediaDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<MediaDomain> {
        TODO("Not yet implemented")
    }

}