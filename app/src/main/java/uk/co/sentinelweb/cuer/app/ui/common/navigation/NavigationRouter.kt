package uk.co.sentinelweb.cuer.app.ui.common.navigation

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.Companion.KEY
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_land.AytLandActivity
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait.AytPortraitActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.UrlLauncherWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.log.AndroidLogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class NavigationRouter constructor(
    private val activity: Activity,
    private val toastWrapper: ToastWrapper,
    private val fragment: Fragment? = null,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val navController: NavController?,
    private val log: LogWrapper,
    private val urlLauncher: UrlLauncherWrapper
) {

    fun navigate(nav: NavigationModel) {
        when (nav.target) {
            LOCAL_PLAYER_FULL ->
                (nav.params[PLAYLIST_ITEM] as PlaylistItemDomain?)?.let {
                    //YoutubeFullScreenActivity.start(activity, it)
                    AytLandActivity.start(activity, it)
                } ?: throw IllegalArgumentException("$LOCAL_PLAYER_FULL: $PLAYLIST_ITEM param required")
            LOCAL_PLAYER -> {
                log.d("YoutubePortraitActivity.NavigationMapper")
                (nav.params[PLAYLIST_ITEM] as PlaylistItemDomain?)?.let {
                    AytPortraitActivity.start(activity, it)
                    log.d("YoutubePortraitActivity.start called")
                } ?: throw IllegalArgumentException("$LOCAL_PLAYER: $PLAYLIST_ITEM param required")
            }
            WEB_LINK ->
                nav.params[LINK]?.let {
                    urlLauncher.launchUrl(it.toString())
                } ?: throw IllegalArgumentException("$WEB_LINK: $LINK param required")
            NAV_BACK -> navController?.popBackStack()
            NAV_FINISH -> activity.finish()
            YOUTUBE_CHANNEL -> if (!ytJavaApi.launchChannel(nav.params[CHANNEL_ID] as String)) {
                toastWrapper.show("can't launch channel")
            }
            YOUTUBE_VIDEO -> if (!ytJavaApi.launchVideoSystem(nav.params[PLATFORM_ID] as String)) {
                toastWrapper.show("can't launch channel")
            }
            PLAYLIST -> navController?.navigate(
                R.id.navigation_playlist,
                bundleOf(
                    PLAYLIST_ID.name to nav.params[PLAYLIST_ID],
                    PLAYLIST_ITEM_ID.name to nav.params[PLAYLIST_ITEM_ID],
                    PLAY_NOW.name to nav.params[PLAY_NOW],
                    SOURCE.name to nav.params[SOURCE].toString()
                ),
                nav.navOpts ?: navOptions(optionsBuilder = {// todo remove
                    launchSingleTop = true
                    popUpTo(R.id.navigation_playlist, { inclusive = true })
                }),
                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )
            PLAYLISTS -> navController?.navigate(
                R.id.navigation_playlists,
                bundleOf(
                    PLAYLIST_ID.name to nav.params[PLAYLIST_ID]
                ),
                nav.navOpts,
                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )
            // todo maybe remove these and use directions: if they are used from different places the back stack might be messed up
            NavigationModel.Target.PLAYLIST_ITEM -> navController?.navigate(
                R.id.navigation_playlist_item_edit,
                bundleOf(
                    PLAYLIST_ITEM.name to (nav.params[PLAYLIST_ITEM] as PlaylistItemDomain).serialise(),
                    SOURCE.name to nav.params[SOURCE].toString()
                ),
                nav.navOpts ?: navOptions(optionsBuilder = {
                    launchSingleTop = true
                    popUpTo(R.id.navigation_playlist_edit, { inclusive = true })
                }),
                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )
            PLAYLIST_EDIT -> navController?.navigate(
                R.id.navigation_playlist_edit,
                bundleOf(
                    PLAYLIST_ID.name to nav.params[PLAYLIST_ID],
                    SOURCE.name to nav.params[SOURCE].toString()
                ),
                nav.navOpts ?: navOptions(optionsBuilder = {
                    launchSingleTop = true
                    popUpTo(R.id.navigation_playlists, { inclusive = false })
                }),
                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )
            PLAYLIST_CREATE -> navController?.navigate(
                R.id.navigation_playlist_edit,
                bundleOf(
                    SOURCE.name to nav.params[SOURCE].toString()
                ),
                nav.navOpts ?: navOptions(optionsBuilder = {
                    launchSingleTop = true
                    popUpTo(R.id.navigation_playlists, { inclusive = false })
                }),
                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )
            else -> toastWrapper.show("Cannot launch ${nav.target}")
        }
    }

    fun clearArgs(intent: Intent, target: NavigationModel.Target) {
        log.d("clearPendingNavigation:$target > ${intent.getStringExtra(KEY)}")
        intent.getStringExtra(KEY)
            ?.takeIf { it == target.name }
            ?.also {
                when (target) {
                    PLAYLIST -> {
                        intent.removeExtra(KEY)
                        intent.removeExtra(PLAYLIST_ID.name)
                        intent.removeExtra(PLAYLIST_ITEM_ID.name)
                        intent.removeExtra(PLAY_NOW.toString())
                        intent.removeExtra(SOURCE.name)
                    }
                    else -> Unit
                }
            }
    }
}

fun Scope.navigationRouter(
    isFragment: Boolean,
    sourceActivity: Activity,
    withNavHost: Boolean = true
) = NavigationRouter(
    activity = sourceActivity,
    toastWrapper = ToastWrapper(sourceActivity),
    fragment = if (isFragment) (getSource() as Fragment) else null,
    ytJavaApi = YoutubeJavaApiWrapper(sourceActivity, get()),
    navController = if (withNavHost && sourceActivity is AppCompatActivity) {
        if (isFragment) {
            try {
                (getSource() as Fragment).findNavController()
            } catch (e: IllegalStateException) {
                null
            }
        } else {
            (sourceActivity
                .supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                .navController
        }
    } else null,
    urlLauncher = UrlLauncherWrapper(sourceActivity),
    log = AndroidLogWrapper()
)