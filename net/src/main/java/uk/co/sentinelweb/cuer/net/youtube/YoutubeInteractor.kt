package uk.co.sentinelweb.cuer.net.youtube

import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.ID
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.SNIPPET

interface YoutubeInteractor {

    @Throws(Exception::class)
    suspend fun videos(
        ids: List<String>,
        parts: List<YoutubePart> = listOf(ID, SNIPPET)
    ): NetResult<List<MediaDomain>>

    @Throws(Exception::class)
    suspend fun channels(
        ids: List<String>,
        parts: List<YoutubePart> = listOf(ID, SNIPPET)
    ): NetResult<List<ChannelDomain>>

    @Throws(Exception::class)
    suspend fun playlist(
        id: String
    ): NetResult<PlaylistDomain>
}
