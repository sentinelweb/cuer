package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.State
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlayerContract {

    interface MviStore : Store<Intent, State, Nothing> {

        sealed class Intent {
            object Play : Intent()
            object Pause : Intent()
            object Load : Intent()

            //            object SkipFwd : Intent()
//            object SkipBack : Intent()
            object TrackFwd : Intent()
            object TrackBack : Intent()
            data class PlayState(val state: PlayerStateDomain) : Intent()
            data class TrackChange(val item: PlaylistItemDomain) : Intent()
        }

        data class State constructor(
            val item: PlaylistItemDomain? = null,
            val state: PlayerStateDomain = PlayerStateDomain.UNKNOWN,
            val command: PlayerCommand = PlayerCommand.NONE
        )


    }

    interface View : MviView<View.Model, View.Event> {

        data class Model(
            val title: String?,
            val platformId: String?,
            val playState: PlayerStateDomain,
            val playCommand: PlayerCommand
        )

        sealed class Event {
            object PlayClicked : Event()
            object PauseClicked : Event()
            object TrackFwdClicked : Event()
            object TrackBackClicked : Event()
            object Initialised : Event()
            data class PlayerStateChanged(val state: PlayerStateDomain) : Event()
        }
    }

    enum class PlayerCommand {
        NONE, PLAY, PAUSE
    }

    interface PlaylistItemLoader {
        suspend fun load(): PlaylistItemDomain?
    }

}