package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.Command
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder

class FloatingWindowMviView(
    private val service: FloatingPlayerService,
    private val aytViewHolder: AytViewHolder,
    private val windowManagement: FloatingWindowManagement,
    private val notification: PlayerControlsNotificationContract.External,
) : BaseMviView<Model, Event>(),
    PlayerContract.View {
    var mainPlayControls: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            if (field != null && value == null) {
                field?.removeListener(controlsListener)
            } else if (value != null) {
                value.addListener(controlsListener)
            }
            field = value
        }

    fun init() {
//        notification.setIcon(R.drawable.ic_picture_in_picture)
        notification.setIcon(R.drawable.ic_play_yang_combined)
        aytViewHolder.addView(service.baseContext, windowManagement.binding!!.playerContainer, this, false)
    }


    fun setTitlePrefix(prefix: String?) {
        notification.setTitlePrefix(prefix)
    }

    override val renderer: ViewRenderer<Model> = diff {
        diff(get = Model::playState, set = {
            notification.setPlayerState(it)
            mainPlayControls
                ?.apply { setPlayerState(it) }
        })
        diff(get = Model::playlistItem, set = { item ->
            notification.setPlaylistItem(item, LOCAL)
            mainPlayControls?.apply {
                item?.also { item ->
                    item.media.duration?.let { setDuration(it / 1000f) }
                    item.media.positon?.let { setCurrentSecond(it / 1000f) }
                    item.media.title?.let { setTitle(it) }
                    setPlaylistItem(item, LOCAL)
                }
            }
        })
    }

    override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
        when (label) {
            is Command -> {
                label.command.let { aytViewHolder.processCommand(it) }
            }

            else -> Unit
        }
    }

    fun cleanup() {
        mainPlayControls?.disconnectSource()
        mainPlayControls?.removeListener(controlsListener)
        mainPlayControls = null
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