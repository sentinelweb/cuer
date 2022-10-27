package uk.co.sentinelweb.cuer.domain.platform

import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class YoutubeUrl {
    companion object {
        fun channelUrl(media: MediaDomain) =
            "https://youtube.com/channel/${media.channelData.let { it.customUrl ?: it.platformId }}"

        // fixme this doesn't work too well
        // it.customUrl is https://youtube.com/channel/academyOfIdeas when parsed frm youtube api (i.e. in db)
        // it.platformId can be NoChannelId if parsed from https://youtube.com/channel/academyOfIdeas in UrlMediaMapper
        // need to sync the results of these to some standard
        fun channelUrl(channel: ChannelDomain) =
            "https://youtube.com/channel/${channel.let { it.customUrl ?: it.platformId }}"

        fun channelPlatformIdUrl(channel: ChannelDomain) =
            "https://youtube.com/channel/${channel.platformId}"

        fun videoUrl(media: MediaDomain) = "https://www.youtube.com/watch?v=${media.platformId}"

        fun videoUrl(platformId: String) = "https://www.youtube.com/watch?v=$platformId"

        fun videoUrlWithTime(media: MediaDomain): String {
            val duration = media.duration ?: -1L
            val position = media.positon
                ?.let { if (duration <= 0) 0 else if ((duration - it) > 1000) it / 1000 else 0 }
                ?: 0
            return "https://www.youtube.com/watch?v=${media.platformId}&t=$position"
        }

        fun videoShortUrl(media: MediaDomain) = "https://youtu.be/${media.platformId}"

        fun playlistUrl(playlist: PlaylistDomain) =
            "https://www.youtube.com/playlist?list=${playlist.platformId}"

        fun playlistUrl(platformId: String) = "https://www.youtube.com/playlist?list=${platformId}"
    }
}