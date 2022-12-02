package uk.co.sentinelweb.cuer.app.ui.playlist

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.resources.Icon
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract.ItemPassView
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract.Model
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Config
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

class PlaylistMviContract {

    enum class UndoType { ItemDelete, ItemMove }

    interface MviStore : Store<Intent, State, Label> {
        sealed class Intent {
            object Refresh : Intent()
            data class SetPlaylistData(
                val plId: Long? = null,
                val plItemId: Long? = null,
                val playNow: Boolean = false,
                val source: Source = Source.LOCAL,
                val addPlaylistParent: Long? = null
            ) : Intent()

            data class Move(val fromPosition: Int, val toPosition: Int) : Intent()
            object CommitMove : Intent()
            data class MoveSwipe(val item: Model.Item) : Intent()
            object Update : Intent()
            object PlayModeChange : Intent()
            object Play : Intent()
            object Edit : Intent()
            object Star : Intent()
            object Help : Intent()
            object Resume : Intent()
            object Pause : Intent()
            object Share : Intent()
            object Launch : Intent()
            object ShowChannel : Intent()
            object CheckToSave : Intent()
            object Commit : Intent()
            data class ShowCards(val isCards: Boolean) : Intent()
            data class DeleteItem(val item: Model.Item) : Intent()
            data class Undo(val undoType: UndoType) : Intent()
            data class StarItem(val item: Model.Item) : Intent()
            data class PlayItem(
                val item: Model.Item,
                val start: Boolean = false,
                val external: Boolean = false
            ) : Intent()

            data class ShareItem(val item: Model.Item) : Intent()
            data class ShowItem(val item: Model.Item) : Intent()
            data class RelatedItem(val item: Model.Item) : Intent()
            data class GotoPlaylist(val item: Model.Item) : Intent()
            data class PlaylistSelected(val playlist: PlaylistDomain) : Intent()
            data class UpdatesPlaylist(val op: Operation, val source: Source, val plist: PlaylistDomain) : Intent()
            data class UpdatesPlaylistItem(val op: Operation, val source: Source, val item: PlaylistItemDomain) :
                Intent()

            data class UpdatesMedia(val op: Operation, val source: Source, val media: MediaDomain) : Intent()
        }

        sealed class Label {
            data class Error(val message: String, val exception: Throwable? = null) : Label()
            data class Message(val message: String) : Label()
            object Loading : Label()
            object Loaded : Label()
            object Help : Label()
            data class ShowUndo(val undoType: UndoType, val message: String) : Label()
            data class ShowPlaylistsSelector(val config: Config) : Label()
            object ShowPlaylistsCreator : Label()
            object ResetItemState : Label()
            data class Navigate(val model: NavigationModel, val view: ItemPassView? = null) : Label()
            data class ItemRemoved(val model: Model.Item) : Label()
            data class ScrollToItem(val pos: Int) : Label()
            data class HighlightPlayingItem(val pos: Int) : Label()
            data class UpdateModelItem(val model: Model.Item) : Label()
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
            var playlistsTree: PlaylistTreeDomain? = null,
            var playlistsTreeLookup: Map<Long, PlaylistTreeDomain>? = null,
            var addPlaylistParent: Long? = null,
            var isModified: Boolean = false,
            var isCards: Boolean = false,
            val itemsIdMap: MutableMap<Long, PlaylistItemDomain> = mutableMapOf(),
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: Label)

        data class Model(
            val header: Header,
            val items: List<PlaylistItemMviContract.Model.Item>?,
            var isCards: Boolean,
        )

        data class Header(
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
            val hasChildren: Int,
        )


        sealed class Event {
            object OnRefresh : Event()
            object OnUpdate : Event()
            object OnPlayModeChange : Event()
            object OnPlay : Event()
            object OnEdit : Event()
            object OnStar : Event()
            object OnHelp : Event()
            object OnResume : Event()
            object OnPause : Event()
            object OnShowChannel : Event()
            object OnShare : Event()
            object OnLaunch : Event()
            object OnCheckToSave : Event()
            object OnCommit : Event()
            data class OnPlaylistSelected(val playlist: PlaylistDomain) : Event()
            data class OnSetPlaylistData(
                val plId: Long? = null,
                val plItemId: Long? = null,
                val playNow: Boolean = false,
                val source: Source = Source.LOCAL,
                val addPlaylistParent: Long? = null
            ) : Event()

            data class OnShowCards(val isCards: Boolean) : Event()

            //            object OnCreatePlaylist : Event()
            data class OnMove(val fromPosition: Int, val toPosition: Int) : Event()
            object OnClearMove : Event()
            data class OnMoveSwipe(val item: PlaylistItemMviContract.Model.Item) : Event()
            data class OnDeleteItem(val item: PlaylistItemMviContract.Model.Item) : Event()
            data class OnUndo(val undoType: UndoType) : Event()
            data class OnStarItem(val item: PlaylistItemMviContract.Model.Item) : Event()
            data class OnPlayItem(
                val item: PlaylistItemMviContract.Model.Item,
                val start: Boolean = false,
                val external: Boolean = false
            ) : Event()

            data class OnShareItem(val item: PlaylistItemMviContract.Model.Item) : Event()
            data class OnShowItem(val item: PlaylistItemMviContract.Model.Item) : Event()
            data class OnRelatedItem(val item: PlaylistItemMviContract.Model.Item) : Event()
            data class OnGotoPlaylist(val item: PlaylistItemMviContract.Model.Item) : Event()
//            data class OnShare(val item: PlaylistsItemMviContract.Model) : Event()
//            data class OnMerge(val item: PlaylistsItemMviContract.Model) : Event()
//            data class OnEdit(val item: PlaylistsItemMviContract.Model, val view: PlaylistsItemMviContract.ItemPassView? = null) : Event()
        }
    }

}