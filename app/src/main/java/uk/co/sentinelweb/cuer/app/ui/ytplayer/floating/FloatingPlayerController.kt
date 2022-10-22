package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.content.Intent
import uk.co.sentinelweb.cuer.app.receiver.ScreenStateReceiver
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_PAUSE
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_PLAY
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_SKIPB
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_SKIPF
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_TRACKB
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_TRACKF
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerService.Companion.ACTION_INIT
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerService.Companion.ACTION_PLAY_ITEM
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistItem

class FloatingPlayerController constructor(
    private val service: FloatingPlayerContract.Service,
    private val playerController: PlayerController,
    private val playerMviViw: FloatingWindowMviView,
    private val windowManagement: FloatingWindowManagement,
    private val aytViewHolder: AytViewHolder,
    private val log: LogWrapper,
    private val screenStateReceiver: ScreenStateReceiver,
    private val toastWrapper: ToastWrapper
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

    override fun initialise() {
        windowManagement.makeWindowWithView()
        windowManagement.callbacks = object : FloatingWindowManagement.Callbacks {
            override fun onClose() = service.stopSelf()
        }
        playerMviViw.init()
        playerController.onViewCreated(listOf(playerMviViw))
        playerController.onStart()
    }

    override fun destroy() {
        playerMviViw.cleanup()
        playerController.onStop()
        playerController.onViewDestroyed()
        playerController.onDestroy(aytViewHolder.willFinish())
        windowManagement.cleanup()
        aytViewHolder.cleanupIfNotSwitching()
    }

    override fun handleAction(intent: Intent) {
        log.d("intent.action = ${intent.action}")
        when (intent.action) {
            ACTION_DISCONNECT -> {
                service.stopSelf()
            }
            ACTION_STAR -> Unit
            // log.d(serviceWrapper.getServiceData(YoutubeCastService::class.java.name).toString())
            ACTION_INIT -> {
                intent
                    .getStringExtra(NavigationModel.Param.PLAYLIST_ITEM.toString())
                    ?.let { deserialisePlaylistItem(it) }
                    ?.also { playerMviViw.dispatch(OnInitFromService(it)) }
            }
            ACTION_PLAY_ITEM -> {
                intent
                    .getStringExtra(NavigationModel.Param.PLAYLIST_ITEM.toString())
                    ?.let { deserialisePlaylistItem(it) }
                    ?.also { playerMviViw.dispatch(OnInitFromService(it)) }
            }

            ACTION_SKIPF ->
                playerMviViw.dispatch(SkipFwdClicked)

            ACTION_SKIPB ->
                playerMviViw.dispatch(SkipBackClicked)

            ACTION_PAUSE ->
                playerMviViw.dispatch(PlayPauseClicked(true))

            ACTION_PLAY ->
                if (!screenStateReceiver.isLocked) {
                    playerMviViw.dispatch(PlayPauseClicked(false))
                } else {
                    toastWrapper.show("Unlock to play")
                }

            ACTION_TRACKB ->
                if (!screenStateReceiver.isLocked) {
                    playerMviViw.dispatch(TrackBackClicked)
                } else {
                    toastWrapper.show("Unlock to play")
                }

            ACTION_TRACKF ->
                if (!screenStateReceiver.isLocked) {
                    playerMviViw.dispatch(TrackFwdClicked)
                } else {
                    toastWrapper.show("Unlock to play")
                }

            else -> {
                log.d("intent.action = ${intent.action}")
            }
        }
    }

    override fun setTitlePrefix(prefix: String?) {
        playerMviViw.setTitlePrefix(prefix)
    }
}