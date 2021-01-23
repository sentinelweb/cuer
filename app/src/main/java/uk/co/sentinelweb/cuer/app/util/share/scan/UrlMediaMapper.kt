package uk.co.sentinelweb.cuer.app.util.share.scan

import android.net.Uri
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.VIDEO
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.MEDIA
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.PLAYLIST
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

val urlMediaMappers = listOf(
    YoutubeShortUrlMediaMapper(),
    YoutubeUrlMediaMapper(),
    YoutubeUrlPlaylistMapper()
)

interface UrlMediaMapper {
    fun check(uri: Uri): Boolean
    fun map(uri: Uri): Pair<ObjectTypeDomain, Any>
}

private class YoutubeShortUrlMediaMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean = uri.host?.endsWith("youtu.be") ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        uri.path
            ?.let {
                MEDIA to MediaDomain(
                    id = null,
                    url = uri.toString(),
                    platformId = it.substring(1),
                    mediaType = VIDEO,
                    platform = YOUTUBE,
                    channelData = ChannelDomain( // todo add real data
                        platformId = null,
                        platform = YOUTUBE
                    )
                )
            }
            ?: throw IllegalArgumentException("Link format error: $uri")
}

private class YoutubeUrlMediaMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("watch") > 0 } ?: false
                && uri.getQueryParameters("v").isNotEmpty()

    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        MEDIA to MediaDomain(
            id = null,
            url = uri.toString(),
            platformId = uri.getQueryParameters("v")[0],
            mediaType = VIDEO,
            platform = YOUTUBE,
            channelData = ChannelDomain(
                platformId = null,
                platform = YOUTUBE
            )
        )
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
                updateUrl = uri.toString(),
                platformId = uri.getQueryParameters("list")[0]
            ),
            starred = false,
            items = listOf(),
            currentIndex = -1,
            title = "",
            mode = PlaylistDomain.PlaylistModeDomain.SINGLE,
            parentId = null,
            default = false,
            archived = false,
            image = null,
            thumb = null
        )
}
