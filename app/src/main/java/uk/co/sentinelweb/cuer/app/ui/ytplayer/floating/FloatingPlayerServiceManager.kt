package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.app.Activity
import android.app.Application
import android.content.Intent
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_AND_ITEM
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class FloatingPlayerServiceManager(
    private val app: Application,
    private val overlayPermission: DisplayOverlayPermissionCheck,
) : FloatingPlayerContract.Manager {
    fun hasPermission(a: Activity) = overlayPermission.checkOverlayDisplayPermission(a)
    fun requestPermission(a: Activity) = overlayPermission.requestOverlayDisplayPermission(a)

    fun start(a: Activity, load: PlaylistAndItemDomain): Boolean {
        if (!overlayPermission.checkOverlayDisplayPermission(a)) {
            overlayPermission.requestOverlayDisplayPermission(a)
        } else {
            if (!isRunning()) {
                app.startForegroundService(startIntent(load))
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

    override fun get(): FloatingPlayerService? = FloatingPlayerService.instance()

    override fun isRunning(): Boolean = FloatingPlayerService.instance() != null

    override fun playItem(item: PlaylistAndItemDomain) {
        app.startService(playIntent(item))
    }

    // note this INIT intent doesnt get picked up by the MVI player for some reason
    // video seems to come from queue trackchange
    private fun startIntent(load: PlaylistAndItemDomain) =
        Intent(app, FloatingPlayerService::class.java)
            .setAction(FloatingPlayerService.ACTION_INIT)
            .putExtra(PLAYLIST_AND_ITEM.toString(), load.serialise())

    private fun playIntent(load: PlaylistAndItemDomain) =
        Intent(app, FloatingPlayerService::class.java)
            .setAction(FloatingPlayerService.ACTION_PLAY_ITEM)
            .putExtra(PLAYLIST_AND_ITEM.toString(), load.serialise())

    private fun stopIntent() = Intent(app, FloatingPlayerService::class.java)
}