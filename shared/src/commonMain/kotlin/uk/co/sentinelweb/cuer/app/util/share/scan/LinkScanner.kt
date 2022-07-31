package uk.co.sentinelweb.cuer.app.util.share.scan

import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain

interface LinkScanner {
    fun scan(uriString: String): Pair<ObjectTypeDomain, Domain>?
    fun scan(link: LinkDomain.UrlLinkDomain): Pair<ObjectTypeDomain, Domain>?
}