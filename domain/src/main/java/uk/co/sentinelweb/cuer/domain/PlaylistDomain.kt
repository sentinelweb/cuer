package uk.co.sentinelweb.cuer.domain

import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistDomainMode.SINGLE

data class PlaylistDomain(
    val items: List<PlaylistItemDomain>,
    val mode:PlaylistDomainMode = SINGLE
) {
    enum class PlaylistDomainMode {
        SINGLE, LOOP, SHUFFLE
    }
}

