package uk.co.sentinelweb.cuer.app.db.repository

interface DatabaseRepository<Domain> {

    suspend fun save(domain: Domain): Result<Boolean>

    suspend fun save(domains: List<Domain>): Result<Boolean>

    suspend fun load(id: Int): Result<Domain>

    suspend fun loadList(filter: Filter? = null): Result<List<Domain>>

    suspend fun count(filter: Filter? = null): Result<Int>

    suspend fun delete(domain: Domain): Result<Boolean>

    interface Filter
}