package uk.co.sentinelweb.cuer.app.util.share.scan

import android.net.Uri
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.*
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM

val urlMediaMappers = listOf(
    YoutubeShortUrlMediaMapper(),
    YoutubeUrlMediaMapper(),
    YoutubeUrlPlaylistMapper(),
    YoutubeUrlChannelUserMapper(),
    YoutubeUrlChannelMapper()
)

interface UrlMediaMapper {
    fun check(uri: Uri): Boolean
    fun map(uri: Uri): Pair<ObjectTypeDomain, Domain>
}

private class YoutubeShortUrlMediaMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean = uri.host?.endsWith("youtu.be") ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        uri.path
            ?.let {
                MEDIA to MediaDomain.createYoutube(uri.toString(), it.substring(1))
            }
            ?: throw IllegalArgumentException("Link format error: $uri")
}

private class YoutubeUrlMediaMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("watch") > 0 } ?: false
                && uri.getQueryParameters("v").isNotEmpty()

    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        MEDIA to MediaDomain.createYoutube(uri.toString(), uri.getQueryParameters("v")[0])
}

// https://www.youtube.com/playlist?list=<playlist ID>.
private class YoutubeUrlPlaylistMapper : UrlMediaMapper {

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

// https://youtube.com/c/<userName>.
// todo need to exec https://developers.google.com/youtube/v3/docs/channels/list#forUsername to get platformID
private class YoutubeUrlChannelUserMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("c") > 0 } ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, ChannelDomain> =
        CHANNEL to ChannelDomain(
            id = null,
            platform = YOUTUBE,
            platformId = NO_PLATFORM_ID,
            customUrl = uri.path?.let { it.substring(it.lastIndexOf('/')) }
        )
}

// https://youtube.com/channel/<platformId>.
private class YoutubeUrlChannelMapper : UrlMediaMapper {

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

const val NO_PLATFORM_ID = "NoPlatformId"
