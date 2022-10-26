package uk.co.sentinelweb.cuer.app.ui.common.navigation

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.LINK
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.share.ShareNavigationHack
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.LinkDomain.UrlLinkDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class LinkNavigator(
    private val navRouter: NavigationRouter,
    private val linkScanner: LinkScanner,
    private val shareNavigationHack: ShareNavigationHack,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val coroutines: CoroutineContextProvider,
    private val isMain: Boolean,
) {

    fun navigateLink(link: UrlLinkDomain) = coroutines.mainScope.launch {
        (linkScanner.scan(link.address)?.let { scanned ->
            when (scanned.first) {
                ObjectTypeDomain.MEDIA -> navMedia(link, scanned.second as MediaDomain)
                ObjectTypeDomain.PLAYLIST -> navPlaylist(link, scanned.second as PlaylistDomain)
                ObjectTypeDomain.PLAYLIST_ITEM -> navShare(link)
                ObjectTypeDomain.CHANNEL -> navLink(link)
                else -> navLink(link)
            }
        } ?: let { navLink(link) })
            .apply { shareNavigationHack.isNavigatingInApp = true }
            .apply { navRouter.navigate(this) }
    }

    private suspend fun navMedia(link: UrlLinkDomain, media: MediaDomain): NavigationModel =
        if (isMain) {
            playlistItemOrchestrator.loadList(
                PlatformIdListFilter(listOf(media.platformId)), LOCAL.flatOptions()
            ).firstOrNull()
                ?.let { navItem(it) }
                ?: navShare(link)
        } else {
            navShare(link)
        }

    private suspend fun navPlaylist(link: UrlLinkDomain, playlist: PlaylistDomain): NavigationModel =
        if (isMain) {
            playlistOrchestrator.loadList(
                PlatformIdListFilter(listOf(playlist.platformId!!)), LOCAL.flatOptions()
            ).firstOrNull()
                ?.let { navPlaylist(it) }
                ?: navShare(link)
        } else {
            navShare(link)
        }

    private fun navItem(it: PlaylistItemDomain) =
        NavigationModel(PLAYLIST_ITEM, mapOf(Param.PLAYLIST_ITEM to it, SOURCE to LOCAL))

    private fun navPlaylist(it: PlaylistDomain) =
        NavigationModel(PLAYLIST, mapOf(Param.PLAYLIST_ID to it.id!!, SOURCE to LOCAL))

    private fun navLink(link: UrlLinkDomain): NavigationModel =
        NavigationModel(WEB_LINK, mapOf(LINK to link.address))

    private fun navShare(link: UrlLinkDomain): NavigationModel =
        NavigationModel(SHARE, mapOf(LINK to link.address))

}