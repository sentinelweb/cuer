package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.annotation.DrawableRes
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
import uk.co.sentinelweb.cuer.app.ui.common.dialog.play.PlayDialog
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
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
        fun onItemPlayClicked(itemModel: ItemContract.Model)
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
        fun setPlaylistData(
            plId: Long? = null,
            plItemId: Long? = null,
            playNow: Boolean = false,
            source: Source = LOCAL
        )

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
        suspend fun commitPlaylist(onCommit: ShareContract.Committer.OnCommit? = null)
        fun reloadHeader()
        fun undoMoveItem()
        fun setAddPlaylistParent(id: Long)
        fun checkToSave()
    }

    interface Interactions {
        fun onPlayStartClick(item: PlaylistItemDomain)
        fun onRelated(item: PlaylistItemDomain)
        fun onView(item: PlaylistItemDomain)
        fun onPlay(item: PlaylistItemDomain)
    }

    interface View {
        val external: External
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
        fun setCastState(state: CastState)
        fun exit()
        fun hideRefresh()
        fun showRefresh()
        fun showError(message: String)
        fun updateItemModel(model: ItemContract.Model)
        fun navigate(nav: NavigationModel)
    }

    interface External {
        var interactions: Interactions?
    }

    enum class ScrollDirection { Up, Down, Top, Bottom }
    enum class CastState { PLAYING, CONNECTING, NOT_CONNECTED }

    data class State(
        var playlistIdentifier: Identifier<*> = NO_PLAYLIST,
        var playlist: PlaylistDomain? = null,
        var deletedPlaylistItem: PlaylistItemDomain? = null,
        var movedPlaylistItem: PlaylistItemDomain? = null,
        var focusIndex: Int? = null,
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var selectedPlaylistItem: PlaylistItemDomain? = null,
        var model: Model? = null,
        var playlistsTree: PlaylistTreeDomain? = null,
        var playlistsTreeLookup: Map<Long, PlaylistTreeDomain>? = null,
        var addPlaylistParent: Long? = null,
        var isModified: Boolean = false,
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
        val hasChildren: Int,
    )

    companion object {
        fun makeNav(
            plId: Long?,
            plItemId: Long? = null,
            play: Boolean,
            source: Source? = LOCAL,
            addPlaylistParent: Long? = null,
            imageUrl: String? = null
        ): NavigationModel {
            val params = mutableMapOf(
                PLAYLIST_ID to (plId ?: throw IllegalArgumentException("No Playlist Id")),
                NavigationModel.Param.PLAY_NOW to play,
                SOURCE to (source ?: throw IllegalArgumentException("No Source"))
            ).apply {
                plItemId?.also { put(NavigationModel.Param.PLAYLIST_ITEM_ID, it) }
            }.apply {
                addPlaylistParent?.also { put(NavigationModel.Param.PLAYLIST_PARENT, it) }
            }.apply {
                imageUrl?.also { put(NavigationModel.Param.IMAGE_URL, it) }
            }
            return NavigationModel(
                PLAYLIST, params
            )
        }

        @JvmStatic
        val fragmentModule = module {
            factory { ItemFactory(get(), get(), get()) }
            factory { ItemModelMapper(get(), get(), get(), get()) }
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
                        ytCastContextHolder = get(),
                        chromeCastWrapper = get(),
                        ytJavaApi = get(),
                        shareWrapper = get(),
                        prefsWrapper = get(),
                        playlistMutator = get(),
                        log = get(),
                        timeProvider = get(),
                        coroutines = get(),
                        res = get(),
                        playlistMediaLookupOrchestrator = get(),
                        playlistUpdateOrchestrator = get(),
                        playlistOrDefaultOrchestrator = get(),
                        dbInit = get(),
                        recentLocalPlaylists = get(),
                        itemMapper = get(),
                        playUseCase = get()
                    )
                }
                scoped { get<Presenter>() as External }
                scoped { PlaylistModelMapper(itemModelMapper = get(), iconMapper = get()) }
                scoped { PlaylistAdapter(get(), getSource()) }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped { ItemTouchHelperCallback(getSource()) }
                scoped<SnackbarWrapper> {
                    AndroidSnackbarWrapper(this.getFragmentActivity(), get())
                }
                scoped { YoutubeJavaApiWrapper(this.getFragmentActivity(),get()) }
                scoped { ShareWrapper(this.getFragmentActivity()) }
                scoped { AlertDialogCreator(this.getFragmentActivity()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped {
                    PlayUseCase(
                        queue = get(),
                        ytCastContextHolder = get(),
                        prefsWrapper = get(),
                        coroutines = get(),
                        floatingService = get(),
                        playDialog = get(),
                    )
                }
                scoped {
                    PlayDialog(
                        getSource(),
                        itemFactory = get(),
                        itemModelMapper = get(),
                        navigationRouter = get(),
                        castDialogWrapper = get(),
                        floatingService = get(),
                        log = get(),
                        alertDialogCreator = get(),
                        youtubeApi = get()
                    )
                }
                viewModel { State() }
            }
        }
    }
}