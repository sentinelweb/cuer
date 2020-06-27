package uk.co.sentinelweb.cuer.net.youtube.videos

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService

internal class YoutubeRetrofitInteractor constructor(
    private val keyProvider: YoutubeApiKeyProvider,
    private val service: YoutubeService,
    private val videoMapper: YoutubeVideoMediaDomainMapper,
    private val channelMapper: YoutubeChannelDomainMapper,
    private val coContext: CoroutineContextProvider,
    private val errorMapper: ErrorMapper

) : YoutubeInteractor {

    init {
        errorMapper.log.tag(this)
    }

    suspend override fun videos(
        ids: List<String>,
        parts: List<YoutubePart>
    ): NetResult<List<MediaDomain>> =
        withContext(coContext.IO) {
            try {
                service.getVideoInfos(
                    ids = ids.joinToString(separator = ","),
                    parts = parts.map { it.part }.joinToString(separator = ","),
                    key = keyProvider.key
                )
                    .let { videoMapper.map(it) }
                    .let { medias ->
                        updateChannelData(medias)
                            .takeIf { it.isSuccessful }
                            .let { it?.data }
                            ?: medias
                    }
                    .let { NetResult.Data(it) }
            } catch (ex: Throwable) {
                errorMapper.map<List<MediaDomain>>(ex, "videos: error: $ids")
            }
        }

    suspend fun updateChannelData(
        medias: List<MediaDomain>
    ): NetResult<List<MediaDomain>> =
        withContext(coContext.IO) {
            val idList = medias.map { it.channelData.remoteId!! }.distinct()
            try {
                // note the items come out of order
                channels(ids = idList)
                    .takeIf { it.isSuccessful }
                    ?.data
                    ?.let { channels ->
                        medias.map { media ->
                            channels
                                .find { channel -> channel.id == media.channelData.id }
                                ?.let { media.copy(channelData = it) }
                                ?: media
                        }
                    }?.let { NetResult.Data(it) }
                    ?: medias
                        .let { NetResult.Data(it) }
            } catch (ex: Throwable) {
                errorMapper.map<List<MediaDomain>>(ex, "updateChannelData: error: $idList")
            }
        }

    /**
     * note the items come out of order from the API
     */
    override suspend fun channels(
        ids: List<String>,
        parts: List<YoutubePart>
    ): NetResult<List<ChannelDomain>> =
        withContext(coContext.IO) {
            try {
                service.getChannelInfos(
                    ids = ids.joinToString(separator = ","),
                    parts = parts.map { it.part }.joinToString(separator = ","),
                    key = keyProvider.key
                )
                    .let { channelMapper.map(it) }
                    .let { NetResult.Data(it) }
            } catch (ex: Throwable) {
                errorMapper.map<List<ChannelDomain>>(ex, "channels: error: $ids")
            }
        }

}