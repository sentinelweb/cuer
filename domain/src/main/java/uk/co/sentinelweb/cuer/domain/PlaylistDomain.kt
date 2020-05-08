package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistDomainMode.SINGLE

@Serializable
data class PlaylistDomain constructor(
    val id: String? = null,
    val items: List<PlaylistItemDomain>,
    val currentIndex: Int = 0,
    val mode: PlaylistDomainMode = SINGLE,
    val tags: List<TagDomain>? = null
) {

    enum class PlaylistDomainMode {
        SINGLE, LOOP, SHUFFLE
    }
}

