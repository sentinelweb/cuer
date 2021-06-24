package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

interface PlaylistContract {

    interface Presenter {
        fun initialise()
        fun destroy()
        fun refreshPlaylist()
        fun onItemSwipeRight(itemModel: ItemContract.Model)
        fun onItemSwipeLeft(itemModel: ItemContract.Model)
        fun onItemClicked(itemModel: ItemContract.Model)
        fun onItemPlay(itemModel: ItemContract.Model, external: Boolean)
        fun onItemShowChannel(itemModel: ItemContract.Model)
        fun onItemStar(itemModel: ItemContract.Model)
        fun onItemRelated(itemModel: ItemContract.Model)
        fun onItemShare(itemModel: ItemContract.Model)
        fun onPlayStartClick(itemModel: ItemContract.Model)
        fun onItemViewClick(itemModel: ItemContract.Model)
        fun onItemGotoPlaylist(item: ItemContract.Model)
        fun moveItem(fromPosition: Int, toPosition: Int)
        fun scroll(direction: ScrollDirection)
        fun undoDelete()
        fun commitMove()
        fun setPlaylistData(plId: Long? = null, plItemId: Long? = null, playNow: Boolean = false, source: Source = LOCAL)
        fun onPlaylistSelected(playlist: PlaylistDomain, selected: Boolean)
        fun onPlayModeChange(): Boolean
        fun onPlayPlaylist(): Boolean
        fun onStarPlaylist(): Boolean
        fun onFilterNewItems(): Boolean
        fun onEdit(): Boolean
        fun onFilterPlaylistItems(): Boolean
        fun onShowChildren(): Boolean
        fun onResume()
        fun onPause()
        suspend fun commitPlaylist(onCommit: ShareContract.Committer.OnCommit)
        fun reloadHeader()
        fun undoMoveItem()
    }

    interface View {
        fun setModel(model: Model, animate: Boolean = true)
        fun setHeaderModel(model: Model)
        fun setList(items: List<ItemContract.Model>, animate: Boolean)
        fun scrollToItem(index: Int)
        fun scrollTo(direction: ScrollDirection)
        fun showUndo(msg: String, undoFunction: () -> Unit)
        fun highlightPlayingItem(currentItemIndex: Int?)
        fun setSubTitle(subtitle: String)
        fun showPlaylistSelector(model: PlaylistsDialogContract.Config)
        fun showPlaylistCreateDialog()
        fun showAlertDialog(model: AlertDialogModel)
        fun resetItemsState()
        fun showItemDescription(modelId: Long, item: PlaylistItemDomain, source: Source)
        fun gotoEdit(id: Long, source: Source)
        fun showCastRouteSelectorDialog()
        fun setPlayState(state: PlayState)
        fun exit()
        fun hideRefresh()
        fun showRefresh()
        fun showError(message: String)
        fun updateItemModel(model: ItemContract.Model)
        fun navigate(nav: NavigationModel)
    }

    enum class ScrollDirection { Up, Down, Top, Bottom }
    enum class PlayState { PLAYING, CONNECTING, NOT_CONNECTED }

    data class State(
        var playlistIdentifier: Identifier<*> = NO_PLAYLIST,
        var playlist: PlaylistDomain? = null,
        var deletedPlaylistItem: PlaylistItemDomain? = null,
        var movedPlaylistItem: PlaylistItemDomain? = null,
        var focusIndex: Int? = null,
        var lastFocusIndex: Int? = null, // used for undo
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var selectedPlaylistItem: PlaylistItemDomain? = null,
        var model: Model? = null,
        var playlistsTree: PlaylistTreeDomain? = null,
        var playlistsTreeLookup: Map<Long, PlaylistTreeDomain>? = null
    ) : ViewModel()

    data class Model constructor(
        val title: String,
        val imageUrl: String,
        val loopModeIndex: Int,
        @DrawableRes val loopModeIcon: Int,
        @DrawableRes val playIcon: Int,
        @DrawableRes val starredIcon: Int,
        val isDefault: Boolean,
        val isPlayFromStart: Boolean,
        val isPinned: Boolean,
        val isSaved: Boolean,
        val canPlay: Boolean,
        val canEdit: Boolean,
        val items: List<ItemContract.Model>?,
        val itemsIdMap: MutableMap<Long, PlaylistItemDomain>,
        val hasChildren: Int
    )

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistFragment>()) {
                scoped<View> { getSource() }
                scoped<Presenter> {
                    PlaylistPresenter(
                        view = get(),
                        state = get(),
                        mediaOrchestrator = get(),
                        playlistOrchestrator = get(),
                        playlistItemOrchestrator = get(),
                        modelMapper = get(),
                        queue = get(),
                        toastWrapper = get(),
                        ytContextHolder = get(),
                        chromeCastWrapper = get(),
                        ytJavaApi = get(),
                        shareWrapper = get(),
                        prefsWrapper = get(named<GeneralPreferences>()),
                        playlistMutator = get(),
                        log = get(),
                        timeProvider = get(),
                        coroutines = get(),
                        res = get(),
                        playlistMediaLookupOrchestrator = get(),
                        playlistUpdateOrchestrator = get()
                    )
                }
                scoped {
                    PlaylistModelMapper(
                        res = get(),
                        timeFormatter = get(),
                        timeSinceFormatter = get(),
                        iconMapper = get(),
                        backgroundMapper = get()
                    )
                }
                scoped { PlaylistAdapter(get(), getSource()) }
                scoped { ItemTouchHelperCallback(getSource()) }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper((getSource() as Fragment).requireActivity(), get()) }
                scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ShareWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ItemFactory(get(), get()) }
                scoped { AlertDialogCreator((getSource() as Fragment).requireActivity()) }
                scoped { navigationMapper(true, getSource<Fragment>().requireActivity() as AppCompatActivity) }
                viewModel { State() }
            }

        }
    }
}