package uk.co.sentinelweb.cuer.net.youtube.videos

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.YoutubeVideosInteractor
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.youtube.YoutubePart
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService
import java.io.IOException

internal class YoutubeVideosRetrofitInteractor constructor(
    private val keyProvider: YoutubeApiKeyProvider,
    private val service: YoutubeService,
    private val mapper: YoutubeVideoMediaDomainMapper
) : YoutubeVideosInteractor {

    suspend override fun videos(ids: List<String>, parts: List<YoutubePart>): List<MediaDomain> {
        try {
            return service.getVideoInfos(
                ids = ids.joinToString(separator = ","),
                parts = parts.map { it.part }.joinToString(separator = ","),
                key = keyProvider.key
            )
                .let { mapper.map(it) }
        } catch (ex: IOException) {
            throw ex // todo error handling
        } catch (ex: Exception) {
            throw ex // todo error handling
        }
    }
}