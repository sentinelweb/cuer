package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistDomainMode.SINGLE

@Serializable
data class PlaylistDomain(
    val id: String? = null,
    val items: List<PlaylistItemDomain>,
    val currentIndex: Int = 0,
    val mode: PlaylistDomainMode = SINGLE
) {

    enum class PlaylistDomainMode {
        SINGLE, LOOP, SHUFFLE
    }
}

