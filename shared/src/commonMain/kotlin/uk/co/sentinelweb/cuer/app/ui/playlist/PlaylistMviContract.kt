package uk.co.sentinelweb.cuer.app.ui.playlist

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.resources.Icon
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract.ItemPassView
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract.Model.Item
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Config
import uk.co.sentinelweb.cuer.app.ui.share.ShareCommitter
import uk.co.sentinelweb.cuer.domain.*

class PlaylistMviContract {

    enum class UndoType { ItemDelete, ItemMove }

    interface MviStore : Store<Intent, State, Label> {
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
            var isHeadless: Boolean = false,
            val itemsIdMap: MutableMap<Long, PlaylistItemDomain> = mutableMapOf(),
        )

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
            data class MoveSwipe(val item: Item) : Intent()
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
            data class ShowChannel(val item: Item) : Intent()
            object CheckToSave : Intent()
            data class Commit(val afterCommit: ShareCommitter.AfterCommit) : Intent()
            data class ShowCards(val isCards: Boolean) : Intent()
            data class DeleteItem(val item: Item) : Intent()
            data class Undo(val undoType: UndoType) : Intent()
            data class StarItem(val item: Item) : Intent()
            data class PlayItem(
                val item: Item,
                val start: Boolean = false,
                val external: Boolean = false
            ) : Intent()

            data class ShareItem(val item: Item) : Intent()
            data class ShowItem(val item: Item) : Intent()
            data class RelatedItem(val item: Item) : Intent()
            data class GotoPlaylist(val item: Item) : Intent()
            data class PlaylistSelected(val playlist: PlaylistDomain) : Intent()
            data class UpdatesPlaylist(val op: Operation, val source: Source, val plist: PlaylistDomain) : Intent()
            data class UpdatesPlaylistItem(val op: Operation, val source: Source, val item: PlaylistItemDomain) :
                Intent()

            data class UpdatesMedia(val op: Operation, val source: Source, val media: MediaDomain) : Intent()
            data class QueueItemFlow(val item: PlaylistItemDomain?) : Intent()
            data class QueuePlaylistFlow(val item: PlaylistDomain?) : Intent()

            object CheckToSaveConfirm : Intent()
            object Headless : Intent()
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
            data class ItemRemoved(val model: Item) : Label()
            data class ScrollToItem(val pos: Int) : Label()
            data class HighlightPlayingItem(val pos: Int) : Label()
            data class UpdateModelItem(val model: Item) : Label()
            data class AfterCommit(
                val type: ObjectTypeDomain,
                val objects: List<Domain>,
                val afterCommit: ShareCommitter.AfterCommit?
            ) : Label()

            data class LaunchPlaylist(val platformId: String, val platform: PlatformDomain = PlatformDomain.YOUTUBE) :
                Label()

            data class LaunchChannel(val platformId: String, val platform: PlatformDomain = PlatformDomain.YOUTUBE) :
                Label()

            data class ShowItem(
                val modelId: Long,
                val item: PlaylistItemDomain,
                val source: OrchestratorContract.Source
            ) :
                Label()

            data class Share(val playlist: PlaylistDomain) : Label()
            data class ShareItem(val playlistItem: PlaylistItemDomain) : Label()
            data class PlayItem(val playlistItem: PlaylistItemDomain, val start: Boolean = false) : Label()
            data class CheckSaveShowDialog(val dialogModel: AlertDialogModel) : Label()
        }
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: Label)

        data class Model(
            val header: Header,
            val items: List<Item>?,
            val isCards: Boolean,
            val identifier: OrchestratorContract.Identifier<*>?,
            val playingIndex: Int?
        )

        data class Header(
            val title: String,
            val imageUrl: String,
            val loopModeIndex: Int,
            val loopModeIcon: Icon,
            val loopModeText: String,
            val playIcon: Icon,
            val playText: String,
            val starredIcon: Icon,
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
            val itemsText: String
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
            data class OnShowChannel(val item: Item) : Event()
            object OnShare : Event()
            object OnLaunch : Event()
            object OnCheckToSave : Event()
            data class OnCommit(val afterCommit: ShareCommitter.AfterCommit) : Event()
            data class OnPlaylistSelected(val playlist: PlaylistDomain) : Event()
            data class OnSetPlaylistData(
                val plId: Long? = null,
                val plItemId: Long? = null,
                val playNow: Boolean = false,
                val source: Source = Source.LOCAL,
                val addPlaylistParent: Long? = null
            ) : Event()

            data class OnShowCards(val isCards: Boolean) : Event()
            data class OnMove(val fromPosition: Int, val toPosition: Int) : Event()
            object OnClearMove : Event()
            data class OnMoveSwipe(val item: Item) : Event()
            data class OnDeleteItem(val item: Item) : Event()
            data class OnUndo(val undoType: UndoType) : Event()
            data class OnStarItem(val item: Item) : Event()
            data class OnPlayItem(
                val item: Item,
                val start: Boolean = false,
                val external: Boolean = false
            ) : Event()

            data class OnShareItem(val item: Item) : Event()
            data class OnShowItem(val item: Item) : Event()
            data class OnRelatedItem(val item: Item) : Event()
            data class OnGotoPlaylist(val item: Item) : Event()
            object OnCheckToSaveConfirm : Event()
        }
    }
}
