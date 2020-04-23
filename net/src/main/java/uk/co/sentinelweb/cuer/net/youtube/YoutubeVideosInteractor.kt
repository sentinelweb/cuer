package uk.co.sentinelweb.cuer.net.youtube

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.ID
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.SNIPPET

interface YoutubeVideosInteractor {

    suspend fun videos(
        ids:List<String>,
        parts:List<YoutubePart> = listOf(ID, SNIPPET)
    ):List<MediaDomain>
}
