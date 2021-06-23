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

            //            object SkipFwd : Intent()
//            object SkipBack : Intent()
//            object TrackFwd : Intent()
//            object TrackBack : Intent()
            data class PlayState(val state: PlayerStateDomain) : Intent()
            data class LoadVideo(val playlistItemId: Long) : Intent()
        }

        data class State constructor(
            val item: PlaylistItemDomain? = null,
            val state: PlayerStateDomain = PlayerStateDomain.UNKNOWN
        )

    }

    interface View : MviView<View.Model, View.Event> {

        data class Model(
            val title: String,
            val url: String,
            val playState: PlayerStateDomain
        )

        sealed class Event {
            object PlayClicked : Event()
            object PauseClicked : Event()
            object LoadClicked : Event()
        }
    }

    interface PlaylistItemLoader {
        suspend fun load(id: Long): PlaylistItemDomain
    }

    interface PlaylistItemIdParser {
        val id: Long
    }

}