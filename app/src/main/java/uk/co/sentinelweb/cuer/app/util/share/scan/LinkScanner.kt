package uk.co.sentinelweb.cuer.app.util.share.scan

import android.net.Uri
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain

class LinkScanner constructor(
    private val log: LogWrapper,
    private val mappers: List<UrlMediaMapper>
) {

    fun scan(uriString: String): Pair<ObjectTypeDomain, Any>? {
        try {
            val uri = Uri.parse(clean(uriString))
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

    private fun clean(uriString: String): String? {
        var cleaned = uriString
        if (!cleaned.startsWith("http") && cleaned.indexOf("http") > -1) {
            cleaned = cleaned.substring(cleaned.indexOf("http"))
        }
        cleaned = Regex("\\s").find(cleaned)
            ?.let { cleaned.substring(0, it.range.first) }
            ?: cleaned

        return cleaned
    }
}