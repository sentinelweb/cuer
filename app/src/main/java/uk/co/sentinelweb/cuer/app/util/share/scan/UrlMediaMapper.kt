package uk.co.sentinelweb.cuer.app.util.share.scan

import android.net.Uri
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.*
import java.net.URLDecoder

val urlMediaMappers = listOf(
    YoutubeShortUrlMapper(),
    YoutubeMediaUrlMapper(),
    YoutubePlaylistUrlMapper(),
    YoutubeChannelUrlUserMapper(),
    YoutubeChannelUrlMapper(),
    YoutubeShortsUrlMapper(),
    GoogleYoutubeUrlMapper(YoutubeMediaUrlMapper())
)

interface UrlMediaMapper {
    fun check(uri: Uri): Boolean
    fun map(uri: Uri): Pair<ObjectTypeDomain, Domain>
}

// https://youtu.be/Wp_UfkH2bL8
private class YoutubeShortUrlMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean = uri.host?.endsWith("youtu.be") ?: false

    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        uri.path
            ?.let { MEDIA to MediaDomain.createYoutube(uri.toString(), it.substring(1)) }
            ?: throw IllegalArgumentException("Link format error: $uri")
}

// https://www.youtube.com/watch?v=Wp_UfkH2bL8
// https://m.youtube.com/watch?v=88YCkY8U2NU
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

// https://www.google.com/url?sa=t&source=web&rct=j&url=https://m.youtube.com/watch%3Fv%3D88YCkY8U2NU&ved=2ahUKEwiuh5DxpYH7AhWJx4UKHVrKB1IQwqsBegQIdhAF&usg=AOvVaw0faoqETwWJ6C2Y_kMcucLW
private class GoogleYoutubeUrlMapper(
    private val youtubeMediaUrlMapper: YoutubeMediaUrlMapper
) : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        ((uri.host?.contains("google.") ?: false)
                && (uri.path?.let { it.indexOf("url") == 1 } ?: false)
                && (uri.getQueryParameters("url")?.takeIf { it.size == 1 }?.get(0)?.contains("youtube.com") ?: false))


    override fun map(uri: Uri): Pair<ObjectTypeDomain, MediaDomain> =
        uri.getQueryParameters("url")
            ?.takeIf { it.size == 1 }
            ?.get(0)
            ?.let { URLDecoder.decode(it) }
            ?.let { Uri.parse(it) }
            ?.takeIf { youtubeMediaUrlMapper.check(it) }
            ?.let { youtubeMediaUrlMapper.map(it) }
            ?: throw IllegalArgumentException("Link format error: $uri")
}

