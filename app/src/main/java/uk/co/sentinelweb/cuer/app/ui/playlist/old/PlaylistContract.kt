package uk.co.sentinelweb.cuer.app.ui.playlist.old

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.play.PlayDialog
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistHelpConfig
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.share.ShareCommitter
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

interface PlaylistContract {

    interface Presenter {
        val isCards: Boolean
        fun initialise()
        fun destroy()
        fun updatePlaylist()
        fun onItemSwipeRight(itemModel: PlaylistItemMviContract.Model.Item)
        fun onItemSwipeLeft(itemModel: PlaylistItemMviContract.Model.Item)
        fun onItemPlayClicked(itemModel: PlaylistItemMviContract.Model.Item)
        fun onItemPlay(itemModel: PlaylistItemMviContract.Model.Item, external: Boolean)
        fun onItemShowChannel(itemModel: PlaylistItemMviContract.Model.Item)
        fun onItemStar(itemModel: PlaylistItemMviContract.Model.Item)
        fun onItemRelated(itemModel: PlaylistItemMviContract.Model.Item)
        fun onItemShare(itemModel: PlaylistItemMviContract.Model.Item)
        fun onPlayStartClick(itemModel: PlaylistItemMviContract.Model.Item)
        fun onItemViewClick(itemModel: PlaylistItemMviContract.Model.Item)
        fun onItemGotoPlaylist(item: PlaylistItemMviContract.Model.Item)
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
        fun onResume()
        fun onPause()
        suspend fun commitPlaylist(onCommit: ShareCommitter.AfterCommit? = null)
        fun reloadHeader()
        fun undoMoveItem()
        fun setAddPlaylistParent(id: Long)
        fun checkToSave()
        fun onShowCards(cards: Boolean): Boolean
        fun onHelp()
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
        fun setList(items: List<PlaylistItemMviContract.Model.Item>, animate: Boolean)
        fun scrollToItem(index: Int)
        fun scrollTo(direction: ScrollDirection)
        fun showUndo(msg: String, undoFunction: () -> Unit)
        fun highlightPlayingItem(currentItemIndex: Int?)
        fun setSubTitle(subtitle: String)
        fun showPlaylistSelector(model: PlaylistsMviDialogContract.Config)
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
        fun showMessage(message: String)
        fun updateItemModel(model: PlaylistItemMviContract.Model.Item)
        fun navigate(nav: NavigationModel)
        fun newAdapter()
        fun getScrollIndex(): Int
        fun showHelp()

        val isHeadless: Boolean
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
        val loopModeText: String,
        @DrawableRes val playIcon: Int,
        val playText: String,
        @DrawableRes val starredIcon: Int,
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
        val items: List<PlaylistItemMviContract.Model.Item>?,
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
                scoped<View> { get<PlaylistFragment>() }
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
                        playlistMutator = get(),
                        log = get(),
                        timeProvider = get(),
                        coroutines = get(),
                        res = get(),
                        addPlaylistUsecase = get(),
                        playlistUpdateUsecase = get(),
                        playlistOrDefaultUsecase = get(),
                        dbInit = get(),
                        recentLocalPlaylists = get(),
                        itemMapper = get(),
                        playUseCase = get(),
                        multiPrefs = get(),
                        appPlaylistInteractors = get()
                    )
                }
                scoped { get<Presenter>() as External }
                scoped { PlaylistModelMapper(itemModelMapper = get(), iconMapper = get(), res = get()) }
                scoped<SnackbarWrapper> {
                    AndroidSnackbarWrapper(this.getFragmentActivity(), get())
                }
                scoped<PlatformLaunchWrapper> { YoutubeJavaApiWrapper(this.getFragmentActivity(), get()) }
                scoped { AndroidShareWrapper(this.getFragmentActivity()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped {
                    PlayUseCase(
                        queue = get(),
                        ytCastContextHolder = get(),
                        prefsWrapper = get(),
                        coroutines = get(),
                        floatingService = get(),
                        playDialog = get(),
                        strings = get()
                    )
                }
                scoped {
                    PlayDialog(
                        get<PlaylistFragment>(),
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
                scoped { PlaylistHelpConfig(get()) }
            }
        }
    }
}