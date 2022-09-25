package uk.co.sentinelweb.cuer.app.util.share.scan

import android.net.Uri
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.*

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
        PLAYLIST to PlaylistDomain.createYoutube(uri.toString(), uri.getQueryParameters("list")[0])
}

// https://youtube.com/c/customUrl
// https://youtube.com/channel/customUrl also?? - might be more work to do to differentiate from simple channel id
// todo need to exec https://developers.google.com/youtube/v3/docs/channels/list#forUsername to get platformID
private class YoutubeChannelUrlUserMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("c/") == 1 } ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, ChannelDomain> =
        CHANNEL to ChannelDomain.createYoutubeCustomUrl(
            uri.path ?: throw IllegalArgumentException("Bad channel url: $uri")
        )
}

// https://youtube.com/channel/<platformId>.
private class YoutubeChannelUrlMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("channel") == 1 } ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, ChannelDomain> =
        CHANNEL to ChannelDomain.createYoutube(
            uri.path ?: throw IllegalArgumentException("Bad channel url: $uri")
        )
}

// https://www.youtube.com/shorts/lq9hzALa4Po.
private class YoutubeShortsUrlMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("shorts") > 0 } ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        uri.path
            ?.let {
                MEDIA to MediaDomain.createYoutube(
                    uri.toString(),
                    it.substring(it.lastIndexOf('/') + 1)
                )
            }
            ?: throw IllegalArgumentException("Link format error: $uri")
}
