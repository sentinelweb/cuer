package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier

@Serializable
data class ChannelDomain constructor(
    val id: Identifier<GUID>?,
    val platformId: String?,
    val platform: PlatformDomain,
    val country: String? = null,
    val title: String? = null,
    val customUrl: String? = null,
    val description: String? = null,
    @Contextual val published: LocalDateTime? = null,
    val thumbNail: ImageDomain? = null,
    val image: ImageDomain? = null,
    val starred: Boolean = false
) : Domain {

    companion object {
        const val FLAG_STARRED = 1L

        fun createYoutube(uriPath: String) = ChannelDomain(
            id = null,
            platform = PlatformDomain.YOUTUBE,
            platformId = uriPath.lastIndexOf('/')
                .takeIf { it > 0 && it < uriPath.length - 2 }
                ?.let { uriPath.substring(it + 1) }
        )

        fun createYoutubeCustomUrl(uriPath: String) = ChannelDomain(
            id = null,
            platform = PlatformDomain.YOUTUBE,
            platformId = NO_PLATFORM_ID,
            // todo make full url to match YoutubeChannelDomainMapper
            customUrl = uriPath.lastIndexOf('/')
                .takeIf { it > 0 && it < uriPath.length - 2 }
                ?.let { uriPath.substring(it + 1) }
        )
    }
}