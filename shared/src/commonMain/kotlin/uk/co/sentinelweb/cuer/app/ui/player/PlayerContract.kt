package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.*
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.UNKNOWN
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlayerContract {

    interface MviStore : Store<Intent, State, Label> {
        sealed class Intent {
            object Play : Intent()
            object Pause : Intent()
            object Load : Intent()
            object SkipFwd : Intent()
            object SkipBack : Intent()
            object TrackFwd : Intent()
            object TrackBack : Intent()
            object SkipFwdSelect : Intent()
            object SkipBackSelect : Intent()
            data class Position(val ms: Int) : Intent()
            data class PlayState(val state: PlayerStateDomain) : Intent()
            data class TrackChange(val item: PlaylistItemDomain) : Intent()
            data class PlaylistChange(val item: PlaylistDomain) : Intent()
        }

        sealed class Label {
            class Command(val command: PlayerCommand) : Label()
        }

        data class State constructor(
            val item: PlaylistItemDomain? = null,
            val playlist: PlaylistDomain? = null,
            val playerState: PlayerStateDomain = UNKNOWN,
            val skipFwdText: String = "-",
            val skipBackText: String = "-"
        )
    }

    interface View : MviView<View.Model, View.Event> {
        suspend fun processLabel(label: Label)
        data class Model(
            val texts: Texts,
            val platformId: String?,
            val playState: PlayerStateDomain,
            val nextTrackEnabled: Boolean,
            val prevTrackEnabled: Boolean
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
        }

        sealed class Event {
            object PlayClicked : Event()
            object PauseClicked : Event()
            object TrackFwdClicked : Event()
            object TrackBackClicked : Event()
            object SkipFwdClicked : Event()
            object SkipBackClicked : Event()
            object Initialised : Event()
            object SkipFwdSelectClicked : Event()
            object SkipBackSelectClicked : Event()
            data class SendPosition(val ms: Int) : Event()
            data class PlayerStateChanged(val state: PlayerStateDomain) : Event()
        }
    }

    sealed class PlayerCommand {
        object None : PlayerCommand()
        object Play : PlayerCommand()
        object Pause : PlayerCommand()
        data class SkipFwd(val ms: Int) : PlayerCommand()
        data class SkipBack(val ms: Int) : PlayerCommand()
        data class JumpTo(val ms: Long) : PlayerCommand()
    }

    interface PlaylistItemLoader {
        suspend fun load(): PlaylistItemDomain?
    }
}