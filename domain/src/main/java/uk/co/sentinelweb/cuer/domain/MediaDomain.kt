package uk.co.sentinelweb.cuer.domain

import java.time.Instant

data class MediaDomain(
    val id: String?,
    val url: String,
    val mediaId: String,
    val mediaType: MediaTypeDomain,
    val platform:PlatformDomain,
    val title: String? = null,
    val duration: Long? = null,
    val positon: Long? = null,
    val dateLastPlayed: Instant? = null,
    val description:String? = null
) {
    enum class MediaTypeDomain {
        VIDEO, AUDIO, WEB
    }

    enum class PlatformDomain {
        YOUTUBE, VIMEO, SOUNDCLOUD, WEB, OTHER
    }
}