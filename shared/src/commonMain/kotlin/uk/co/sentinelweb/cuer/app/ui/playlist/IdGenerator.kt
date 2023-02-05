package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

class IdGenerator(private val guidCreator: GuidCreator) {

    val value: OrchestratorContract.Identifier<GUID>
        get() = guidCreator.create().toIdentifier(OrchestratorContract.Source.MEMORY)

}