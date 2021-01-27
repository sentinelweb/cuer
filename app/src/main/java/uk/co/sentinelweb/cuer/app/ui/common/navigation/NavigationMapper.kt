package uk.co.sentinelweb.cuer.app.ui.common.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.ytplayer.YoutubeActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class NavigationMapper constructor(
    private val activity: Activity,
    private val toastWrapper: ToastWrapper,
    private val fragment: Fragment? = null,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val navController: NavController,
    private val log: LogWrapper
) {

    fun map(nav: NavigationModel) {
        when (nav.target) {
            LOCAL_PLAYER ->
                nav.params[MEDIA_ID]?.let {
                    YoutubeActivity.start(activity, it.toString())
                } ?: throw IllegalArgumentException("$LOCAL_PLAYER: $MEDIA_ID param required")
            WEB_LINK ->
                nav.params[LINK]?.let {
                    val parse = Uri.parse(it.toString())
                    activity.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_VIEW, parse), "Launch ${parse.host}"
                        )
                    )
                } ?: throw IllegalArgumentException("$WEB_LINK: $LINK param required")
            NAV_BACK -> fragment?.findNavController()?.popBackStack()
                ?: throw IllegalStateException("Fragment unavailable")
            NAV_FINISH -> activity.finish()
            YOUTUBE_CHANNEL -> if (!ytJavaApi.launchChannel(nav.params[CHANNEL_ID] as String)) {
                toastWrapper.show("can't launch channel")
            }
            PLAYLIST_FRAGMENT -> navController.navigate(
                R.id.navigation_playlist,
                bundleOf(
                    PLAYLIST_ID.name to nav.params[PLAYLIST_ID],
                    PLAYLIST_ITEM_ID.name to nav.params[PLAYLIST_ITEM_ID],
                    PLAY_NOW.name to nav.params[PLAY_NOW]
                ),
                navOptions(optionsBuilder = {
                    launchSingleTop = true
                    popUpTo(R.id.navigation_playlist, { inclusive = true })
                }),
                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )
            PLAYLIST_ITEM_FRAGMENT -> navController.navigate(
                R.id.navigation_playlist_item_edit,
                bundleOf(PLAYLIST_ITEM.name to (nav.params[PLAYLIST_ITEM] as PlaylistItemDomain).serialise()),
                navOptions(optionsBuilder = {
                    launchSingleTop = true
                    popUpTo(R.id.navigation_playlist_edit, { inclusive = true })
                }),
                nav.params[FRAGMENT_NAV_EXTRAS] as FragmentNavigator.Extras?
            )
            else -> toastWrapper.show("Cannot launch ${nav.target}")
        }
    }

    fun clearArgs(intent: Intent, target: NavigationModel.Target) {
        log.d("clearPendingNavigation:$target > ${intent.getStringExtra(NavigationModel.Target.KEY)}")
        intent.getStringExtra(NavigationModel.Target.KEY)
            ?.takeIf { it == target.name }
            ?.also {
                when (target) {
                    PLAYLIST_FRAGMENT -> {
                        intent.removeExtra(NavigationModel.Target.KEY)
                        intent.removeExtra(PLAY_NOW.toString())
                        intent.removeExtra(PLAYLIST_ITEM.name)
                    }
                    else -> Unit
                }
            }
    }
}