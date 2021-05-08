package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.update.UpdateObject

interface DatabaseRepository<Domain> {

    val updates: Flow<Pair<OrchestratorContract.Operation, Domain>>
    // todo flat: Boolean = true, emit: Boolean = true - for all and check - to match orchestrator
    suspend fun save(domain: Domain, flat: Boolean = false, emit: Boolean = false): RepoResult<Domain>

    suspend fun save(domains: List<Domain>, flat: Boolean = false, emit: Boolean = false): RepoResult<List<Domain>>

    suspend fun load(id: Long, flat: Boolean = false): RepoResult<Domain>

    suspend fun loadList(filter: OrchestratorContract.Filter? = null, flat: Boolean = false): RepoResult<List<Domain>>// todo nonnull filter

    suspend fun count(filter: OrchestratorContract.Filter? = null): RepoResult<Int>// todo nonnull filter

    suspend fun delete(domain: Domain, emit: Boolean = false): RepoResult<Boolean>

    suspend fun deleteAll(): RepoResult<Boolean>

    suspend fun update(update: UpdateObject<Domain>, flat: Boolean = false, emit: Boolean = false): RepoResult<Domain>
}