package uk.co.sentinelweb.cuer.app.ui.playlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.playlist_fragment.*
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.Const
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.util.wrapper.AlertDialogWrapper

class PlaylistFragment :
    Fragment(R.layout.playlist_fragment),
    PlaylistContract.View,
    ItemContract.Interactions,
    ItemContract.ItemMoveInteractions {

    private val presenter: PlaylistContract.Presenter by currentScope.inject()
    private val adapter: PlaylistAdapter by currentScope.inject()
    private val alertWrapper: AlertDialogWrapper by currentScope.inject()
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
        activity?.intent?.getStringExtra(Const.EXTRA_YTID)?.let {
            presenter.setFocusId(it)
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
        playlist_list.scrollToPosition(index)
    }

    //endregion

    // region ItemContract.Interactions
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        // todo make move in queue
        //Collections.swap(data, fromPosition, toPosition)
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
        presenter.onItemSwipeLeft(item as PlaylistModel.PlaylistItemModel)
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
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get()
                    )
                }
                scoped { PlaylistModelMapper() }
                scoped { PlaylistAdapter(get(), getSource()) }
                scoped { ItemTouchHelperCallback(getSource()) }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped { AlertDialogWrapper((getSource() as Fragment).requireActivity()) }
                scoped { ItemFactory() }
                viewModel { PlaylistState() }
            }
        }
    }
}
