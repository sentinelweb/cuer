package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.content.Intent
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.receiver.ScreenStateReceiver
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_PAUSE
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_PLAY
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_SKIPB
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_SKIPF
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_TRACKB
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_TRACKF
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_AND_ITEM
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerService.Companion.ACTION_INIT
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerService.Companion.ACTION_PLAY_ITEM
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistAndItem

class FloatingPlayerController constructor(
    private val service: FloatingPlayerContract.Service,
    private val playerController: PlayerController,
    private val playerMviViw: FloatingWindowMviView,
    private val windowManagement: FloatingWindowManagement,
    private val aytViewHolder: AytViewHolder,
    private val log: LogWrapper,
    private val screenStateReceiver: ScreenStateReceiver,
    private val toastWrapper: ToastWrapper,
    private val multiPrefs: MultiPlatformPreferencesWrapper,
) : FloatingPlayerContract.Controller, FloatingPlayerContract.External {

    init {
        log.tag(this)
    }

    override val external: FloatingPlayerContract.External
        get() = this

    override var mainPlayerControls: PlayerContract.PlayerControls?
        get() = playerMviViw.mainPlayControls
        set(value) {
            playerMviViw.mainPlayControls = value
        }

    private var wasPausedScreenLocked: Boolean = false

    private val screenLockHandler = {
        log.d("Screen off -> ACTION_PAUSE")
        playerMviViw.setBlocked(true)
        handleAction(Intent(ACTION_PAUSE))
        wasPausedScreenLocked = true
    }

    private val screenUnlockHandler = {
        log.d("Unlock -> ACTION_PLAY")
        playerMviViw.setBlocked(false)
        if (wasPausedScreenLocked && multiPrefs.restartAfterUnlock) {
            handleAction(Intent(ACTION_PLAY))
        }
        wasPausedScreenLocked = false
    }

    override fun initialise() {
        windowManagement.makeWindowWithView()
        windowManagement.callbacks = object : FloatingWindowManagement.Callbacks {
            override fun onClose() = service.stopSelf()
            override fun onLaunch() {
                playerMviViw.launchActivity()
                service.stopSelf()
            }

            override fun onPlayPause() {
                playerMviViw.dispatch(PlayPauseClicked())
            }
        }
        if (!BuildConfig.cuerBackgroundPlay) {
            screenStateReceiver.screenOffCallbacks.add(screenLockHandler)
            screenStateReceiver.unlockCallbacks.add(screenUnlockHandler)
        }
        playerMviViw.init()
        playerController.onViewCreated(listOf(playerMviViw))
        playerController.onStart()
    }

    override fun destroy() {
        if (!BuildConfig.cuerBackgroundPlay) {
            screenStateReceiver.screenOffCallbacks.remove(screenLockHandler)
            screenStateReceiver.unlockCallbacks.remove(screenUnlockHandler)
        }
        playerMviViw.cleanup()
        playerController.onStop()
        playerController.onViewDestroyed()
        playerController.onDestroy(aytViewHolder.willFinish())
        windowManagement.cleanup()
        aytViewHolder.cleanupIfNotSwitching()
    }

    override fun handleAction(intent: Any) {
        intent as Intent
        log.d("intent.action = ${intent.action}")
        when (intent.action) {
            ACTION_DISCONNECT -> {
                service.stopSelf()
            }

            ACTION_STAR -> Unit
            // log.d(serviceWrapper.getServiceData(YoutubeCastService::class.java.name).toString())
            ACTION_INIT -> {
                intent
                    .getStringExtra(PLAYLIST_AND_ITEM.toString())
                    ?.let { deserialisePlaylistAndItem(it) }
                    ?.also { playerMviViw.dispatch(OnInitFromService(it)) }
            }

            ACTION_PLAY_ITEM -> {
                intent
                    .getStringExtra(PLAYLIST_AND_ITEM.toString())
                    ?.let { deserialisePlaylistAndItem(it) }
                    ?.also { playerMviViw.dispatch(OnPlayItemFromService(it)) }
            }

            ACTION_SKIPF ->
                playerMviViw.dispatch(SkipFwdClicked)

            ACTION_SKIPB ->
                playerMviViw.dispatch(SkipBackClicked)

            ACTION_PAUSE ->
                playerMviViw.dispatch(PlayPauseClicked(true))

            ACTION_PLAY ->
                if (allowPlayInBackground()) {
                    playerMviViw.dispatch(PlayPauseClicked(false))
                } else {
                    toastWrapper.show("Unlock to play")
                }

            ACTION_TRACKB ->
                if (allowPlayInBackground()) {
                    playerMviViw.dispatch(TrackBackClicked)
                } else {
                    toastWrapper.show("Unlock to play")
                }

            ACTION_TRACKF ->
                if (allowPlayInBackground()) {
                    playerMviViw.dispatch(TrackFwdClicked)
                } else {
                    screenStateReceiver.requestUnlockKeyGuard()
                    toastWrapper.show("Unlock to play")
                }

            else -> {
                log.d("intent.action = ${intent.action}")
            }
        }
    }

    private fun allowPlayInBackground() = !screenStateReceiver.isLocked || BuildConfig.cuerBackgroundPlay
}
