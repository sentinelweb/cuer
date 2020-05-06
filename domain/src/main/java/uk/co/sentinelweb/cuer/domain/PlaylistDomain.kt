package uk.co.sentinelweb.cuer.domain

import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistDomainMode.SINGLE

data class PlaylistDomain(
    val id: String? = null,
    val items: List<PlaylistItemDomain>,
    val currentIndex: Int = 0,
    val mode:PlaylistDomainMode = SINGLE
) {

    enum class PlaylistDomainMode {
        SINGLE, LOOP, SHUFFLE
    }
}

