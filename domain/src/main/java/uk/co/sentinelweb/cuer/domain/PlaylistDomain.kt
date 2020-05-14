package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistDomainMode.SINGLE
import java.time.Instant

@Serializable
data class PlaylistDomain constructor(
    val id: String? = null,
    val items: List<PlaylistItemDomain>,
    val currentIndex: Int = 0,
    val mode: PlaylistDomainMode = SINGLE,
    val tags: List<TagDomain>? = null,
    val starred: Boolean = false,
    val archived: Boolean = false,
    val config: PlaylistConfigDomain = PlaylistConfigDomain()
) {

    enum class PlaylistDomainMode {
        SINGLE, LOOP, SHUFFLE
    }

    enum class PlaylistTypeDomain {
        USER,
        CHANNEL, /* link to a channel */
        EXTERNAL /* External playlist do allow adding - just fix to source - maybe allow archiving of items */
    }

    @Serializable
    class PlaylistConfigDomain constructor(
        val type: PlaylistTypeDomain = PlaylistTypeDomain.USER,
        val platform: PlatformDomain = PlatformDomain.YOUTUBE,
        val updateUrl: String? = null,
        @ContextualSerialization val lastUpdate: Instant? = null,
        val updateInterval: Long? = null,
        val channelData: ChannelDomain? = null
    )
}

