package uk.co.sentinelweb.cuer.app.ui.common.navigation

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import uk.co.sentinelweb.cuer.app.R

class NavigationFragmentMapper constructor(
    private val activity: AppCompatActivity
) {
    // todo args
    fun makeFragment(@IdRes itemId: Int): NavigationModel.Target = when (itemId) {
        R.id.navigation_browse -> NavigationModel.Target.BROWSE_FRAGMENT
        R.id.navigation_playlists -> NavigationModel.Target.PLAYLISTS_FRAGMENT
        R.id.navigation_playlist -> NavigationModel.Target.PLAYLIST_FRAGMENT
        R.id.navigation_player -> NavigationModel.Target.PLAYER_FRAGMENT
        else -> throw IllegalArgumentException("No model for ID :" + activity.resources.getResourceName(itemId))
    }
}