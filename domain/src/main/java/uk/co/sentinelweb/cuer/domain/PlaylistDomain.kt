package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import java.time.Instant
import java.time.LocalDateTime

@Serializable
data class PlaylistDomain constructor(
    val id: Long? = null,
    val title: String,
    val items: List<PlaylistItemDomain> = listOf(),
    val currentIndex: Int = 0, // todo make nullable
    val parentId: Long? = null,
    val mode: PlaylistModeDomain = SINGLE,
    val type: PlaylistTypeDomain = PlaylistTypeDomain.USER,
    val platform: PlatformDomain? = null,
    val channelData: ChannelDomain? = null,
    val platformId: String? = null,
    val starred: Boolean = false,
    val archived: Boolean = false,
    val default: Boolean = false,
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
        PLATFORM /* External playlist do allow adding - just fix to source - maybe allow archiving of items */
    }

    @Serializable
    data class PlaylistConfigDomain constructor(
        val updateUrl: String? = null,
        @Contextual val lastUpdate: Instant? = null,
        @Contextual val published: LocalDateTime? = null,
        val updateInterval: Long? = null,
        val description: String? = null
    )
}

