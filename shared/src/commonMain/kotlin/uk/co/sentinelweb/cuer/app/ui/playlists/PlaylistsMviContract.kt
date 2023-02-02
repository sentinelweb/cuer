package uk.co.sentinelweb.cuer.app.ui.playlists

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.ItemPassView
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Model
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.domain.*

class PlaylistsMviContract {

    enum class UndoType { PlaylistDelete, SearchDelete }

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object Refresh : Intent()
            object CreatePlaylist : Intent()
            data class Move(val fromPosition: Int, val toPosition: Int) : Intent()
            object ClearMove : Intent()
            data class MoveSwipe(val item: Model) : Intent()
            data class OpenPlaylist(val item: Model, val view: ItemPassView? = null) : Intent()
            data class Delete(val item: Model) : Intent()
            data class Undo(val undoType: UndoType) : Intent()
            data class Play(val item: Model, val external: Boolean, val view: ItemPassView? = null) : Intent()
            data class Star(val item: Model) : Intent()
            data class Share(val item: Model) : Intent()
            data class Merge(val item: Model) : Intent()
            data class Edit(val item: Model, val view: ItemPassView? = null) : Intent()
        }

        sealed class Label {
            data class Error(val message: String, val exception: Throwable? = null) : Label()
            data class Message(val message: String) : Label()
            object Repaint : Label()
            data class ShowUndo(val undoType: UndoType, val message: String) : Label()
            data class ShowPlaylistsSelector(val config: PlaylistsMviDialogContract.Config) : Label()
            data class Navigate(val model: NavigationModel, val view: ItemPassView? = null) : Label()
            data class ItemRemoved(val model: Model) : Label()
        }

        data class State(
            var playlists: List<PlaylistDomain> = listOf(),
            var deletedItem: Domain? = null,
            var dragFrom: Int? = null,
            var dragTo: Int? = null,
            var playlistStats: List<PlaylistStatDomain> = listOf(),
            var treeRoot: PlaylistTreeDomain = PlaylistTreeDomain(),
            var treeLookup: Map<Identifier<GUID>, PlaylistTreeDomain> = mapOf(),
            val currentPlayingPlaylistId: Identifier<GUID>? = null,
            val appLists: Map<PlaylistDomain, PlaylistStatDomain> = mapOf(),
            val recentPlaylists: List<Identifier<GUID>> = listOf(),
            val pinnedPlaylistId: GUID? = null,
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val imageUrl: String = "https://cuer-275020.firebaseapp.com/images/headers/headphones-2588235_640.jpg",
            val currentPlaylistId: Identifier<*>?, // todo non null?
            val items: List<PlaylistsItemMviContract.Model>
        )


        sealed class Event {
            object OnRefresh : Event()
            object OnCreatePlaylist : Event()
            data class OnMove(val fromPosition: Int, val toPosition: Int) : Event()
            object OnClearMove : Event()
            data class OnMoveSwipe(val item: PlaylistsItemMviContract.Model) : Event()
            data class OnDelete(val item: PlaylistsItemMviContract.Model) : Event()

            data class OnUndo(val undoType: UndoType) : Event()
            data class OnOpenPlaylist(
                val item: PlaylistsItemMviContract.Model, val view: ItemPassView? = null
            ) :
                Event()

            data class OnPlay(
                val item: PlaylistsItemMviContract.Model, val external: Boolean, val view: ItemPassView? = null
            ) : Event()

            data class OnStar(val item: PlaylistsItemMviContract.Model) : Event()
            data class OnShare(val item: PlaylistsItemMviContract.Model) : Event()
            data class OnMerge(val item: PlaylistsItemMviContract.Model) : Event()
            data class OnEdit(val item: PlaylistsItemMviContract.Model, val view: ItemPassView? = null) : Event()
        }
    }

    open class Strings {
        // todo move to StringResources
        open val playlists_section_app: String = "App"
        open val playlists_section_recent: String = "Recent"
        open val playlists_section_starred: String = "Starred"
        open val playlists_section_all: String = "App"
        open val search_local: String = "Local"
        open val search_youtube: String = "Youtube"
        open fun playlists_message_deleted(title: String) = "Deleted playlist: ${title}"
        open fun playlists_message_deleted_search(type: String) = "Deleted $type search"
        open val playlists_error_cant_backup = "Cannot load playlist backup"
        open val playlists_error_load_failed = "Load failed"
        open val playlists_error_cant_delete = "Cannot delete playlist"
        open val playlists_error_delete_children = "Please delete the children first"
        open val playlists_error_delete_default = "Please choose another default playlist before deleting"
        open val playlist_dialog_title = "Select playlist"
        open val playlists_error_circular = "That's a circular reference ..."
        open val playlists_error_cant_play = "Cannot launch playlist"
        open val playlists_error_cant_load = "Couldn't load playlist ..."
        open val playlists_error_cant_merge = "Cannot merge this playlist"
    }
}
