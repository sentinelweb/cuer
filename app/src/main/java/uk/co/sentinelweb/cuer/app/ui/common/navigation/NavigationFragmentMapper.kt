package uk.co.sentinelweb.cuer.app.ui.common.navigation

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import uk.co.sentinelweb.cuer.app.R

@Suppress("unused")
class NavigationFragmentMapper constructor(
    private val activity: AppCompatActivity
) {
    fun makeFragment(@IdRes itemId: Int): NavigationModel.Target = when (itemId) {
        R.id.navigation_browse -> NavigationModel.Target.BROWSE
        R.id.navigation_playlists -> NavigationModel.Target.PLAYLISTS
        R.id.navigation_playlist -> NavigationModel.Target.PLAYLIST
        else -> throw IllegalArgumentException(
            "No model for ID :" + activity.resources.getResourceName(itemId)
        )
    }
}