package uk.co.sentinelweb.cuer.app.ui.playlists

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.playlists_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity.Companion.TOP_LEVEL_DESTINATIONS
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModel
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper

class PlaylistsFragment :
    Fragment(R.layout.playlists_fragment),
    PlaylistsContract.View,
    ItemContract.Interactions,
    ItemBaseContract.ItemMoveInteractions {

    private val presenter: PlaylistsContract.Presenter by currentScope.inject()
    private val adapter: PlaylistsAdapter by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()
    private val toastWrapper: ToastWrapper by inject()
    private val itemTouchHelper: ItemTouchHelper by currentScope.inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()

    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlists_toolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
            it.setupWithNavController(findNavController(), AppBarConfiguration(TOP_LEVEL_DESTINATIONS))
        }

        //presenter.initialise()
        playlists_list.layoutManager = LinearLayoutManager(context)
        playlists_list.adapter = adapter
        itemTouchHelper.attachToRecyclerView(playlists_list)
//        playlist_fab_up.setOnClickListener { presenter.scroll(Up) }
//        playlist_fab_up.setOnLongClickListener { presenter.scroll(Top);true }
//        playlist_fab_down.setOnClickListener { presenter.scroll(Down) }
//        playlist_fab_down.setOnLongClickListener { presenter.scroll(Bottom);true }
    }

    override fun onDestroyView() {
        presenter.destroy()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }
    // endregion

    // region PlaylistContract.View
    override fun setList(model: PlaylistsModel, animate: Boolean) {
        Glide.with(playlists_header_image)
            .load(imageProvider.makeRef(model.imageUrl))
            .into(playlists_header_image)
        playlists_swipe.isRefreshing = false
        playlists_items.text = "${model.items.size}"
        adapter.currentPlaylistId = model.currentPlaylistId
        adapter.setData(model.items, animate)
        playlists_swipe.setOnRefreshListener { presenter.refreshList() }
    }

    override fun showDeleteUndo(msg: String) {
        snackbar = snackbarWrapper.make(msg, length = Snackbar.LENGTH_LONG, actionText = "UNDO") {
            presenter.undoDelete()
            snackbar?.dismiss()
        }
        snackbar?.show()
    }

    override fun gotoPlaylist(id: Long, play: Boolean) {
        PlaylistsFragmentDirections.actionGotoPlaylist(id, play)
            .apply { findNavController().navigate(this) }
    }

    override fun gotoEdit(id: Long) {
        PlaylistsFragmentDirections.actionEditPlaylist(id)
            .apply { findNavController().navigate(this) }
    }

    override fun scrollToItem(index: Int) {
        (playlists_list.layoutManager as LinearLayoutManager).run {
            val useIndex = if (index > 0 && index < adapter.data.size) {
                index
            } else 0

            if (index !in this.findFirstCompletelyVisibleItemPosition()..this.findLastCompletelyVisibleItemPosition()) {
                playlists_list.scrollToPosition(useIndex)
            }
        }
    }
    //endregion

    // region ItemContract.ItemMoveInteractions
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        presenter.moveItem(fromPosition, toPosition)
        // shows the move while dragging
        adapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemClear() {
        presenter.commitMove()
    }
    //endregion

    // region ItemContract.Interactions
    override fun onClick(item: ItemModel) {
        presenter.onItemClicked(item)
    }

    override fun onRightSwipe(item: ItemModel) {
        presenter.onItemSwipeRight(item)
    }

    override fun onLeftSwipe(item: ItemModel) {
        val playlistItemModel = item
        adapter.notifyItemRemoved(playlistItemModel.index)
        presenter.onItemSwipeLeft(playlistItemModel) // delays for animation
    }

    override fun onPlay(item: ItemModel, external: Boolean) {
        presenter.onItemPlay(item, external)
    }

    override fun onStar(item: ItemModel) {
        presenter.onItemStar(item)
    }

    override fun onShare(item: ItemModel) {
        presenter.onItemShare(item)
    }
    //endregion

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistsFragment>()) {
                scoped<PlaylistsContract.View> { getSource() }
                scoped<PlaylistsContract.Presenter> {
                    PlaylistsPresenter(
                        view = get(),
                        state = get(),
                        playlistRepository = get(),
                        modelMapper = get(),
                        queue = get(),
                        log = get(),
                        toastWrapper = get(),
                        prefsWrapper = get(named<GeneralPreferences>())
                    )
                }
                scoped { PlaylistsModelMapper() }
                scoped { PlaylistsAdapter(get(), getSource()) }
                scoped {
                    ItemTouchHelperCallback(
                        getSource()
                    )
                }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped { SnackbarWrapper((getSource() as Fragment).requireActivity()) }
                scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ShareWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ItemFactory() }
                viewModel { PlaylistsState() }
            }
        }

    }

//    override fun scrollTo(direction: PlaylistsContract.ScrollDirection) {
//        (playlist_list.layoutManager as LinearLayoutManager).run {
//            val itemsOnScreen =
//                this.findLastCompletelyVisibleItemPosition() - this.findFirstCompletelyVisibleItemPosition()
//
//            val useIndex = when (direction) {
//                Up -> max(this.findFirstCompletelyVisibleItemPosition() - itemsOnScreen, 0)
//                Down ->
//                    min(
//                        this.findLastCompletelyVisibleItemPosition() + itemsOnScreen,
//                        adapter.data.size - 1
//                    )
//                Top -> 0
//                Bottom -> adapter.data.size - 1
//            }
//            if (abs(this.findLastCompletelyVisibleItemPosition() - useIndex) > 20)
//                playlist_list.scrollToPosition(useIndex)
//            else {
//                playlist_list.smoothScrollToPosition(useIndex)
//            }
//        }
//    }

}
