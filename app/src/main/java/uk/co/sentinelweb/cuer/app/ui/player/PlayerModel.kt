package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.domain.MediaDomain

data class PlayerModel constructor(
    val url: String,
    val type: MediaDomain.MediaTypeDomain,
    val title: String,
    val length: String,
    val positon: String
)