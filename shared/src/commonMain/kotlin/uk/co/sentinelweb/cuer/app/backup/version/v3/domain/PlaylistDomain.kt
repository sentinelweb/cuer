package uk.co.sentinelweb.cuer.app.backup.version.v3.domain

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.USER


@Serializable
data class PlaylistDomain(
    val id: Long? = null,
    val title: String,
    val items: List<PlaylistItemDomain> = listOf(),
    val currentIndex: Int = 0, // todo make nullable
    val parentId: Long? = null,
    val mode: PlaylistModeDomain = SINGLE,
    val type: PlaylistTypeDomain = USER,
    val platform: PlatformDomain? = null,
    val channelData: ChannelDomain? = null,
    val platformId: String? = null,
    val starred: Boolean = false,
    val archived: Boolean = false,
    val default: Boolean = false,
    val thumb: ImageDomain? = null,
    val image: ImageDomain? = null,
    val playItemsFromStart: Boolean = false,
    val config: uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistConfigDomain = uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistConfigDomain()
) : Domain {


//    companion object {
//        const val FLAG_STARRED = 1L
//        const val FLAG_ARCHIVED = 2L
//        const val FLAG_DEFAULT = 4L
//        const val FLAG_PLAY_ITEMS_FROM_START = 8L
//
//        fun createYoutube(url: String, platformId: String) = PlaylistDomain(
//            id = null,
//            config = PlaylistConfigDomain(
//                platformUrl = url
//            ),
//            type = PlaylistTypeDomain.PLATFORM,
//            platform = PlatformDomain.YOUTUBE,
//            platformId = platformId,
//            starred = false,
//            items = listOf(),
//            currentIndex = -1,
//            title = "",
//            mode = SINGLE,
//            parentId = null,
//            default = false,
//            archived = false,
//            image = null,
//            thumb = null
//        )
//
//        fun createDummy(title: String) = PlaylistDomain(
//            id = null,
//            config = PlaylistConfigDomain(),
//            type = PlaylistTypeDomain.APP,
//            platform = PlatformDomain.OTHER,
//            platformId = null,
//            starred = false,
//            items = listOf(),
//            currentIndex = -1,
//            title = title,
//            mode = SINGLE,
//            parentId = null,
//            default = false,
//            archived = false,
//            image = null,
//            thumb = null
//        )
//    }
}

