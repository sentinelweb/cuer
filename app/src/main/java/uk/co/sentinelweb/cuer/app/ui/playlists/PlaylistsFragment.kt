package uk.co.sentinelweb.cuer.app.ui.playlists

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.PlaylistsFragmentBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.firebase.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlaylistsFragment :
    Fragment(),
    PlaylistsContract.View,
    ItemContract.Interactions,
    ItemBaseContract.ItemMoveInteractions {

    private val presenter: PlaylistsContract.Presenter by currentScope.inject()
    private val adapter: PlaylistsAdapter by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()
    private val toastWrapper: ToastWrapper by inject()
    private val itemTouchHelper: ItemTouchHelper by currentScope.inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()
    private val log: LogWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()

    private var _binding: PlaylistsFragmentBinding? = null
    private val binding get() = _binding!!

    private val searchMenuItem: MenuItem
        get() = binding.playlistsToolbar.menu.findItem(R.id.playlists_search)

    private var snackbar: Snackbar? = null

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = PlaylistsFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playlistsToolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
        }

        //presenter.initialise()
        binding.playlistsList.layoutManager = LinearLayoutManager(context)
        binding.playlistsList.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.playlistsList)
        binding.playlistsSwipe.setOnRefreshListener { presenter.refreshList() }
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
        inflater.inflate(R.menu.playlists_actionbar, menu)
        searchMenuItem.setOnMenuItemClickListener {
            val bottomSheetFragment = SearchBottomSheetFragment()
            bottomSheetFragment.show(childFragmentManager, "bottomSheetFragment.tag")
            true
        }
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }
    // endregion

    // region PlaylistContract.View
    override fun setList(model: PlaylistsContract.Model, animate: Boolean) {
        Glide.with(requireContext())
            .loadFirebaseOrOtherUrl(model.imageUrl, imageProvider)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.playlistsHeaderImage)
        binding.playlistsSwipe.isRefreshing = false
        binding.playlistsItems.text = "${model.items.size}"
        adapter.currentPlaylistId = model.currentPlaylistId
        adapter.setData(model.items, animate)
        binding.playlistsSwipe.setOnRefreshListener { presenter.refreshList() }
    }

    override fun showUndo(msg: String, undo: () -> Unit) {
        snackbar?.dismiss()
        snackbar = snackbarWrapper.make(msg, length = Snackbar.LENGTH_LONG, actionText = "UNDO") {
            undo()
            snackbar?.dismiss()
        }
        snackbar?.show()
    }

    override fun showMessage(msg: String) {
        snackbar?.dismiss()
        snackbar = snackbarWrapper.make(msg, length = Snackbar.LENGTH_LONG)
            .apply { show() }
    }

    override fun gotoPlaylist(id: Long, play: Boolean, source: Source) {
        PlaylistsFragmentDirections.actionGotoPlaylist(id, play, source.toString())
            .apply { findNavController().navigate(this) }
    }

    override fun gotoEdit(id: Long, source: Source) {
        PlaylistsFragmentDirections.actionEditPlaylist(id, source.toString())
            .apply { findNavController().navigate(this) }
    }

    override fun scrollToItem(index: Int) {
        (binding.playlistsList.layoutManager as LinearLayoutManager).run {
            val useIndex = if (index > 0 && index < adapter.data.size) {
                index
            } else 0

            if (index !in this.findFirstCompletelyVisibleItemPosition()..this.findLastCompletelyVisibleItemPosition()) {
                binding.playlistsList.scrollToPosition(useIndex)
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
    override fun onClick(item: ItemContract.Model) {
        presenter.onItemClicked(item)
    }

    override fun onRightSwipe(item: ItemContract.Model) {
        presenter.onItemSwipeRight(item)
    }

    override fun onLeftSwipe(item: ItemContract.Model) {
        val playlistItemModel = item
        adapter.notifyItemRemoved(playlistItemModel.index)
        presenter.onItemSwipeLeft(playlistItemModel) // delays for animation
    }

    override fun onPlay(item: ItemContract.Model, external: Boolean) {
        presenter.onItemPlay(item, external)
    }

    override fun onStar(item: ItemContract.Model) {
        presenter.onItemStar(item)
    }

    override fun onShare(item: ItemContract.Model) {
        presenter.onItemShare(item)
    }
    //endregion

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
