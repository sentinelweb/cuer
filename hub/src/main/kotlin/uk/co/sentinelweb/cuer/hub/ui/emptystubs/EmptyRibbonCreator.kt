package uk.co.sentinelweb.cuer.hub.ui.emptystubs

import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonCreator
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonModel
import uk.co.sentinelweb.cuer.domain.MediaDomain

class EmptyRibbonCreator : RibbonCreator {
    override fun createRibbon(media: MediaDomain): List<RibbonModel> = listOf()

    override fun createPlayerRibbon(media: MediaDomain): List<RibbonModel> = listOf()
}
