package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.State.TargetDetails
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ControlTarget.FloatingWindow
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.Command
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.Stop
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait.AytPortraitActivity
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain

class FloatingWindowMviView(
    private val service: FloatingPlayerService,
    private val aytViewHolder: AytViewHolder,
    private val windowManagement: FloatingWindowManagement,
    private val notification: PlayerControlsNotificationContract.External,
    private val playerControls: PlayerContract.PlayerControls,
) : BaseMviView<Model, Event>(),
    PlayerContract.View {

    private var currentItem: PlaylistAndItemDomain? = null
    private var currentButtons: Model.Buttons? = null

    var mainPlayControls: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            if (field != null && value == null) {
                field?.removeListener(controlsListener)
            } else if (value != null) {
                value.addListener(controlsListener)
            }
            field = value
            currentButtons?.let { value?.setButtons(it) }
            field?.setCastDetails(TargetDetails(FloatingWindow))
        }

    fun init() {
//        notification.setIcon(R.drawable.ic_picture_in_picture)
        notification.setIcon(R.drawable.ic_play_yang_combined)
        aytViewHolder.addView(service.baseContext, windowManagement.binding.playerContainer, this, false)
    }


    fun setBlocked(blocked: Boolean) {
        notification.setBlocked(blocked)
    }

    override val renderer: ViewRenderer<Model> = diff {
        diff(get = Model::playState, set = {
            playerControls.setPlayerState(it)
            mainPlayControls
                ?.apply { setPlayerState(it) }
            windowManagement.setPlayerState(it)
        })
        diff(get = Model::texts, set = {
            it.playlistTitle
                ?.also { mainPlayControls?.setPlaylistName(it) }
        })
        diff(get = Model::playlistAndItem, set = { playlistAndItem ->
            currentItem = playlistAndItem
            playerControls.setPlaylistItem(playlistAndItem?.item)
            mainPlayControls?.apply {
                playlistAndItem?.item?.also { item ->
                    item.media.duration?.let { setDuration(it / 1000f) }
                    item.media.positon?.let { setCurrentSecond(it / 1000f) }
                    item.media.title?.let { setTitle(it) }
                    setPlaylistItem(item)
                }
            }
        })
        diff(get = Model::buttons, set = { buttons ->
            currentButtons = buttons
            mainPlayControls?.setButtons(buttons)
        })
    }

    override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
        when (label) {
            is Command -> {
                label.command.let { aytViewHolder.processCommand(it) }
            }
            is Stop -> service.stopSelf()
            else -> Unit
        }
    }

    fun cleanup() {
        mainPlayControls?.disconnectSource()
        mainPlayControls?.removeListener(controlsListener)
        mainPlayControls = null
    }

    fun launchActivity() {
        // fixme invert deps
        currentItem
            ?.apply { AytPortraitActivity.startFromService(service, this) }
            ?: MainActivity.startFromService(service)
    }

    private val controlsListener = object : PlayerContract.PlayerControls.Listener {
        override fun play() {
            dispatch(Event.PlayPauseClicked(null))
        }

        override fun pause() {
            dispatch(Event.PlayPauseClicked(null))
        }

        override fun trackBack() {
            dispatch(Event.TrackBackClicked)
        }

        override fun trackFwd() {
            dispatch(Event.TrackFwdClicked)
        }

        override fun seekTo(positionMs: Long) {
            dispatch(Event.OnSeekToPosition(positionMs))
        }

        override fun getLiveOffsetMs(): Long = 0

        override fun skipBack() {
            dispatch(Event.SkipBackClicked)
        }

        override fun skipFwd() {
            dispatch(Event.SkipFwdClicked)
        }
    }
}
