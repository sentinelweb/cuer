package uk.co.sentinelweb.cuer.app.ui.main

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.navigation.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviFragment
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistItem
import uk.co.sentinelweb.cuer.domain.toGUID

// todo review all of this as part of
// https://github.com/sentinelweb/cuer/issues/279
class MainNavigationProvider(
    private val mainActivity: MainActivity,
    private val navRouter: NavigationRouter,
    private val log: LogWrapper,
) : NavigationProvider {

    private val navController: NavController by lazy {
        val navHostFragment =
            mainActivity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }

    override fun navigate(destination: NavigationModel) {
        navRouter.navigate(destination)
    }

    override fun navigate(id: Int) {
        navController.navigate(id)
    }

    override fun checkForPendingNavigation(target: Target?): NavigationModel? {
        val intent = mainActivity.intent

        log.d("checkForPendingNavigation: $target > ${intent.getStringExtra(Target.KEY)}")
        return intent.getStringExtra(Target.KEY)
            ?.takeIf { target == null || it == target.name }
            ?.let {
                return when (it) {
                    Target.PLAYLIST.name ->
                        PlaylistMviFragment.makeNav(
                            PLAYLIST_ID.getString(intent)?.toGUID()
                                ?: throw IllegalArgumentException("Playlist ID is required"),
                            PLAYLIST_ITEM_ID.getString(intent)?.toGUID(),
                            PLAY_NOW.getBoolean(intent),
                            SOURCE.getEnum<Source>(intent)
                        ).apply {
                            log.d("got nav: $this")
                        }

                    Target.PLAYLIST_ITEM.name ->
                        PLAYLIST_ITEM.getString(intent)?.let { itemString ->
                            val item = deserialisePlaylistItem(itemString)
                            // clear this nav straight away
                            navRouter.clearArgs(mainActivity.intent, Target.PLAYLIST_ITEM)
                            NavigationModel(
                                Target.PLAYLIST_ITEM,
                                mapOf(
                                    SOURCE to SOURCE.getEnum<Source>(intent),
                                    PLAYLIST_ITEM to item
                                )
                            )
                        }

                    else -> null
                }
            }
    }

    override fun clearPendingNavigation(target: Target) {
        navRouter.clearArgs(mainActivity.intent, target)
    }

}