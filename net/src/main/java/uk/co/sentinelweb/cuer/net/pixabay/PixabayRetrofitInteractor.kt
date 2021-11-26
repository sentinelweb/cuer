package uk.co.sentinelweb.cuer.net.pixabay

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.pixabay.dto.PixabayImageListDto.Orientation.HORIZONTAL
import uk.co.sentinelweb.cuer.net.pixabay.dto.PixabayImageListDto.Type.PHOTO
import uk.co.sentinelweb.cuer.net.pixabay.mapper.PixabayImageMapper
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper

/**
 * Youtube interactor implementation
 * todo categories : https://www.googleapis.com/youtube/v3/videoCategories?regionCode=uk&key=
 */
internal class PixabayRetrofitInteractor constructor(
    private val keyProvider: ApiKeyProvider,
    private val service: PixabayService,
    private val imageMapper: PixabayImageMapper,
    private val coContext: CoroutineContextProvider,
    private val errorMapper: ErrorMapper,
    private val connectivity: ConnectivityWrapper
) : PixabayInteractor {

    init {
        errorMapper.log.tag(this)
    }

    @Throws(Exception::class)
    override suspend fun images(
        q: String
    ): NetResult<List<ImageDomain>> =
        withContext(coContext.IO) {
            try {
                if (connectivity.isConnected()) {
                    service.imageSearch(
                        query = q,
                        key = keyProvider.key,
                        imageType = PHOTO.param,
                        orientation = HORIZONTAL.param
                    )
                        .let { imageMapper.map(it) }
                        .let { NetResult.Data(it) }
                } else {
                    errorMapper.notConnected<List<ImageDomain>>()
                }
            } catch (ex: Throwable) {
                errorMapper.map<List<ImageDomain>>(ex, "iamges: error loading")
            }
        }


}