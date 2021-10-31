package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.content.Intent
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
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerService.Companion.ACTION_INIT
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistItem

class FloatingPlayerController constructor(
    private val service: FloatingPlayerContract.Service,
    private val playerController: PlayerController,
    private val playerMviViw: FloatingWindowMviView,
    private val windowManagement: FloatingWindowManagement,
    private val aytViewHolder: AytViewHolder,
    private val log: LogWrapper,
) : FloatingPlayerContract.Controller {
    init {
        log.tag(this)
    }

    override fun initialise() {
        windowManagement.makeWindowWithView()
        playerMviViw.init()
        playerController.onViewCreated(listOf(playerMviViw))
        playerController.onStart()
    }

    override fun destroy() {
        playerMviViw.cleanup()
        playerController.onStop()
        playerController.onViewDestroyed()
        playerController.onDestroy()
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
                    ?.also { playerMviViw.dispatch(PlayerContract.View.Event.InitFromService(it)) }
            }
            ACTION_SKIPF ->
                playerMviViw.dispatch(PlayerContract.View.Event.SkipFwdClicked)
            ACTION_SKIPB ->
                playerMviViw.dispatch(PlayerContract.View.Event.SkipBackClicked)
            ACTION_PAUSE ->
                playerMviViw.dispatch(PlayerContract.View.Event.PlayPauseClicked(true))
            ACTION_PLAY ->
                playerMviViw.dispatch(PlayerContract.View.Event.PlayPauseClicked(false))
            ACTION_TRACKB ->
                playerMviViw.dispatch(PlayerContract.View.Event.TrackBackClicked)
            ACTION_TRACKF ->
                playerMviViw.dispatch(PlayerContract.View.Event.TrackFwdClicked)
            else -> {
                //notification.handleAction(intent.action)
                log.d("intent.action = ${intent.action}")
            }
        }
    }
}