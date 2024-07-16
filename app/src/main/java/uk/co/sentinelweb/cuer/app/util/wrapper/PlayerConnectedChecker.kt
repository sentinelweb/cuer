package uk.co.sentinelweb.cuer.app.util.wrapper

import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastYouTubePlayerContextHolder

class PlayerConnectedChecker(
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val floatingPlayerManager: FloatingPlayerContract.Manager,
) {
    fun isConnected() = ytContextHolder.isConnected() || floatingPlayerManager.isRunning()
}