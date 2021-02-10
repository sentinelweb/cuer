package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import kotlinx.coroutines.Job
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlaylistContract {

    interface Presenter {
        fun initialise()
        fun destroy()
        fun refreshList()
        fun setFocusMedia(mediaDomain: MediaDomain)
        fun onItemSwipeRight(item: ItemContract.Model)
        fun onItemSwipeLeft(item: ItemContract.Model)
        fun onItemClicked(item: ItemContract.Model)
        fun onItemPlay(item: ItemContract.Model, external: Boolean)
        fun onItemShowChannel(item: ItemContract.Model)
        fun onItemStar(item: ItemContract.Model)
        fun onItemShare(item: ItemContract.Model)
        fun onPlayStartClick(item: ItemContract.Model)
        fun onItemViewClick(item: ItemContract.Model)
        fun moveItem(fromPosition: Int, toPosition: Int)
        fun scroll(direction: ScrollDirection)
        fun undoDelete()
        fun commitMove()
        fun setPlaylistData(plId: Long? = null, plItemId: Long? = null, playNow: Boolean = false, source: Source = LOCAL)
        fun onPlaylistSelected(playlist: PlaylistDomain)
        fun onPlayModeChange(): Boolean
        fun onPlayPlaylist(): Boolean
        fun onStarPlaylist(): Boolean
        fun onFilterNewItems(): Boolean
        fun onEdit(): Boolean
        fun onFilterPlaylistItems(): Boolean
        fun onResume()
        fun onPause()
    }

    interface View {
        fun setModel(model: Model, animate: Boolean = true)
        fun setHeaderModel(model: Model)
        fun setList(items: List<ItemContract.Model>, animate: Boolean)
        fun scrollToItem(index: Int)
        fun scrollTo(direction: ScrollDirection)
        fun playLocal(media: MediaDomain)
        fun showDeleteUndo(msg: String)
        fun highlightPlayingItem(currentItemIndex: Int?)
        fun setSubTitle(subtitle: String)
        fun showPlaylistSelector(model: SelectDialogModel)
        fun showPlaylistCreateDialog()
        fun showAlertDialog(model: AlertDialogModel)
        fun resetItemsState()
        fun showItemDescription(itemWitId: PlaylistItemDomain, source: Source)
        fun gotoEdit(id: Long, source: Source)
        fun showCastRouteSelectorDialog()
        fun setPlayState(state: PlayState)
        fun exit()
    }

    enum class ScrollDirection { Up, Down, Top, Bottom }
    enum class PlayState { PLAYING, CONNECTING, NOT_CONNECTED }

    data class State constructor(
        var playlistIdentifier: Identifier<*> = NO_PLAYLIST,
        var playlist: PlaylistDomain? = null,
        var deletedPlaylistItem: PlaylistItemDomain? = null,
        var focusIndex: Int? = null,
        var lastFocusIndex: Int? = null, // used for undo
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var selectedPlaylistItem: PlaylistItemDomain? = null,
        var queueItemJob: Job? = null
    ) : ViewModel()

    data class Model constructor(
        val title: String,
        val imageUrl: String,
        val loopModeIndex: Int,
        @DrawableRes val loopModeIcon: Int,
        @DrawableRes val playIcon: Int,
        @DrawableRes val starredIcon: Int,
        val isDefault: Boolean,
        val isSaved: Boolean,
        val canPlay: Boolean,
        val items: List<ItemContract.Model>?
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
                        playlistDialogModelCreator = get(),
                        timeProvider = get(),
                        coroutines = get(),
                        res = get()
                    )
                }
                scoped {
                    PlaylistModelMapper(
                        res = get(),
                        timeFormatter = get(),
                        timeSinceFormatter = get(),
                        loopModeMapper = get(),
                        backgroundMapper = get()
                    )
                }
                scoped { PlaylistAdapter(get(), getSource()) }
                scoped { ItemTouchHelperCallback(getSource()) }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper((getSource() as Fragment).requireActivity()) }
                scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ShareWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ItemFactory(get(), get()) }
                scoped { SelectDialogCreator((getSource() as Fragment).requireActivity()) }
                scoped { AlertDialogCreator((getSource() as Fragment).requireActivity()) }
                viewModel { State() }
            }

        }
    }
}