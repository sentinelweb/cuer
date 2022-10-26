package uk.co.sentinelweb.cuer.app.ui.main

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

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

    override fun checkForPendingNavigation(target: NavigationModel.Target?): NavigationModel? {
        val intent = mainActivity.intent

        //log.d("checkForPendingNavigation:$target > ${intent.getStringExtra(NavigationModel.Target.KEY)}")
        return intent.getStringExtra(NavigationModel.Target.KEY)
            ?.takeIf { target == null || it == target.name }
            ?.let {
                return when (it) {
                    NavigationModel.Target.PLAYLIST.name ->
                        PlaylistContract.makeNav(
                            NavigationModel.Param.PLAYLIST_ID.getLong(intent)
                                ?: throw IllegalArgumentException("Playlist ID is required"),
                            NavigationModel.Param.PLAYLIST_ITEM_ID.getLong(intent),
                            NavigationModel.Param.PLAY_NOW.getBoolean(intent),
                            NavigationModel.Param.SOURCE.getEnum<OrchestratorContract.Source>(intent)
                        ).apply {
                            log.d("got nav:$this")
                        }

                    else -> null
                }
            }
    }

    override fun clearPendingNavigation(target: NavigationModel.Target) {
        navRouter.clearArgs(mainActivity.intent, target)
    }

}