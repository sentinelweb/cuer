package uk.co.sentinelweb.cuer.app.util.share.scan

import android.content.Context
import android.net.Uri
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class LinkScanner constructor(
    private val c: Context,
    private val log: LogWrapper,
    private val mappers: List<UrlMediaMapper>
) {

    fun scan(uriString: String): MediaDomain? {
        try {
            val uri = Uri.parse(uriString)
            mappers.forEach {
                it
                    .takeIf { it.check(uri) }
                    ?.map(uri)
                    ?.apply { return@scan this }
            }
        } catch (t: Throwable) {
            log.e("unable to process link : $uriString", t)
        }
        return null
    }
}