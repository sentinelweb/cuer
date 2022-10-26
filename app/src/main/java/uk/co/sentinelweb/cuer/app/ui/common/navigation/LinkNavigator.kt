package uk.co.sentinelweb.cuer.app.ui.common.navigation

import uk.co.sentinelweb.cuer.app.ui.share.ShareNavigationHack
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain

class LinkNavigator(
    private val navRouter: NavigationRouter,
    private val linkScanner: LinkScanner,
    private val shareNavigationHack: ShareNavigationHack,
    private val isMain: Boolean,
) {

    fun navigateLink(link: LinkDomain.UrlLinkDomain) {
        (linkScanner.scan(link.address)?.let { scanned ->
            when (scanned.first) {
                ObjectTypeDomain.MEDIA -> navShare(link)
                ObjectTypeDomain.PLAYLIST -> navShare(link)
                ObjectTypeDomain.PLAYLIST_ITEM -> navShare(link)
                ObjectTypeDomain.CHANNEL -> navLink(link)
                else -> navLink(link)
            }
        } ?: let { navLink(link) })
            .apply { shareNavigationHack.isNavigatingInApp = true }
            .apply { navRouter.navigate(this) }
    }

    private fun navLink(link: LinkDomain.UrlLinkDomain): NavigationModel =
        NavigationModel(NavigationModel.Target.WEB_LINK, mapOf(NavigationModel.Param.LINK to link.address))

    private fun navShare(link: LinkDomain.UrlLinkDomain): NavigationModel =
        NavigationModel(NavigationModel.Target.SHARE, mapOf(NavigationModel.Param.LINK to link.address))


}