package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

interface DatabaseRepository<Domain, Stats> {

    val updates: Flow<Pair<Operation, Domain>>
    val stats: Flow<Pair<Operation, Stats>>

    suspend fun save(domain: Domain, flat: Boolean, emit: Boolean): DbResult<Domain>

    suspend fun save(domains: List<Domain>, flat: Boolean, emit: Boolean): DbResult<List<Domain>>

    suspend fun load(id: GUID, flat: Boolean): DbResult<Domain>

    suspend fun loadList(filter: Filter, flat: Boolean): DbResult<List<Domain>>

    suspend fun loadStatsList(filter: Filter): DbResult<List<Stats>>

    suspend fun count(filter: Filter): DbResult<Int>

    suspend fun delete(domain: Domain, emit: Boolean): DbResult<Boolean>

    suspend fun deleteAll(): DbResult<Boolean>

    suspend fun update(update: UpdateDomain<Domain>, flat: Boolean, emit: Boolean): DbResult<Domain>
}

interface ChannelDatabaseRepository : DatabaseRepository<ChannelDomain, Nothing>
interface MediaDatabaseRepository : DatabaseRepository<MediaDomain, Nothing>
interface PlaylistDatabaseRepository : DatabaseRepository<PlaylistDomain, PlaylistStatDomain>
interface PlaylistItemDatabaseRepository : DatabaseRepository<PlaylistItemDomain, Nothing>
interface ImageDatabaseRepository : DatabaseRepository<ImageDomain, Nothing>

class ConflictException(msg: String) : Exception(msg)