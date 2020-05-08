package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class PlaylistItemDomain(
    val id: String? = null,
    val media: MediaDomain,
    @ContextualSerialization val dateAdded: Instant,
    val order: Long,
    private val _tags: List<TagDomain>?
) {
    val tags: List<TagDomain>?
        get() = _tags ?: media.tags
}
