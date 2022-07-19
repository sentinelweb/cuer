package uk.co.sentinelweb.cuer.app.util.link

import uk.co.sentinelweb.cuer.domain.LinkDomain
import kotlin.math.max

class LinkExtractor {

    fun extractLinks(text: String): List<LinkDomain> {
        val links = REGEX
            .findAll(text)
            .map { mapUrlToLinkDomain(it.value) }
            .toList()
        val crypto = LinkDomain.Crypto.values()
            .map { crypto ->
                crypto.regex.findAll(text).toList()
                    .map { match ->
                        LinkDomain.CryptoLinkDomain(
                            match.value,
                            getTitle(text, match),
                            crypto,
                            extractRegion = match.range.start to match.range.endInclusive
                        )
                    }
            }
            .toList()
            .flatten()
        return links + crypto
    }

    private fun getTitle(text: String, match: MatchResult): String? =
        text.indexOfBackwards('\n', match.range.start, TITLE_MAX)
            .takeIf { it != -1 }
            ?.let { text.substring(it + 1, match.range.start) }
            ?.takeIf { (TITLE_MIN..TITLE_MAX).contains(it.length) }

    fun mapUrlToLinkDomain(url: String): LinkDomain.UrlLinkDomain {
        val domain = LinkDomain.domain(url)
        val domainHost = LinkDomain.DomainHost.values()
            .find { domainHost -> domainHost.domains.find { domain.endsWith(it) } != null }
            ?: LinkDomain.DomainHost.UNKNOWN
        val category =
            LinkDomain.Category.categoryLookup[domainHost]
                ?: LinkDomain.Category.OTHER
        return LinkDomain.UrlLinkDomain(url, domain = domainHost, category = category)
    }

    companion object {
        val REGEX =
            Regex("(?i)\\b(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?\\b")
        val TITLE_MAX = 30
        val TITLE_MIN = 3
    }
}

private fun String.indexOfBackwards(c: Char, start: Int, maxSize: Int): Int {
    for (i in start downTo max(start - maxSize, 0)) {
        if (this[i] == c) return i
    }
    return -1
}
