package uk.co.sentinelweb.cuer.app.util.wrapper

import uk.co.sentinelweb.cuer.domain.LinkDomain

interface CryptoLauncher {
    val cryptoAppWhiteList: List<String>
    fun launch(link: LinkDomain.CryptoLinkDomain)
}