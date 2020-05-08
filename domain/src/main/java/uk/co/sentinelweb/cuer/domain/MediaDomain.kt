package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime

@Serializable
data class MediaDomain(
    val id: String?,
    val url: String,
    val mediaId: String,
    val mediaType: MediaTypeDomain,
    val platform: PlatformDomain,
    val title: String? = null,
    val duration: Long? = null,
    val positon: Long? = null,
    @ContextualSerialization val dateLastPlayed: Instant? = null,
    val description: String? = null,
    @ContextualSerialization val published: LocalDateTime? = null,
    val channelId: String? = null,
    val channelTitle: String? = null,
    val thumbNail: ImageDomain? = null,
    val image: ImageDomain? = null,
    val starred: Boolean = false,
    val archived: Boolean = false,
    val tags: List<TagDomain>? = null
) {
    enum class MediaTypeDomain {
        VIDEO, AUDIO, WEB
    }

    enum class PlatformDomain {
        YOUTUBE, VIMEO, SOUNDCLOUD, WEB, OTHER
    }

}