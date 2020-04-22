package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.domain.MediaDomain
import java.net.URI

class LinkScanner {

    fun scan(uriString: String): MediaDomain? {
        val uri = URI(uriString)
        val host = uri.host.toLowerCase()
        if (host.endsWith("youtu.be")) {
            return createYt(uriString, uri.path.substring(1))
        } else if (uri.query != null && host.endsWith("youtube.com")) {
            return parseQuery(uri.query)
                .firstOrNull { it[0] == "v" }
                ?.let {
                    createYt(uriString, it[0])
                }
        } else {
            return null
        }
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
        platform = MediaDomain.PlatformDomain.YOUTUBE
    )
}