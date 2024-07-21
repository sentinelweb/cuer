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
            chromeCastHolder.mainPlayerControls = playerControls
        } else if (floatingManager.isRunning()) {
            floatingManager.get()?.external?.mainPlayerControls = playerControls
        } else if (cuerCastPlayerWatcher.isWatching()) {
            cuerCastPlayerWatcher.mainPlayerControls = playerControls
        }
    }

    fun initialiseForService() {
        if (cuerCastPlayerWatcher.isWatching()) {
            cuerCastPlayerWatcher.mainPlayerControls = playerControls
        } else if (chromeCastHolder.isCreated() && chromeCastHolder.isConnected()) {
            chromeCastHolder.mainPlayerControls = playerControls
        }
    }

    // rules:
    // if cuercast then switch that to service - start svc
    // else if chromecast then switch to that - start svc
    // else - dont start service
    fun switchToService() {
        if (cuerCastPlayerWatcher.isWatching()) {
            cuerCastPlayerWatcher.mainPlayerControls = null
            ytServiceManager.start()
            chromeCastHolder.destroy() // kills any existing chromecast session
        } else if (chromeCastHolder.isCreated() && chromeCastHolder.isConnected()) {
            ytServiceManager.start()
        } else {
            cuerCastPlayerWatcher.cleanup()
            chromeCastHolder.destroy()
        }
    }

    // player controls is notification in the service instance of this class
    // todo check this is needed - possibly for floting player?
    fun onServiceDestroy() {
        if (chromeCastHolder.mainPlayerControls == playerControls) {
            chromeCastHolder.destroy()
        }
    }

    fun killCurrentSession() {
        chromeCastHolder.destroy()
        cuerCastPlayerWatcher.cleanup()
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
