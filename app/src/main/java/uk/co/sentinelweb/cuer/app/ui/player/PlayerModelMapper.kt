package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.domain.MediaDomain

class PlayerModelMapper constructor() {
    fun map(domain: MediaDomain): PlayerModel =
        PlayerModel(
            domain.url.toString(),
            domain.type,
            domain.title,
            "${(domain.lengthMs / 1000)}s",
            "${(domain.positonMs / 1000)}s"
        )
}
