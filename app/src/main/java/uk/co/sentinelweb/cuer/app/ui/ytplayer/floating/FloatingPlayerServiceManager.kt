package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.app.Activity
import android.app.Application
import android.content.Intent
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class FloatingPlayerServiceManager(
    private val app: Application,
    private val overlayPermission: DisplayOverlayPermissionCheck,
) {
    fun hasPermission(a: Activity) = overlayPermission.checkOverlayDisplayPermission(a)

    fun start(a: Activity, load: PlaylistItemDomain): Boolean {
        if (!overlayPermission.checkOverlayDisplayPermission(a)) {
            overlayPermission.requestOverlayDisplayPermission(a)
        } else {
            if (!isRunning()) {
                // todo use foreground svc !!!
                app.startForegroundService(startIntent(load))
                //app.startService(startIntent(load))
                return true
            }
        }
        return false
    }

    fun stop() {
        if (isRunning()) {
            app.stopService(stopIntent())
        }
    }

    fun get(): FloatingPlayerService? = FloatingPlayerService.instance()

    fun isRunning(): Boolean = FloatingPlayerService.instance() != null

    fun playItem(item: PlaylistItemDomain) = app.startService(playIntent(item))

    // note this INIT intent doesnt get picked up by the MVI player for some reason
    // video seems to come from queue trackchange
    private fun startIntent(load: PlaylistItemDomain) =
        Intent(app, FloatingPlayerService::class.java)
            .setAction(FloatingPlayerService.ACTION_INIT)
            .putExtra(PLAYLIST_ITEM.toString(), load.serialise())

    private fun playIntent(load: PlaylistItemDomain) =
        Intent(app, FloatingPlayerService::class.java)
            .setAction(FloatingPlayerService.ACTION_PLAY_ITEM)
            .putExtra(PLAYLIST_ITEM.toString(), load.serialise())

    private fun stopIntent() = Intent(app, FloatingPlayerService::class.java)
}