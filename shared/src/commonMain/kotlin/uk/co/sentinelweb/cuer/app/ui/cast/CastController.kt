package uk.co.sentinelweb.cuer.app.ui.cast

import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceContract
import uk.co.sentinelweb.cuer.app.ui.cast.CastDialogModel.CuerCastStatus
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.name

class CastController(
    private val cuerCastPlayerWatcher: CuerCastPlayerWatcher,
    private val chromeCastHolder: ChromecastContract.PlayerContextHolder,
    private val chromeCastDialogWrapper: ChromecastContract.DialogWrapper,
    private val chromeCastWrapper: ChromecastContract.Wrapper,
    private val floatingManager: FloatingPlayerContract.Manager,
    private val playerControls: PlayerContract.PlayerControls,
    private val castDialogLauncher: CastContract.CastDialogLauncher,
    private val ytServiceManager: YoutubeCastServiceContract.Manager,
) {

    fun showCastDialog() {
        castDialogLauncher.launchCastDialog()
    }

    fun checkCastConnectionToActivity() {
        ytServiceManager.stop()
        // todo check priorities maybe chromecast is lower?
        if (chromeCastHolder.isCreated() && chromeCastHolder.isConnected()) {
            chromeCastHolder.playerUi = playerControls
        } else if (floatingManager.isRunning()) {
            floatingManager.get()?.external?.mainPlayerControls = playerControls
        } else if (cuerCastPlayerWatcher.isWatching()) {
            cuerCastPlayerWatcher.mainPlayerControls = playerControls
        }
    }

    fun switchToNotification() {
        // todo handle cuer cast
        if (chromeCastHolder.isCreated() && !chromeCastHolder.isConnected()) {
            chromeCastHolder.destroy()
        } else {
            ytServiceManager.start()
        }
    }

    fun connectCuerCast(node: RemoteNodeDomain?) {
        cuerCastPlayerWatcher.remoteNode = node
        cuerCastPlayerWatcher.mainPlayerControls = node?.let { playerControls }
        castDialogLauncher.hideCastDialog()
    }

    suspend fun stopCuerCast() {
        cuerCastPlayerWatcher.sendStop()

    }

    fun connectChromeCast() {
        castDialogLauncher.hideCastDialog()
        if (!chromeCastHolder.isConnected()) {
            chromeCastDialogWrapper.showRouteSelector()
        }
    }

    fun disonnectChromeCast() {
        if (chromeCastHolder.isConnected()) {
            chromeCastWrapper.killCurrentSession()
        }
    }

    fun map(): CastDialogModel {
        val connectedStatus = if (cuerCastPlayerWatcher.isWatching()) {
            "Cuer: " + cuerCastPlayerWatcher.remoteNode?.name()
        } else if (chromeCastHolder.isConnected()) {
            "Chromecast: " + chromeCastWrapper.getCastDeviceName()
        } else "Not Connected"
        return CastDialogModel(
            connectedStatus,
            cuerCastPlayerWatcher.run {
                CuerCastStatus(
                    isWatching(),
                    remoteNode?.name(),
                    isPlaying()
                )
            },
            CastDialogModel.ChromeCastStatus(chromeCastHolder.isConnected(), chromeCastWrapper.getCastDeviceName()),
            floatingManager.isRunning()
        )
    }
}