package uk.co.sentinelweb.cuer.net

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubePart
import uk.co.sentinelweb.cuer.net.youtube.YoutubePart.ID
import uk.co.sentinelweb.cuer.net.youtube.YoutubePart.SNIPPET

interface YoutubeVideosInteractor {

    suspend fun videos(
        ids:List<String>,
        parts:List<YoutubePart> = listOf(ID, SNIPPET)
    ):List<MediaDomain>
}
