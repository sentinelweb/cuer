package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import java.time.Instant

@Serializable
data class PlaylistDomain constructor(
    val id: Long? = null,
    val title: String,
    val items: List<PlaylistItemDomain>,
    val currentIndex: Int = -1,
    val mode: PlaylistModeDomain = SINGLE,
    val starred: Boolean = false,
    val archived: Boolean = false,
    val thumb: ImageDomain? = null,
    val image: ImageDomain? = null,
    val config: PlaylistConfigDomain = PlaylistConfigDomain()
) {

    enum class PlaylistModeDomain {
        SINGLE, LOOP, SHUFFLE
    }

    enum class PlaylistTypeDomain {
        USER,
        CHANNEL, /* link to a channel */
        EXTERNAL /* External playlist do allow adding - just fix to source - maybe allow archiving of items */
    }

    @Serializable
    data class PlaylistConfigDomain constructor(
        val type: PlaylistTypeDomain = PlaylistTypeDomain.USER,
        val platform: PlatformDomain = PlatformDomain.YOUTUBE,
        val updateUrl: String? = null,
        @ContextualSerialization val lastUpdate: Instant? = null,
        val updateInterval: Long? = null,
        val channelData: ChannelDomain? = null
    )
}

