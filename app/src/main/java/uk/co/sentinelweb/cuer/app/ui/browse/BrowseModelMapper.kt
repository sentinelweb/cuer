package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.domain.MediaDomain

class BrowseModelMapper constructor() {

    fun map(domain: List<MediaDomain>): BrowseModel = BrowseModel(
        domain.map {
            BrowseModel.BrowseItemModel(
                it.url,
                it.mediaType,
                it.title ?: "-",
                it.duration?.let { "${(it / 1000)}s" } ?: "-",
                it.positon?.let { "${(it / 1000)}s" } ?: "-"
            )
        }
    )
}