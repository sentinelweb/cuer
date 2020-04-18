package uk.co.sentinelweb.cuer.net

import uk.co.sentinelweb.cuer.domain.MediaDomain

interface YoutubeDataInteractor {

    suspend fun getInfo(id:String):MediaDomain
}
