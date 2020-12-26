package uk.co.sentinelweb.cuer.app.ui.playlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.playlist_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity.Companion.TOP_LEVEL_DESTINATIONS
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract.ScrollDirection.*
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.ui.ytplayer.YoutubeActivity
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PlaylistFragment :
    Fragment(R.layout.playlist_fragment),
    PlaylistContract.View,
    ItemContract.Interactions,
    ItemBaseContract.ItemMoveInteractions {

    private val presenter: PlaylistContract.Presenter by currentScope.inject()
    private val adapter: PlaylistAdapter by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()
    private val toastWrapper: ToastWrapper by inject()
    private val itemTouchHelper: ItemTouchHelper by currentScope.inject()
    private val log: LogWrapper by inject()
    private val selectDialogCreator: SelectDialogCreator by currentScope.inject()
    private val alertDialogCreator: AlertDialogCreator by currentScope.inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()

    private val starMenuItem: MenuItem
        get() = playlist_toolbar.menu.findItem(R.id.playlist_star)
    private val playMenuItem: MenuItem
        get() = playlist_toolbar.menu.findItem(R.id.playlist_play)
    private val editMenuItem: MenuItem
        get() = playlist_toolbar.menu.findItem(R.id.playlist_edit)
    private val newMenuItem: MenuItem
        get() = playlist_toolbar.menu.findItem(R.id.playlist_new)
    private val filterMenuItem: MenuItem
        get() = playlist_toolbar.menu.findItem(R.id.playlist_filter)
    private val modeMenuItems: List<MenuItem>
        get() = listOf( // same order as the enum in PlaylistDomain
            playlist_toolbar.menu.findItem(R.id.playlist_mode_single),
            playlist_toolbar.menu.findItem(R.id.playlist_mode_loop),
            playlist_toolbar.menu.findItem(R.id.playlist_mode_shuffle)
        )

    private var snackbar: Snackbar? = null
    private var createPlaylistDialog: DialogFragment? = null

    private var lastPlayModeIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlist_toolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
            it.setupWithNavController(findNavController(), AppBarConfiguration(TOP_LEVEL_DESTINATIONS))
        }
        presenter.initialise()
        playlist_list.layoutManager = LinearLayoutManager(context)
        playlist_list.adapter = adapter
        itemTouchHelper.attachToRecyclerView(playlist_list)
        playlist_fab_up.setOnClickListener { presenter.scroll(Up) }
        playlist_fab_up.setOnLongClickListener { presenter.scroll(Top);true }
        playlist_fab_down.setOnClickListener { presenter.scroll(Down) }
        playlist_fab_down.setOnLongClickListener { presenter.scroll(Bottom);true }
        playlist_appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    // only show the menu items for the non-empty state
                    modeMenuItems.forEachIndexed { i, item -> item.isVisible = i == lastPlayModeIndex }
                    playMenuItem.isVisible = true
                } else if (isShow) {
                    isShow = false
                    modeMenuItems.forEach { it.isVisible = false }
                    playMenuItem.isVisible = false
                }
            }
        })
        playlist_fab_playmode.setOnClickListener { presenter.onPlayModeChange() }
        playlist_fab_shownew.setOnClickListener { presenter.onFilterNewItems() }
        playlist_fab_play.setOnClickListener { presenter.onPlayPlaylist() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.playlist_actionbar, menu)
        modeMenuItems.forEach { it.isVisible = false }
        modeMenuItems.forEach { it.setOnMenuItemClickListener { presenter.onPlayModeChange() } }
        playMenuItem.isVisible = false
        playMenuItem.setOnMenuItemClickListener { presenter.onPlayPlaylist() }
        starMenuItem.setOnMenuItemClickListener { presenter.onStarPlaylist() }
        newMenuItem.setOnMenuItemClickListener { presenter.onFilterNewItems() }
        editMenuItem.setOnMenuItemClickListener { presenter.onEdit() }
        filterMenuItem.setOnMenuItemClickListener { presenter.onFilterPlaylistItems() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        presenter.destroy()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        log.d("onResume arg.plid=${PLAYLIST_ID.getLong(arguments)} arg.pl_item_id=${PLAYLIST_ITEM_ID.getLong(arguments)}")
        presenter.setPlaylistData(
            PLAYLIST_ID.getLong(arguments),
            PLAYLIST_ITEM_ID.getLong(arguments),
            PLAY_NOW.getBoolean(arguments) ?: false
        )
    }
    // endregion

    // region PlaylistContract.View
    override fun setModel(model: PlaylistModel, animate: Boolean) {
        setHeaderModel(model)

        // update list
        setList(model.items, animate)
    }

    override fun setList(items: List<ItemContract.PlaylistItemModel>, animate: Boolean) {
        playlist_swipe.isRefreshing = false
        adapter.setData(items, animate)
        playlist_swipe.setOnRefreshListener { presenter.refreshList() }
    }

    override fun setHeaderModel(model: PlaylistModel) {
        Glide.with(playlist_header_image).load(imageProvider.makeRef(model.imageUrl)).into(playlist_header_image)
        playlist_collapsing_toolbar.title = model.title
        playlist_fab_play.setImageResource(model.playIcon)
        playMenuItem.setIcon(model.playIcon)
        starMenuItem.setIcon(model.starredIcon)
        playlist_items.setText("${model.items.size}")
        playlist_flags.isVisible = model.isDefault
        playlist_fab_playmode.setImageResource(model.loopModeIcon)
        lastPlayModeIndex = model.loopModeIndex
        if (!playlist_fab_playmode.isVisible) {
            modeMenuItems.forEachIndexed { i, item -> item.isVisible = i == lastPlayModeIndex }
        }
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
        YoutubeActivity.start(requireContext(), media.platformId)
    }

    override fun highlightPlayingItem(currentItemIndex: Int?) {
        adapter.highlightItem = currentItemIndex
    }

    override fun setSubTitle(subtitle: String) {
        // todo make better in ui upgrade
        (activity as AppCompatActivity).supportActionBar?.setTitle(subtitle)
    }

    override fun showPlaylistSelector(model: SelectDialogModel) {
        selectDialogCreator.createSingle(model).apply { show() }
    }

    override fun showPlaylistCreateDialog() {
        createPlaylistDialog?.dismissAllowingStateLoss()
        createPlaylistDialog = PlaylistEditFragment.newInstance(null).apply {
            listener = object : PlaylistEditFragment.Listener {
                override fun onPlaylistCommit(domain: PlaylistDomain?) {
                    domain?.apply { presenter.onPlaylistSelected(this) }
                    createPlaylistDialog?.dismissAllowingStateLoss()
                }
            }
        }
        createPlaylistDialog?.show(childFragmentManager, CREATE_PLAYLIST_TAG)
    }

    override fun onView(item: ItemContract.PlaylistItemModel) {
        presenter.onItemViewClick(item)
    }

    override fun showItemDescription(itemWitId: PlaylistItemDomain) {
        PlaylistFragmentDirections.actionGotoPlaylistItem(itemWitId.serialise())
            .apply { findNavController().navigate(this) }
    }

    override fun gotoEdit(id: Long) {
        PlaylistFragmentDirections.actionGotoEditPlaylist(id)
            .apply { findNavController().navigate(this) }
    }

    override fun showAlertDialog(model: AlertDialogModel) {
        alertDialogCreator.create(model).show()
    }

    override fun resetItemsState() {
        adapter.notifyDataSetChanged()
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
    override fun onClick(item: ItemContract.PlaylistItemModel) {
        presenter.onItemClicked(item)
    }

    override fun onPlayStartClick(item: ItemContract.PlaylistItemModel) {
        presenter.onPlayStartClick(item)
    }

    override fun onRightSwipe(item: ItemContract.PlaylistItemModel) {
        presenter.onItemSwipeRight(item)
    }

    override fun onLeftSwipe(item: ItemContract.PlaylistItemModel) {
        val playlistItemModel = item
        adapter.notifyItemRemoved(playlistItemModel.index)
        presenter.onItemSwipeLeft(playlistItemModel) // delays for animation
    }

    override fun onPlay(item: ItemContract.PlaylistItemModel, external: Boolean) {
        presenter.onItemPlay(item, external)
    }

    override fun onShowChannel(item: ItemContract.PlaylistItemModel) {
        presenter.onItemShowChannel(item)
    }

    override fun onStar(item: ItemContract.PlaylistItemModel) {
        presenter.onItemStar(item)
    }

    override fun onShare(item: ItemContract.PlaylistItemModel) {
        presenter.onItemShare(item)
    }

    //endregion

    companion object {
        private val CREATE_PLAYLIST_TAG = "pe_dialog"

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistFragment>()) {
                scoped<PlaylistContract.View> { getSource() }
                scoped<PlaylistContract.Presenter> {
                    PlaylistPresenter(
                        view = get(),
                        state = get(),
                        repository = get(),
                        playlistRepository = get(),
                        modelMapper = get(),
                        contextProvider = get(),
                        queue = get(),
                        toastWrapper = get(),
                        ytContextHolder = get(),
                        ytJavaApi = get(),
                        shareWrapper = get(),
                        prefsWrapper = get(named<GeneralPreferences>()),
                        playlistMutator = get(),
                        log = get(),
                        playlistDialogModelCreator = get(),
                        timeProvider = get()
                    )
                }
                scoped { PlaylistModelMapper(res = get()) }
                scoped { PlaylistAdapter(get(), getSource()) }
                scoped { ItemTouchHelperCallback(getSource()) }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped { SnackbarWrapper((getSource() as Fragment).requireActivity()) }
                scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ShareWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ItemFactory() }
                scoped { SelectDialogCreator((getSource() as Fragment).requireActivity()) }
                scoped { AlertDialogCreator((getSource() as Fragment).requireActivity()) }
                viewModel { PlaylistState() }
            }
        }

    }
}
