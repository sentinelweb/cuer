package uk.co.sentinelweb.cuer.app.ui.playlist

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.resources.Icon
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

class PlaylistMviContract {

    enum class UndoType { ItemDelete }

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object Refresh : Intent()
//            object CreatePlaylist : Intent()
//            data class Move(val fromPosition: Int, val toPosition: Int) : Intent()
//            object ClearMove : Intent()
//            data class MoveSwipe(val item: PlaylistsItemMviContract.Model) : Intent()
//            data class OpenPlaylist(val item: PlaylistsItemMviContract.Model, val view: PlaylistsItemMviContract.ItemPassView? = null) : Intent()
//            data class Delete(val item: PlaylistsItemMviContract.Model) : Intent()
//            data class Undo(val undoType: UndoType) : Intent()
//            data class Play(val item: PlaylistsItemMviContract.Model, val external: Boolean, val view: PlaylistsItemMviContract.ItemPassView? = null) : Intent()
//            data class Star(val item: PlaylistsItemMviContract.Model) : Intent()
//            data class Share(val item: PlaylistsItemMviContract.Model) : Intent()
//            data class Merge(val item: PlaylistsItemMviContract.Model) : Intent()
//            data class Edit(val item: PlaylistsItemMviContract.Model, val view: PlaylistsItemMviContract.ItemPassView? = null) : Intent()
        }

        sealed class Label {
            data class Error(val message: String, val exception: Throwable? = null) : Label()
//            data class Message(val message: String) : Label()
//            object Repaint : Label()
//            data class ShowUndo(val undoType: UndoType, val message: String) : Label()
//            data class ShowPlaylistsSelector(val config: PlaylistsMviDialogContract.Config) : Label()
//            data class Navigate(val model: NavigationModel, val view: PlaylistsItemMviContract.ItemPassView? = null) : Label()
//            data class ItemRemoved(val model: PlaylistsItemMviContract.Model) : Label()
        }

        data class State(
            var playlistIdentifier: OrchestratorContract.Identifier<*> = OrchestratorContract.NO_PLAYLIST,
            var playlist: PlaylistDomain? = null,
            var deletedPlaylistItem: PlaylistItemDomain? = null,
            var movedPlaylistItem: PlaylistItemDomain? = null,
            var focusIndex: Int? = null,
            var dragFrom: Int? = null,
            var dragTo: Int? = null,
            var selectedPlaylistItem: PlaylistItemDomain? = null,
            var model: View.Model? = null,
            var playlistsTree: PlaylistTreeDomain? = null,
            var playlistsTreeLookup: Map<Long, PlaylistTreeDomain>? = null,
            var addPlaylistParent: Long? = null,
            var isModified: Boolean = false,
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val imageUrl: String,
            val loopModeIndex: Int,
            /*@DrawableRes*/ val loopModeIcon: Icon,
            val loopModeText: String,
            /*@DrawableRes*/ val playIcon: Icon,
            val playText: String,
            /*@DrawableRes*/ val starredIcon: Icon,
            val starredText: String,
            val isStarred: Boolean,
            val isDefault: Boolean,
            val isPlayFromStart: Boolean,
            val isPinned: Boolean,
            val isSaved: Boolean,
            val canPlay: Boolean,
            val canEdit: Boolean,
            val canUpdate: Boolean,
            val canDelete: Boolean,
            val canEditItems: Boolean,
            val canDeleteItems: Boolean,
            val items: List<PlaylistItemMviContract.Model>?,
            val itemsIdMap: MutableMap<Long, PlaylistItemDomain>,
            val hasChildren: Int,
        )


        sealed class Event {
            object OnRefresh : Event()
//            object OnCreatePlaylist : Event()
//            data class OnMove(val fromPosition: Int, val toPosition: Int) : Event()
//            object OnClearMove : Event()
//            data class OnMoveSwipe(val item: PlaylistsItemMviContract.Model) : Event()
//            data class OnDelete(val item: PlaylistsItemMviContract.Model) : Event()
//
//            data class OnUndo(val undoType: UndoType) : Event()
//            data class OnOpenPlaylist(
//                val item: PlaylistsItemMviContract.Model, val view: PlaylistsItemMviContract.ItemPassView? = null
//            ) :
//                Event()
//
//            data class OnPlay(
//                val item: PlaylistsItemMviContract.Model, val external: Boolean, val view: PlaylistsItemMviContract.ItemPassView? = null
//            ) : Event()
//
//            data class OnStar(val item: PlaylistsItemMviContract.Model) : Event()
//            data class OnShare(val item: PlaylistsItemMviContract.Model) : Event()
//            data class OnMerge(val item: PlaylistsItemMviContract.Model) : Event()
//            data class OnEdit(val item: PlaylistsItemMviContract.Model, val view: PlaylistsItemMviContract.ItemPassView? = null) : Event()
        }
    }

}