package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.domain.MediaDomain

data class BrowseModel constructor(
    val items:List<BrowseItemModel>
) {
    data class BrowseItemModel constructor(
        val url: String,
        val type: MediaDomain.MediaType,
        val title: String,
        val length: String,
        val positon: String
    )
}