package uk.co.sentinelweb.cuer.app.orchestrator.memory

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*

interface MemoryRepository<Domain> {
    val updates: Flow<Pair<Operation, Domain>>

    fun load(platformId: String, options: Options): Domain?

    fun load(domain: Domain, options: Options): Domain?

    fun load(id: Long, options: Options): Domain?

    fun loadList(filter: Filter, options: Options): List<Domain>

    fun save(domain: Domain, options: Options): Domain

    fun save(domains: List<Domain>, options: Options): List<Domain>

    fun count(filter: Filter, options: Options): Int

    fun delete(domain: Domain, options: Options): Boolean


}