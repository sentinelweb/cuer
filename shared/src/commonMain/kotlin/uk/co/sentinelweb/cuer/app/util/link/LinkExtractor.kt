package uk.co.sentinelweb.cuer.app.util.link

import uk.co.sentinelweb.cuer.domain.LinkDomain

class LinkExtractor {

    fun extractLinks(text: String): List<LinkDomain> {
        val links = REGEX
            .findAll(text)
            .map { mapUrlToLinkDomain(it) }
            .toList()
        val crypto = LinkDomain.Crypto.values()
            .map { crypto ->
                crypto.regex.findAll(text).toList()
                    .map { LinkDomain.CryptoLinkDomain(it.value, crypto) }
            }
            .toList()
            .flatten()
        return links + crypto
    }

    fun mapUrlToLinkDomain(it: MatchResult): LinkDomain.UrlLinkDomain {
        val domain = LinkDomain.domain(it.value)
        val domainHost = LinkDomain.DomainHost.values()
            .find { domainHost -> domainHost.domains.find { domain.endsWith(it) } != null }
            ?: LinkDomain.DomainHost.UNKNOWN
        val category =
            LinkDomain.Category.categoryLookup[domainHost]
                ?: LinkDomain.Category.OTHER
        return LinkDomain.UrlLinkDomain(it.value, domain = domainHost, category = category)
    }

    companion object {
        val REGEX =
            Regex("(?i)\\b(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?\\b")
    }
}