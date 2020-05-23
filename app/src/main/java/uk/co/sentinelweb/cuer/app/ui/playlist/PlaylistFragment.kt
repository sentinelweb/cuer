package uk.co.sentinelweb.cuer.app.ui.playlist

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.playlist_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.MEDIA
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.PLAY_NOW
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.ytplayer.YoutubeActivity
import uk.co.sentinelweb.cuer.app.util.extension.deserialiseMedia
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AlertDialogWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class PlaylistFragment :
    Fragment(R.layout.playlist_fragment),
    PlaylistContract.View,
    ItemContract.Interactions,
    ItemContract.ItemMoveInteractions {

    private val presenter: PlaylistContract.Presenter by currentScope.inject()
    private val adapter: PlaylistAdapter by currentScope.inject()
    private val alertWrapper: AlertDialogWrapper by currentScope.inject()
    private val toastWrapper: ToastWrapper by inject()
    private val itemTouchHelper: ItemTouchHelper by currentScope.inject()

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.initialise()
        playlist_list.layoutManager = LinearLayoutManager(context)
        playlist_list.adapter = adapter
        itemTouchHelper.attachToRecyclerView(playlist_list)
    }

    override fun onDestroyView() {
        presenter.destroy()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        presenter.loadList()
        // todo map in NavigationMapper
        activity?.intent?.getStringExtra(MEDIA.toString())?.let {
            val media = deserialiseMedia(it)
            presenter.setFocusMedia(media)
            if (activity?.intent?.getBooleanExtra(PLAY_NOW.toString(), false) ?: false) {
                presenter.playNow(media)
                activity?.intent?.removeExtra(PLAY_NOW.toString())
            }
        }
    }
    // endregion

    // region PlaylistContract.View
    override fun setList(list: List<ItemModel>) {
        playlist_swipe.isRefreshing = false
        adapter.data = list
        playlist_swipe.setOnRefreshListener { presenter.refreshList() }
    }

    override fun showAlert(msg: String) {
        alertWrapper.showMessage("Alert", msg)
    }

    override fun scrollToItem(index: Int) {
        (playlist_list.layoutManager as LinearLayoutManager).run {
            if (index !in this.findFirstCompletelyVisibleItemPosition()..this.findLastCompletelyVisibleItemPosition()) {
                playlist_list.scrollToPosition(index)
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
                scoped { AlertDialogWrapper((getSource() as Fragment).requireActivity()) }
                scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ShareWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ItemFactory() }
                viewModel { PlaylistState() }
            }
        }
    }
}
