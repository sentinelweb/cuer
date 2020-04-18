package uk.co.sentinelweb.cuer.app.db.repository

interface DatabaseRepository<Domain> {

    suspend fun save(domain:Domain)

    suspend fun load(id:Int):Domain

    suspend fun loadList(filter:Filter):List<Domain>

    suspend fun delete(domain:Domain)

    interface Filter
}