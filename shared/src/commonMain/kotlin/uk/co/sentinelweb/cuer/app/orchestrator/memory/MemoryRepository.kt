package uk.co.sentinelweb.cuer.app.orchestrator.memory

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

interface MemoryRepository<Domain> {
    val updates: Flow<Pair<Operation, Domain>>

    suspend fun load(platformId: String, options: Options): Domain?

    suspend fun load(domain: Domain, options: Options): Domain?

    suspend fun load(id: GUID, options: Options): Domain?

    suspend fun loadList(filter: Filter, options: Options): List<Domain>

    suspend fun save(domain: Domain, options: Options): Domain?

    suspend fun save(domains: List<Domain>, options: Options): List<Domain>

    suspend fun count(filter: Filter, options: Options): Int

    suspend fun delete(domain: Domain, options: Options): Boolean

    suspend fun delete(id: GUID, options: Options): Boolean

    suspend fun update(update: UpdateDomain<Domain>, options: Options): Domain

}