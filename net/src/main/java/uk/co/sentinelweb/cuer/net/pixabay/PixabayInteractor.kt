package uk.co.sentinelweb.cuer.net.pixabay

import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.net.NetResult

interface PixabayInteractor {

    @Throws(Exception::class)
    suspend fun images(
        q: String
    ): NetResult<List<ImageDomain>>

}
