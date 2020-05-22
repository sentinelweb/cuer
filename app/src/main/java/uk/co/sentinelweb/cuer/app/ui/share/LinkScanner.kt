package uk.co.sentinelweb.cuer.app.ui.share

import android.content.Context
import android.net.Uri
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import java.util.*

class LinkScanner constructor(
    private val c: Context,
    private val log: LogWrapper
) {

    fun scan(uriString: String): MediaDomain? {
        try {
            val uri = Uri.parse(uriString)
            val host = uri.host?.toLowerCase(Locale.getDefault())
            if (uri.scheme == "content") {
                val input =
                    c.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                log.d("File: $input")
                return parseQuery(Uri.parse(input).query!!)
                    .firstOrNull { it[0] == "v" }
                    ?.let {
                        createYt(uriString, it[1])
                    }
            } else if (host!!.endsWith("youtu.be")) {
                return createYt(uriString, uri.path!!.substring(1))
            } else if (uri.query != null && host.endsWith("youtube.com")) {
                return parseQuery(uri.query!!)
                    .firstOrNull { it[0] == "v" }
                    ?.let {
                        createYt(uriString, it[1])
                    }
            }
        } catch (t: Throwable) {
            log.e("unable to process link : $uriString", t)
        }
        return null
    }

    private fun parseQuery(query: String): List<List<String>> {
        return query
            .split("&")
            .map { param -> param.split("=") }
    }

    private fun createYt(u: String, mid: String) = MediaDomain(
        id = "0",
        url = u,
        mediaId = mid,
        mediaType = MediaDomain.MediaTypeDomain.VIDEO,
        platform = PlatformDomain.YOUTUBE
    )
}