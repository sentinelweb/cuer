package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

//import java.time.Instant
//import java.time.LocalDateTime

@Serializable
data class MediaDomain(
    val id: Long?,
    val url: String,
    val platformId: String,
    val mediaType: MediaTypeDomain,
    val platform: PlatformDomain,
    val title: String? = null,
    val duration: Long? = null,
    val positon: Long? = null,
    @Contextual val dateLastPlayed: Instant? = null,
    val description: String? = null,
    @Contextual val published: LocalDateTime? = null,
    val channelData: ChannelDomain,
    val thumbNail: ImageDomain? = null,
    val image: ImageDomain? = null,
    val watched: Boolean = false,
    val starred: Boolean = false,
    val isLiveBroadcast: Boolean = false,
    val isLiveBroadcastUpcoming: Boolean = false,
    val playFromStart: Boolean = false,
) : Domain {

    enum class MediaTypeDomain {
        VIDEO, AUDIO, WEB
    }

    companion object {
        fun createYoutube(url: String, platformId: String) = MediaDomain(
            id = null,
            url = url,
            platformId = platformId,
            mediaType = MediaTypeDomain.VIDEO,
            platform = PlatformDomain.YOUTUBE,
            channelData = ChannelDomain(
                platformId = null,
                platform = PlatformDomain.YOUTUBE
            )
        )
    }

}