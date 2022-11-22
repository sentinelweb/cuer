package uk.co.sentinelweb.cuer.app.ui.playlists

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

class PlaylistsMviContract {

    enum class UndoType { PlaylistDelete, SearchDelete }

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object Empty : Intent()
            object Refresh : Intent()
            object CreatePlaylist : Intent()
            data class Undo(val undoType: UndoType) : Intent()
            data class Move(val fromPosition: Int, val toPosition: Int) : Intent()
//            data class ClickCategory(val id: Long, val forceItem: Boolean) : Intent()
//            object Up : Intent()
//            object ActionSettings : Intent()
//            object ActionSearch : Intent()
//            object ActionHelp : Intent()
//            data class SetOrder(val order: BrowseContract.Order) : Intent()
        }

        sealed class Label {
            data class Error(val message: String, val exception: Throwable? = null) : Label()
            data class Message(val message: String) : Label()
            object Repaint : Label()
            data class ShowUndo(val undoType: UndoType, val message: String) : Label()
            data class ShowPlaylistsSelector(val config: PlaylistsMviDialogContract.Config) : Label()
            data class Navigate(val model: NavigationModel) : Label()
            data class ItemRemoved(val model: ItemMviContract.Model) : Label()
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
            var treeLookup: Map<Long, PlaylistTreeDomain> = mapOf(),
            val currentPlayingPlaylistId: Identifier<Long>? = null,
            val appLists: Map<PlaylistDomain, PlaylistStatDomain> = mapOf(),
            val recentPlaylists: List<Identifier<Long>> = listOf(),
            val pinnedPlaylistId: Long? = null,
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val imageUrl: String = "https://cuer-275020.firebaseapp.com/images/headers/headphones-2588235_640.jpg",
            val currentPlaylistId: Identifier<*>?, // todo non null?
            val items: List<ItemMviContract.Model>
        )


        sealed class Event {
            object OnRefresh : Event()
            object OnCreatePlaylist : Event()
            data class OnUndo(val undoType: UndoType) : Event()
            data class OnMove(val fromPosition: Int, val toPosition: Int) : Event()
            object OnCommitMove : Event()
            data class OnOpenPlaylist(val item: ItemMviContract.Model) : Event()
            data class OnMoveSwipe(val item: ItemMviContract.Model) : Event()
            data class OnDelete(val item: ItemMviContract.Model) : Event()
            data class OnPlay(val item: ItemMviContract.Model, val external: Boolean) : Event()
            data class OnStar(val item: ItemMviContract.Model) : Event()
            data class OnShare(val item: ItemMviContract.Model) : Event()
            data class OnMerge(val item: ItemMviContract.Model) : Event()
            data class OnEdit(val item: ItemMviContract.Model) : Event()

        }
    }

    interface Strings {
        val playlists_section_app: String
        val playlists_section_recent: String
        val playlists_section_starred: String
        val playlists_section_all: String
    }
}