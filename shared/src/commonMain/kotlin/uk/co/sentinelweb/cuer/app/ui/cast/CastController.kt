package uk.co.sentinelweb.cuer.app.ui.cast

import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceContract
import uk.co.sentinelweb.cuer.app.ui.cast.CastDialogModel.CuerCastStatus
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.name
import uk.co.sentinelweb.cuer.shared.generated.resources.Res
import uk.co.sentinelweb.cuer.shared.generated.resources.cast_control_floating_window
import uk.co.sentinelweb.cuer.shared.generated.resources.cast_control_not_connected
import uk.co.sentinelweb.cuer.shared.generated.resources.cast_control_unknown

class CastController(
    private val cuerCastPlayerWatcher: CuerCastPlayerWatcher,
    private val chromeCastHolder: ChromecastContract.PlayerContextHolder,
    private val chromeCastDialogWrapper: ChromecastContract.DialogWrapper,
    private val chromeCastWrapper: ChromecastContract.Wrapper,
    private val floatingManager: FloatingPlayerContract.Manager,
    private val playerControls: PlayerContract.PlayerControls,
    private val castDialogLauncher: CastContract.CastDialogLauncher,
    private val ytServiceManager: YoutubeCastServiceContract.Manager,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
) {

    init {
        log.tag(this)
    }
    fun showCastDialog() {
        castDialogLauncher.launchCastDialog()
    }

    fun checkCastConnectionToActivity() {
        ytServiceManager.stop()
        if (cuerCastPlayerWatcher.isWatching()) {
            cuerCastPlayerWatcher.mainPlayerControls = playerControls
        } else if (chromeCastHolder.isCreated() && chromeCastHolder.isConnected()) {
            chromeCastHolder.mainPlayerControls = playerControls
        } else if (floatingManager.isRunning()) {
            floatingManager.get()?.external?.mainPlayerControls = playerControls
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

    fun getVolume(): Float = // 0..1
        if (cuerCastPlayerWatcher.isWatching()) {
            cuerCastPlayerWatcher.volume / cuerCastPlayerWatcher.volumeMax
        } else if (chromeCastHolder.isConnected()) {
            (chromeCastWrapper.getVolume() / chromeCastWrapper.getMaxVolume()).toFloat()
        } else 0f

    fun setVolume(volume: Float) { // 0 .. 1
        if (cuerCastPlayerWatcher.isWatching() && volume < 1) {
            val newVolume = volume * cuerCastPlayerWatcher.volumeMax
            log.d("cuercast newVolume:$newVolume max=${cuerCastPlayerWatcher.volumeMax}")
            cuerCastPlayerWatcher.sendVolume(newVolume)
        } else if (chromeCastHolder.isConnected() && volume < 1) {
            val maxVolume = chromeCastWrapper.getMaxVolume().toFloat()
            val newVolume = volume * maxVolume
            log.d("chromecast newVolume:$newVolume max=${maxVolume}")
            chromeCastWrapper.setVolume(newVolume)
        }
    }


    suspend fun map(): CastDialogModel = withContext(coroutines.Main) {
//        chromeCastWrapper.logCastDevice()
//        chromeCastWrapper.logRoutes()
        // goes into the dialog header - player data is mapped lower down
        val connectedStatus = if (cuerCastPlayerWatcher.isWatching()) {
            cuerCastPlayerWatcher.remoteNode?.name() ?: getString(Res.string.cast_control_unknown)
        } else if (chromeCastHolder.isConnected()) {
            chromeCastWrapper.getCastDeviceName() ?: getString(Res.string.cast_control_unknown)
        } else if (floatingManager.isRunning()) {
            getString(Res.string.cast_control_floating_window)
        } else getString(Res.string.cast_control_not_connected)

        CastDialogModel(
            connectedStatus,
            cuerCastPlayerWatcher.run {
                CuerCastStatus(
                    isWatching(),
                    remoteNode?.name(),
                    isPlaying(),
                    if (isWatching()) {
                        (volume / volumeMax * 100).toInt().toString() + " %"
                    } else {
                        "--"
                    },
                )
            },
            CastDialogModel.ChromeCastStatus(
                chromeCastHolder.isConnected(),
                chromeCastWrapper.getCastDeviceName(),
                if (chromeCastHolder.isConnected()) {
                    (chromeCastWrapper.getVolume() / chromeCastWrapper.getMaxVolume() * 100).toInt().toString() + " %"
                } else {
                    "--"
                },
            ),
            floatingManager.isRunning()
        )
    }
}
