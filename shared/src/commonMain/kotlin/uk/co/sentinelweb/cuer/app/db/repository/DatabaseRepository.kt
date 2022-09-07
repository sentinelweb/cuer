package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

interface DatabaseRepository<Domain, Stats> {

    val updates: Flow<Pair<Operation, Domain>>
    val stats: Flow<Pair<Operation, Stats>>

    // todo flat: Boolean = true, emit: Boolean = true - for all and check - to match orchestrator
    suspend fun save(domain: Domain, flat: Boolean = false, emit: Boolean = false): RepoResult<Domain>

    suspend fun save(domains: List<Domain>, flat: Boolean = false, emit: Boolean = false): RepoResult<List<Domain>>

    suspend fun load(id: Long, flat: Boolean = false): RepoResult<Domain>

    suspend fun loadList(filter: Filter? = null, flat: Boolean = false): RepoResult<List<Domain>>// todo nonnull filter

    suspend fun loadStatsList(filter: Filter? = null): RepoResult<List<Stats>>// todo nonnull filter

    suspend fun count(filter: Filter? = null): RepoResult<Int>// todo nonnull filter

    suspend fun delete(domain: Domain, emit: Boolean = false): RepoResult<Boolean>

    suspend fun deleteAll(): RepoResult<Boolean>

    suspend fun update(update: UpdateDomain<Domain>, flat: Boolean = false, emit: Boolean = false): RepoResult<Domain>
}

interface ChannelDatabaseRepository : DatabaseRepository<ChannelDomain, Nothing>
interface MediaDatabaseRepository : DatabaseRepository<MediaDomain, Nothing>
interface PlaylistDatabaseRepository : DatabaseRepository<PlaylistDomain, PlaylistStatDomain>
interface PlaylistItemDatabaseRepository : DatabaseRepository<PlaylistItemDomain, Nothing>
interface ImageDatabaseRepository : DatabaseRepository<ImageDomain, Nothing>