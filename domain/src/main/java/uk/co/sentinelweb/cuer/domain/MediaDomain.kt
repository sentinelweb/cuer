package uk.co.sentinelweb.cuer.domain

import java.time.Instant

data class MediaDomain(
    val id: String,
    val url: String,
    val mediaId: String,
    val mediaType: MediaTypeDomain,
    val title: String?,
    val duration: Long?,
    val positon: Long?,
    val dateLastPlayed: Instant?,
    val description:String?,
    val platform:PlatformDomain
) {
    enum class MediaTypeDomain {
        VIDEO, AUDIO, WEB
    }

    enum class PlatformDomain {
        YOUTUBE, VIMEO, SOUNDCLOUD, WEB, OTHER
    }
}