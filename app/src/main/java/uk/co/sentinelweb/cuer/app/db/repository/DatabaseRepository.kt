package uk.co.sentinelweb.cuer.app.db.repository

interface DatabaseRepository<Domain> {

    suspend fun save(domain: Domain): Boolean

    suspend fun save(domains: List<Domain>): Boolean

    suspend fun load(id: Int): Domain

    suspend fun loadList(filter: Filter? = null): List<Domain>

    suspend fun count(filter: Filter? = null): Int

    suspend fun delete(domain: Domain): Boolean

    interface Filter
}