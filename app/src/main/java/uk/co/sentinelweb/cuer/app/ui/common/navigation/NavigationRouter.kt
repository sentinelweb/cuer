package uk.co.sentinelweb.cuer.app.ui.common.navigation

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.Companion.KEY
import uk.co.sentinelweb.cuer.app.ui.exoplayer.ExoActivity
import uk.co.sentinelweb.cuer.app.ui.share.ShareActivity
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_land.AytLandActivity
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait.AytPortraitActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class NavigationRouter (
    private val activity: Activity,
    private val toastWrapper: ToastWrapper,
    private val ytJavaApi: PlatformLaunchWrapper,
    private val navController: NavController?,
    private val log: LogWrapper,
    private val urlLauncher: UrlLauncherWrapper,
    private val cryptoLauncher: CryptoLauncher
) {

    fun navigate(nav: NavigationModel) {
        when (nav.target) {
            NAV_NONE -> Unit
            LOCAL_PLAYER_FULL ->
                (nav.params[PLAYLIST_AND_ITEM] as PlaylistAndItemDomain?)?.let {
                    //YoutubeFullScreenActivity.start(activity, it)
                    AytLandActivity.start(activity, it)
                }
                    ?: throw IllegalArgumentException("$LOCAL_PLAYER_FULL: $PLAYLIST_ITEM param required")

            LOCAL_PLAYER -> {
                (nav.params[PLAYLIST_AND_ITEM] as PlaylistAndItemDomain?)?.let {
                    AytPortraitActivity.start(activity, it)
                } ?: throw IllegalArgumentException("$LOCAL_PLAYER: $PLAYLIST_ITEM param required")
            }

            EXO_PLAYER_FULL -> {
                (nav.params[PLAYLIST_AND_ITEM] as PlaylistAndItemDomain?)?.let {
                    ExoActivity.start(activity, it)
                } ?: throw IllegalArgumentException("$LOCAL_PLAYER: $PLAYLIST_ITEM param required")
            }

            WEB_LINK ->
                nav.params[LINK]?.let {
                    urlLauncher.launchWithChooser(it.toString())
                } ?: throw IllegalArgumentException("$WEB_LINK: $LINK param required")

            CRYPTO_LINK ->
                nav.getParam<LinkDomain.CryptoLinkDomain>(CRYPTO_ADDRESS)
                    ?.let { cryptoLauncher.launch(it) }
                    ?: throw IllegalArgumentException("$CRYPTO_LINK: $CRYPTO_ADDRESS param required")

            NAV_BACK -> {
                nav.params[BACK_PARAMS]?.let {
                    navController?.popBackStack(it as Int, false)
                } ?: navController?.popBackStack()
            }

            NAV_FINISH -> activity.finish()
            YOUTUBE_CHANNEL -> if (!ytJavaApi.launchChannel(nav.params[CHANNEL_ID] as String)) {
                toastWrapper.show("can't launch channel")
            }

            YOUTUBE_VIDEO -> if (!ytJavaApi.launchVideoSystem(nav.params[PLATFORM_ID] as String)) {
                toastWrapper.show("can't launch channel")
            }

            YOUTUBE_VIDEO_POS ->
                (nav.params[PLAYLIST_ITEM] as? PlaylistItemDomain)?.media
                    ?.also {
                        if (!ytJavaApi.launchVideoWithTimeSystem(it)) {
                            toastWrapper.show("can't launch media with time")
                        }
                    }

            PLAYLIST -> navController?.navigate(
                R.id.navigation_playlist,
                bundleOf(
                    PLAYLIST_ID.name to nav.params[PLAYLIST_ID],
                    PLAYLIST_ITEM_ID.name to nav.params[PLAYLIST_ITEM_ID],
                    PLAY_NOW.name to (nav.params[PLAY_NOW] ?: false),
                    SOURCE.name to nav.params[SOURCE].toString() // todo remove source arg
                ),
            )

            PLAYLISTS -> navController?.navigate(
                R.id.navigation_playlists,
                bundleOf(
                    PLAYLIST_ID.name to nav.params[PLAYLIST_ID]
                ),
//                nav.navOpts,
//                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )

            NavigationModel.Target.PLAYLIST_ITEM -> navController?.navigate(
                R.id.navigation_playlist_item_edit,
                bundleOf(
                    PLAYLIST_ITEM.name to (nav.params[PLAYLIST_ITEM] as PlaylistItemDomain).serialise(),
                    SOURCE.name to nav.params[SOURCE].toString() // todo remove source arg
                ),
//                nav.navOpts,
//                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )

            PLAYLIST_EDIT -> navController?.navigate(
                R.id.navigation_playlist_edit,
                bundleOf(
                    PLAYLIST_ID.name to nav.params[PLAYLIST_ID],
                    SOURCE.name to nav.params[SOURCE].toString() // todo remove source arg
                ),
                /*nav.navOpts ?: */
                navOptions(optionsBuilder = {
                    launchSingleTop = true
                    popUpTo(R.id.navigation_playlists, { inclusive = false })
                }),
//                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )

            PLAYLIST_CREATE -> navController?.navigate(
                R.id.navigation_playlist_edit,
                bundleOf(
                    SOURCE.name to nav.params[SOURCE].toString()
                ),
                /*nav.navOpts ?: */
                navOptions(optionsBuilder = {
                    launchSingleTop = true
                    popUpTo(R.id.navigation_playlists, { inclusive = false })
                }),
                //nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )

            FOLDER_LIST -> navController?.navigate(
                R.id.navigation_folders,
                bundleOf(
                    REMOTE_ID.name to nav.params[REMOTE_ID].toString(),
                    FILE_PATH.name to nav.params[FILE_PATH]?.toString()
                ),
            )

            SHARE -> nav.getParam<String>(LINK)
                ?.let { activity.startActivity(ShareActivity.urlIntent(activity, it)) }
                ?: throw IllegalArgumentException("$SHARE: $LINK param required")

            else -> {
                log.e("Cannot launch ${nav}")
                toastWrapper.show("Cannot launch ${nav.target}")
            }
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

                    NavigationModel.Target.PLAYLIST_ITEM -> {
                        intent.removeExtra(KEY)
                        intent.removeExtra(PLAYLIST_ITEM.name)
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
    ytJavaApi = YoutubeJavaApiWrapper(sourceActivity, get()),
    navController = if (withNavHost && sourceActivity is AppCompatActivity) {
        if (isFragment) {
            try {
                (get() as Fragment).findNavController()
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
    log = get(),
    cryptoLauncher = AndroidCryptoLauncher(
        sourceActivity,
        get(),
        AlertDialogCreator(sourceActivity, get()),
        get(),
        get()
    )
)
