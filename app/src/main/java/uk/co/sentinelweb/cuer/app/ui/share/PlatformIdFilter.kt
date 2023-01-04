package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.domain.Domain

class PlatformIdFilter(val each: Triple<Operation, Source, Domain>, val platform: Triple<Operation, Source, Domain>) {
    fun invoke(): Boolean = each == platform
}
