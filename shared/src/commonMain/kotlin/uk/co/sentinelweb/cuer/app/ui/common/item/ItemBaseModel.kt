package uk.co.sentinelweb.cuer.app.ui.common.item

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID

open class ItemBaseModel(
    open val id: OrchestratorContract.Identifier<GUID>
)