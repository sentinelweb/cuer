package uk.co.sentinelweb.cuer.app.impl

class ProxyFilter {
    fun allFilter() = uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
    fun defaultFilter() = uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.DefaultFilter
    fun idListFilter(list: List<Long>) =
        uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.IdListFilter(list)
}