package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.app.domain.MediaDomain

data class PlayerModel constructor(
    val url: String,
    val type: MediaDomain.MediaType,
    val title: String,
    val length: String,
    val positon: String
)