package uk.co.sentinelweb.cuer.app.ui.common.ribbon

import uk.co.sentinelweb.cuer.domain.MediaDomain

interface RibbonCreator {
    fun createRibbon(media: MediaDomain): List<RibbonModel>
    fun createPlayerRibbon(media: MediaDomain): List<RibbonModel>
}