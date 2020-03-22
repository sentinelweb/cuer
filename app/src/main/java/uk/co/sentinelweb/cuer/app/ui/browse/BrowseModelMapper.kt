package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.domain.MediaDomain

class BrowseModelMapper constructor() {
    fun map(domain: List<MediaDomain>): BrowseModel = BrowseModel(
        domain.map {
            BrowseModel.BrowseItemModel(
                it.url.toString(),
                it.type,
                it.title,
                "${(it.lengthMs / 1000)}s",
                "${(it.positonMs / 1000)}s"
            )
        }
    )
}
