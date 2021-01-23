package uk.co.sentinelweb.cuer.app.db.repository

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract

interface DatabaseRepository<Domain> {

    suspend fun save(domain: Domain, flat: Boolean = false): RepoResult<Domain>

    suspend fun save(domains: List<Domain>, flat: Boolean = false): RepoResult<List<Domain>>

    suspend fun load(id: Long, flat: Boolean = false): RepoResult<Domain>

    suspend fun loadList(filter: OrchestratorContract.Filter? = null, flat: Boolean = false): RepoResult<List<Domain>>// todo nonnull filter

    suspend fun count(filter: OrchestratorContract.Filter? = null): RepoResult<Int>// todo nonnull filter

    suspend fun delete(domain: Domain): RepoResult<Boolean>

    suspend fun deleteAll(): RepoResult<Boolean>

}