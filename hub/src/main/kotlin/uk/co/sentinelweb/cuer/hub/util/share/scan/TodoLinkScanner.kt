package uk.co.sentinelweb.cuer.hub.util.share.scan

import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain

// need a Uri class that matched android
// AndroidLinkScanner can run on jvm with matching Uri class
class TodoLinkScanner : LinkScanner {
    override fun scan(uriString: String): Pair<ObjectTypeDomain, Domain>? {
        TODO("Not yet implemented")
    }

    override fun scan(link: LinkDomain.UrlLinkDomain): Pair<ObjectTypeDomain, Domain>? {
        TODO("Not yet implemented")
    }
}