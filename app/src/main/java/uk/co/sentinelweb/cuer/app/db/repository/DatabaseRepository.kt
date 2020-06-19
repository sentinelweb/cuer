package uk.co.sentinelweb.cuer.app.db.repository

interface DatabaseRepository<Domain> {

    suspend fun save(domain: Domain): RepoResult<Boolean>

    suspend fun save(domains: List<Domain>): RepoResult<Boolean>

    suspend fun load(id: Int): RepoResult<Domain>

    suspend fun loadList(filter: Filter? = null): RepoResult<List<Domain>>

    suspend fun count(filter: Filter? = null): RepoResult<Int>

    suspend fun delete(domain: Domain): RepoResult<Boolean>

    suspend fun deleteAll(): RepoResult<Boolean>

    interface Filter
}