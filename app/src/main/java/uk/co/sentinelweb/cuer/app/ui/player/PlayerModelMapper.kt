package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.domain.MediaDomain

class PlayerModelMapper constructor() {
    fun map(domain: MediaDomain): PlayerContract.Model =
        PlayerContract.Model(
            domain.url.toString(),
            domain.mediaType,
            domain.title ?: "-",
            domain.duration?.let { "${(it / 1000)}s" } ?: "-",
            domain.positon?.let { "${(it / 1000)}s" } ?: "-"
        )
}
