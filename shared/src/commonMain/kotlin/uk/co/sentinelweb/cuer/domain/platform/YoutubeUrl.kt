package uk.co.sentinelweb.cuer.domain.platform

import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class YoutubeUrl {
    companion object {
        fun channelUrl(media: MediaDomain) =
            "https://youtube.com/channel/${media.channelData.let { it.customUrl ?: it.platformId }}"

        fun channelUrl(channel: ChannelDomain) =
            "https://youtube.com/channel/${channel.let { it.customUrl ?: it.platformId }}"

        fun videoUrl(media: MediaDomain) = "https://www.youtube.com/watch?v=${media.platformId}"
        fun videoUrl(platformId: String) = "https://www.youtube.com/watch?v=$platformId"
        fun videoShortUrl(media: MediaDomain) = "https://youtu.be/${media.platformId}"

        fun playlistUrl(playlist: PlaylistDomain) =
            "https://www.youtube.com/playlist?list=${playlist.platformId}"

        fun playlistUrl(platformId: String) = "https://www.youtube.com/playlist?list=${platformId}"
    }
}