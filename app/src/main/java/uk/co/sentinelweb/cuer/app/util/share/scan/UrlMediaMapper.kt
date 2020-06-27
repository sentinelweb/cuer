package uk.co.sentinelweb.cuer.app.util.share.scan

import android.net.Uri
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain

val urlMediaMappers = listOf(
    YoutubeShortUrlMediaMapper(),
    YoutubeUrlMediaMapper()
)

interface UrlMediaMapper {
    fun check(uri: Uri): Boolean
    fun map(uri: Uri): MediaDomain
}

private class YoutubeShortUrlMediaMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean = uri.host?.endsWith("youtu.be") ?: false

    override fun map(uri: Uri): MediaDomain =
        uri.path
            ?.let {
                MediaDomain(
                    id = null,
                    url = uri.toString(),
                    mediaId = it.substring(1),
                    mediaType = MediaDomain.MediaTypeDomain.VIDEO,
                    platform = PlatformDomain.YOUTUBE,
                    channelData = ChannelDomain()// todo add real data
                )
            }
            ?: throw IllegalArgumentException("Link format error: $uri")
}

private class YoutubeUrlMediaMapper : UrlMediaMapper {

    override fun check(uri: Uri): Boolean =
        uri.host?.contains("youtube.") ?: false
                && uri.path?.let { it.indexOf("watch") > 0 } ?: false
                && uri.getQueryParameters("v").isNotEmpty()

    override fun map(uri: Uri): MediaDomain =
        MediaDomain(
            id = null,
            url = uri.toString(),
            mediaId = uri.getQueryParameters("v")[0],
            mediaType = MediaDomain.MediaTypeDomain.VIDEO,
            platform = PlatformDomain.YOUTUBE,
            channelData = ChannelDomain()// todo add real data
        )
}
