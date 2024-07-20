package uk.co.sentinelweb.cuer.app.ui.cast

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromeCastPlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

class CastController(
    private val cuerCastPlayerWatcher: CuerCastPlayerWatcher,
    private val chromeCastHolder: ChromeCastPlayerContextHolder,
    private val floatingManager: FloatingPlayerContract.Manager,
    private val playerControls: PlayerContract.PlayerControls,
) {

    fun showCastDialog() {

    }

    fun checkCastConnection() {
        // todo check priorities maybe chromecast is lower?
        if (chromeCastHolder.isCreated() && chromeCastHolder.isConnected()) {
            chromeCastHolder.playerUi = playerControls
        } else if (floatingManager.isRunning()) {
            floatingManager.get()?.external?.mainPlayerControls = playerControls
        } else if (cuerCastPlayerWatcher.isWatching()) {
            cuerCastPlayerWatcher.mainPlayerControls = playerControls
        }
    }

    fun connectCuerCast(node: RemoteNodeDomain?) {
        cuerCastPlayerWatcher.remoteNode = node
        cuerCastPlayerWatcher.mainPlayerControls = node?.let { playerControls }
    }
}