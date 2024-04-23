package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract

@Serializable
data class MediaDomain(
    val id: OrchestratorContract.Identifier<GUID>? = null,
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
    @Contextual val broadcastDate: LocalDateTime? = null, // todo https://github.com/sentinelweb/cuer/issues/196
) : Domain {

    enum class MediaTypeDomain {
        VIDEO, AUDIO, WEB, FILE
    }

    companion object {

        const val FLAG_WATCHED = 1L
        const val FLAG_STARRED = 2L
        const val FLAG_LIVE = 4L
        const val FLAG_LIVE_UPCOMING = 8L
        const val FLAG_PLAY_FROM_START = 16L

        fun createYoutube(url: String, platformId: String) = MediaDomain(
            id = null,
            url = url,
            platformId = platformId,
            mediaType = MediaTypeDomain.VIDEO,
            platform = PlatformDomain.YOUTUBE,
            channelData = ChannelDomain(
                id = null,
                platformId = null,
                platform = PlatformDomain.YOUTUBE
            )
        )
    }

}