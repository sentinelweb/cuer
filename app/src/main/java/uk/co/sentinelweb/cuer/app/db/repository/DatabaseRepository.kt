package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract

interface DatabaseRepository<Domain> {

    val updates: Flow<Pair<OrchestratorContract.Operation, Domain>>

    suspend fun save(domain: Domain, flat: Boolean = false, emit: Boolean = false): RepoResult<Domain>

    suspend fun save(domains: List<Domain>, flat: Boolean = false, emit: Boolean = false): RepoResult<List<Domain>>

    suspend fun load(id: Long, flat: Boolean = false): RepoResult<Domain>

    suspend fun loadList(filter: OrchestratorContract.Filter? = null, flat: Boolean = false): RepoResult<List<Domain>>// todo nonnull filter

    suspend fun count(filter: OrchestratorContract.Filter? = null): RepoResult<Int>// todo nonnull filter

    suspend fun delete(domain: Domain, emit: Boolean = false): RepoResult<Boolean>

    suspend fun deleteAll(): RepoResult<Boolean>
}