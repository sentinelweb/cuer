package uk.co.sentinelweb.cuer.app.util.share.scan

import android.net.Uri
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.*
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM

val urlMediaMappers = listOf(
    YoutubeShortUrlMapper(),
    YoutubeMediaUrlMapper(),
    YoutubePlaylistUrlMapper(),
    YoutubeChannelUrlUserMapper(),
    YoutubeChannelUrlMapper(),
    YoutubeShortsUrlMapper()
)

interface UrlMediaMapper {
    fun check(uri: Uri): Boolean
    fun map(uri: Uri): Pair<ObjectTypeDomain, Domain>
}

private class YoutubeShortUrlMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean = uri.host?.endsWith("youtu.be") ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        uri.path
            ?.let { MEDIA to MediaDomain.createYoutube(uri.toString(), it.substring(1)) }
            ?: throw IllegalArgumentException("Link format error: $uri")
}

private class YoutubeMediaUrlMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("watch") > 0 } ?: false
                && uri.getQueryParameters("v").isNotEmpty()

    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        MEDIA to MediaDomain.createYoutube(uri.toString(), uri.getQueryParameters("v")[0])
}

// https://www.youtube.com/playlist?list=<playlist ID>.
private class YoutubePlaylistUrlMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("playlist") > 0 } ?: false
                && uri.getQueryParameters("list").isNotEmpty()

    override fun map(uri: Uri): Pair<ObjectTypeDomain, PlaylistDomain> =
        PLAYLIST to PlaylistDomain(
            id = null,
            config = PlaylistDomain.PlaylistConfigDomain(
                platformUrl = uri.toString()
            ),
            type = PLATFORM,
            platform = YOUTUBE,
            platformId = uri.getQueryParameters("list")[0],
            starred = false,
            items = listOf(),
            currentIndex = -1,
            title = "",
            mode = SINGLE,
            parentId = null,
            default = false,
            archived = false,
            image = null,
            thumb = null
        )
}

// https://youtube.com/c/customUrl
// https://youtube.com/channel/customUrl also?? - might be more work to do to differentiate from simple channel id
// todo need to exec https://developers.google.com/youtube/v3/docs/channels/list#forUsername to get platformID
private class YoutubeChannelUrlUserMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("c") > 0 } ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, ChannelDomain> =
        CHANNEL to ChannelDomain(
            id = null,
            platform = YOUTUBE,
            platformId = NO_PLATFORM_ID,
            // todo make full url to match YoutubeChannelDomainMapper
            customUrl = uri.path?.let { it.substring(it.lastIndexOf('/')) }
        )
}

// https://youtube.com/channel/<platformId>.
private class YoutubeChannelUrlMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("c") > 0 } ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, ChannelDomain> =
        CHANNEL to ChannelDomain(
            id = null,
            platform = YOUTUBE,
            platformId = uri.path?.let { it.substring(it.lastIndexOf('/')) }
        )
}

// https://www.youtube.com/shorts/lq9hzALa4Po.
private class YoutubeShortsUrlMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("shorts") > 0 } ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        uri.path
            ?.let { MEDIA to MediaDomain.createYoutube(uri.toString(), it.substring(it.lastIndexOf('/'))) }
            ?: throw IllegalArgumentException("Link format error: $uri")
}

const val NO_PLATFORM_ID = "NoPlatformId"
