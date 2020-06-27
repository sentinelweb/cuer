package uk.co.sentinelweb.cuer.app.ui.playlist

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.playlist_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.MEDIA
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.PLAY_NOW
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract.ScrollDirection.*
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.ytplayer.YoutubeActivity
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseMedia
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PlaylistFragment :
    Fragment(R.layout.playlist_fragment),
    PlaylistContract.View,
    ItemContract.Interactions,
    ItemContract.ItemMoveInteractions {

    private val presenter: PlaylistContract.Presenter by currentScope.inject()
    private val adapter: PlaylistAdapter by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()
    private val toastWrapper: ToastWrapper by inject()
    private val itemTouchHelper: ItemTouchHelper by currentScope.inject()

    private var snackbar: Snackbar? = null

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.initialise()
        playlist_list.layoutManager = LinearLayoutManager(context)
        playlist_list.adapter = adapter
        itemTouchHelper.attachToRecyclerView(playlist_list)
        playlist_fab_up.setOnClickListener { presenter.scroll(Up) }
        playlist_fab_up.setOnLongClickListener { presenter.scroll(Top);true }
        playlist_fab_down.setOnClickListener { presenter.scroll(Down) }
        playlist_fab_down.setOnLongClickListener { presenter.scroll(Bottom);true }
    }

    override fun onDestroyView() {
        presenter.destroy()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()

        // todo map in NavigationMapper
        activity?.intent?.getStringExtra(MEDIA.toString())?.let {
            val media = deserialiseMedia(it)
            presenter.setFocusMedia(media)
            if (activity?.intent?.getBooleanExtra(PLAY_NOW.toString(), false) ?: false) {
                presenter.playNow(media)
                activity?.intent?.removeExtra(PLAY_NOW.toString())
            }
        } ?: presenter.loadList() // queue refresh triggered from shareactivity
    }
    // endregion

    // region PlaylistContract.View
    override fun setList(list: List<ItemModel>) {
        playlist_swipe.isRefreshing = false
        adapter.data = list
        playlist_swipe.setOnRefreshListener { presenter.refreshList() }
    }

    override fun showDeleteUndo(msg: String) {
        snackbar = snackbarWrapper.make(msg, length = Snackbar.LENGTH_LONG, actionText = "UNDO") {
            presenter.undoDelete()
            snackbar?.dismiss()
        }
        snackbar?.show()
    }

    override fun scrollTo(direction: PlaylistContract.ScrollDirection) {
        (playlist_list.layoutManager as LinearLayoutManager).run {
            val itemsOnScreen =
                this.findLastCompletelyVisibleItemPosition() - this.findFirstCompletelyVisibleItemPosition()

            val useIndex = when (direction) {
                Up -> max(this.findFirstCompletelyVisibleItemPosition() - itemsOnScreen, 0)
                Down ->
                    min(
                        this.findLastCompletelyVisibleItemPosition() + itemsOnScreen,
                        adapter.data.size - 1
                    )
                Top -> 0
                Bottom -> adapter.data.size - 1
            }
            if (abs(this.findLastCompletelyVisibleItemPosition() - useIndex) > 20)
                playlist_list.scrollToPosition(useIndex)
            else {
                playlist_list.smoothScrollToPosition(useIndex)
            }
        }
    }

    override fun scrollToItem(index: Int) {
        (playlist_list.layoutManager as LinearLayoutManager).run {
            val useIndex = if (index > 0 && index < adapter.data.size) {
                index
            } else 0

            if (index !in this.findFirstCompletelyVisibleItemPosition()..this.findLastCompletelyVisibleItemPosition()) {
                playlist_list.scrollToPosition(useIndex)
            }
        }
    }

    override fun playLocal(media: MediaDomain) {
        YoutubeActivity.start(requireContext(), media.mediaId)
    }
    //endregion

    // region ItemContract.ItemMoveInteractions
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        presenter.moveItem(fromPosition, toPosition)
        // shows the move while dragging
        adapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }

    //endregion

    // region ItemContract.Interactions
    override fun onClick(item: ItemModel) {
        presenter.onItemClicked(item as PlaylistModel.PlaylistItemModel)
    }

    override fun onRightSwipe(item: ItemModel) {
        presenter.onItemSwipeRight(item as PlaylistModel.PlaylistItemModel)
    }

    override fun onLeftSwipe(item: ItemModel) {
        val playlistItemModel = item as PlaylistModel.PlaylistItemModel
        adapter.notifyItemRemoved(playlistItemModel.index)
        presenter.onItemSwipeLeft(playlistItemModel) // delays for animation
    }

    override fun onPlay(item: ItemModel, external: Boolean) {
        presenter.onItemPlay(item as PlaylistModel.PlaylistItemModel, external)
    }

    override fun onShowChannel(item: ItemModel) {
        presenter.onItemShowChannel(item as PlaylistModel.PlaylistItemModel)
    }

    override fun onStar(item: ItemModel) {
        presenter.onItemStar(item as PlaylistModel.PlaylistItemModel)
    }

    override fun onShare(item: ItemModel) {
        presenter.onItemShare(item as PlaylistModel.PlaylistItemModel)
    }

    //endregion

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistFragment>()) {
                scoped<PlaylistContract.View> { getSource() }
                scoped<PlaylistContract.Presenter> {
                    PlaylistPresenter(
                        view = get(),
                        state = get(),
                        repository = get(),
                        modelMapper = get(),
                        contextProvider = get(),
                        queue = get(),
                        toastWrapper = get(),
                        ytInteractor = get(),
                        ytContextHolder = get(),
                        ytJavaApi = get(),
                        shareWrapper = get()
                    )
                }
                scoped { PlaylistModelMapper() }
                scoped { PlaylistAdapter(get(), getSource()) }
                scoped { ItemTouchHelperCallback(getSource()) }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped { SnackbarWrapper((getSource() as Fragment).requireActivity()) }
                scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ShareWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ItemFactory() }
                viewModel { PlaylistState() }
            }
        }

    }
}
