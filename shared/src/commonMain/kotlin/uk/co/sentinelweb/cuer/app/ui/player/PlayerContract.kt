package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract.DescriptionModel
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.*
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.UNKNOWN
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlayerContract {

    interface MviStore : Store<Intent, State, Label> {
        sealed class Intent {
            object Play : Intent()
            object Pause : Intent()
            object SkipFwd : Intent()
            object SkipBack : Intent()
            object TrackFwd : Intent()
            object TrackBack : Intent()
            object SkipFwdSelect : Intent()
            object SkipBackSelect : Intent()
            object PlaylistView : Intent()
            object PlaylistItemView : Intent()
            object ChannelOpen : Intent()
            object FullScreenPlayerOpen : Intent()
            object PortraitPlayerOpen : Intent()
            object PipPlayerOpen : Intent()
            data class PlayPause(val isPlaying: Boolean?) : Intent()
            data class Position(val ms: Long) : Intent()
            data class PlayState(val state: PlayerStateDomain) : Intent()
            data class TrackChange(val item: PlaylistItemDomain) : Intent()
            data class TrackSelected(val item: PlaylistItemDomain, val resetPosition: Boolean) : Intent()
            data class PlaylistChange(val item: PlaylistDomain) : Intent()
            data class SeekTo(val fraction: Float) : Intent()
            data class LinkOpen(val url: String) : Intent()
            data class Duration(val ms: Long) : Intent()
            data class Id(val videoId: String) : Intent()
        }

        sealed class Label {
            data class Command(val command: PlayerCommand) : Label()
            data class LinkOpen(val url: String) : Label()
            data class ChannelOpen(val channel: ChannelDomain) : Label()
            data class FullScreenPlayerOpen(val item: PlaylistItemDomain) : Label()
            data class PortraitPlayerOpen(val item: PlaylistItemDomain) : Label()
            data class PipPlayerOpen(val item: PlaylistItemDomain) : Label()
        }

        enum class Screen { DESCRIPTION, PLAYLIST, PLAYLISTS }

        data class State constructor(
            val item: PlaylistItemDomain? = null,
            val playlist: PlaylistDomain? = null,
            val playerState: PlayerStateDomain = UNKNOWN,
            val skipFwdText: String = "-",
            val skipBackText: String = "-",
            val screen: Screen = Screen.DESCRIPTION,
            val position: Long = -1
        )
    }

    interface View : MviView<View.Model, View.Event> {
        suspend fun processLabel(label: Label)
        data class Model(
            val texts: Texts,
            val playState: PlayerStateDomain,
            val nextTrackEnabled: Boolean,
            val prevTrackEnabled: Boolean,
            val times: Times,
            val itemImage: String?,
            val description: DescriptionModel,
            val screen: Screen
        ) {
            data class Texts(
                val title: String?,
                val playlistTitle: String?,
                val playlistData: String?,
                val nextTrackText: String?,
                val lastTrackText: String?,
                val skipFwdText: String?,
                val skipBackText: String?,
            )

            data class Times constructor(
                val positionText: String,
                val durationText: String,
                val isLive: Boolean,
                val seekBarFraction: Float
            )
        }

        sealed class Event {
            object PlayClicked : Event()
            object PauseClicked : Event()
            object TrackFwdClicked : Event()
            object TrackBackClicked : Event()
            object SkipFwdClicked : Event()
            object SkipBackClicked : Event()
            object SkipFwdSelectClicked : Event()
            object SkipBackSelectClicked : Event()
            object ItemClicked : Event()
            object PlaylistClicked : Event()
            object FullScreenClick : Event()
            object PortraitClick : Event()
            object PipClick : Event()
            object ChannelClick : Event()

            data class SeekBarChanged(val fraction: Float) : Event()
            data class PlayPauseClicked(val isPlaying: Boolean? = null) : Event()
            data class PositionReceived(val ms: Long) : Event()
            data class PlayerStateChanged(val state: PlayerStateDomain) : Event()
            data class TrackClick(val item: PlaylistItemDomain, val resetPosition: Boolean) : Event()
            data class LinkClick(val url: String) : Event()
            data class DurationReceived(val ms: Long) : Event()
            data class IdReceived(val videoId: String) : Event()
        }
    }

    sealed class PlayerCommand {
        object Play : PlayerCommand()
        object Pause : PlayerCommand()
        data class Load(val platformId: String, val startPosition: Long) : PlayerCommand()
        data class SkipFwd(val ms: Int) : PlayerCommand()
        data class SkipBack(val ms: Int) : PlayerCommand()
        data class SeekTo(val ms: Long) : PlayerCommand()
    }

    interface PlaylistItemLoader {
        fun load(): PlaylistItemDomain?
    }
}