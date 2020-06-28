package uk.co.sentinelweb.cuer.app.db.repository

interface DatabaseRepository<Domain> {

    suspend fun save(domain: Domain, flat: Boolean = false): RepoResult<Domain>

    suspend fun save(domains: List<Domain>, flat: Boolean = false): RepoResult<List<Domain>>

    suspend fun load(id: Long, flat: Boolean = false): RepoResult<Domain>

    suspend fun loadList(filter: Filter? = null): RepoResult<List<Domain>>

    suspend fun count(filter: Filter? = null): RepoResult<Int>

    suspend fun delete(domain: Domain): RepoResult<Boolean>

    suspend fun deleteAll(): RepoResult<Boolean>

    interface Filter
}