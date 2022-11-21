package uk.co.sentinelweb.cuer.app.ui.playlists

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

class PlaylistsMviContract {


    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
//            object Display : Intent()
//            data class ClickCategory(val id: Long, val forceItem: Boolean) : Intent()
//            object Up : Intent()
//            object ActionSettings : Intent()
//            object ActionSearch : Intent()
//            object ActionHelp : Intent()
//            data class SetOrder(val order: BrowseContract.Order) : Intent()
        }

        sealed class Label {
//            object None : Label()
//            data class Error(val message: String, val exception: Throwable? = null) : Label()
//            object TopReached : Label()
//            object ActionSettings : Label()
//            object ActionSearch : Label()
//            object ActionHelp : Label()
//
//            data class AddPlaylist(
//                val cat: CategoryDomain,
//                val parentId: Long? = null,
//            ) : Label()
//
//            data class OpenLocalPlaylist(val id: Long, val play: Boolean = false) : Label()
        }

        data class State(
            var playlists: List<PlaylistDomain> = listOf(),
            var deletedPlaylist: PlaylistDomain? = null,
            var dragFrom: Int? = null,
            var dragTo: Int? = null,
            var playlistStats: List<PlaylistStatDomain> = listOf(),
            var treeRoot: PlaylistTreeDomain = PlaylistTreeDomain(),
            var treeLookup: Map<Long, PlaylistTreeDomain> = mapOf()
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val imageUrl: String = "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
            val currentPlaylistId: OrchestratorContract.Identifier<*>?, // todo non null?
            val items: List<ItemMviContract.Model>
        )



        sealed class Event {
//            object OnResume : Event()
//            object OnUpClicked : Event()
//            object OnActionSettingsClicked : Event()
//            object OnActionSearchClicked : Event()
//            object OnActionHelpClicked : Event()
//            data class OnCategoryClicked(val model: CategoryModel) : Event()
//            data class OnSetOrder(val order: BrowseContract.Order) : Event()
        }
    }

    interface Strings {
//        val allCatsTitle: String
//        val recent: String
//        val errorNoPlaylistConfigured: String
//        fun errorNoCatWithID(id: Long): String
    }
}