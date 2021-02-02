package uk.co.sentinelweb.cuer.app.ui.playlist

//import kotlinx.android.synthetic.main.view_playlist_item.view.*
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.PlaylistFragmentBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract.PlayState.*
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract.ScrollDirection.*
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.ui.ytplayer.YoutubeActivity
import uk.co.sentinelweb.cuer.app.util.cast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PlaylistFragment :
    Fragment(),
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
    private val castDialogWrapper: CastDialogWrapper by inject()

    // todo consider making binding nulll - getting crashes - or tighten up coroutine scope
    private var _binding: PlaylistFragmentBinding? = null
    private val binding get() = _binding!!

    private val starMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_star)
    private val playMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_play)
    private val editMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_edit)
    private val newMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_new)
    private val filterMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_filter)
    private val modeMenuItems: List<MenuItem>
        get() = listOf( // same order as the enum in PlaylistDomain
            binding.playlistToolbar.menu.findItem(R.id.playlist_mode_single),
            binding.playlistToolbar.menu.findItem(R.id.playlist_mode_loop),
            binding.playlistToolbar.menu.findItem(R.id.playlist_mode_shuffle)
        )

    private var snackbar: Snackbar? = null
    private var createPlaylistDialog: DialogFragment? = null

    private var lastPlayModeIndex = 0

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = PlaylistFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playlistToolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
            //it.setupWithNavController(findNavController(), AppBarConfiguration(TOP_LEVEL_DESTINATIONS))
        }
        presenter.initialise()
        binding.playlistList.layoutManager = LinearLayoutManager(context)
        binding.playlistList.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.playlistList)
        binding.playlistFabUp.setOnClickListener { presenter.scroll(Up) }
        binding.playlistFabUp.setOnLongClickListener { presenter.scroll(Top);true }
        binding.playlistFabDown.setOnClickListener { presenter.scroll(Down) }
        binding.playlistFabDown.setOnLongClickListener { presenter.scroll(Bottom);true }
        binding.playlistAppbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

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
        binding.playlistFabPlaymode.setOnClickListener { presenter.onPlayModeChange() }
        //playlist_fab_shownew.setOnClickListener { presenter.onFilterNewItems() }
        binding.playlistFabPlay.setOnClickListener { presenter.onPlayPlaylist() }
        binding.playlistSwipe.setOnRefreshListener { presenter.refreshList() }
        postponeEnterTransition()
        binding.playlistList.doOnPreDraw {
            startPostponedEnterTransition()
        }
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
        _binding = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        // todo clean up after im sure it works for all cases
        // see issue as to why this is needed https://github.com/sentinelweb/cuer/issues/105
        ((activity as? NavigationProvider)?.checkForPendingNavigation(PLAYLIST_FRAGMENT)?.apply {
            //onResumeGotArguments = true
            log.d("onResume: got nav on callup model = $this")
        } ?: let {
            val plId = PLAYLIST_ID.getLong(arguments)
            val source: Source? = SOURCE.getEnum<Source>(arguments)// todo enforce source?
            val onResumeGotArguments = plId?.let { it != -1L } ?: false
            if (onResumeGotArguments) {
                //log.d("onResume: got nav on args model = plid = $plId plitemId = ${PLAYLIST_ITEM_ID.getLong(arguments)} playNow = ${PLAY_NOW.getBoolean(arguments) }" )
                makeNav(plId, PLAYLIST_ITEM_ID.getLong(arguments), PLAY_NOW.getBoolean(arguments) ?: false, source)
            } else null
        })?.apply {
            log.d("onResume: apply nav args model = $this")
            presenter.setPlaylistData(
                params[PLAYLIST_ID] as Long?,
                params[PLAYLIST_ITEM_ID] as Long?,
                params[PLAY_NOW] as Boolean? ?: false,
                params[SOURCE] as Source
            )
            (activity as? NavigationProvider)?.clearPendingNavigation(PLAYLIST_FRAGMENT)
        } ?: run {
            log.d("onResume: got no nav args")
            presenter.setPlaylistData()
        }
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }
    // endregion

    // region PlaylistContract.View
    override fun setModel(model: PlaylistContract.Model, animate: Boolean) {
        setHeaderModel(model)

        // update list
        model.items?.apply { setList(this, animate) }
    }

    override fun setList(items: List<ItemContract.Model>, animate: Boolean) {
        binding.playlistSwipe.isRefreshing = false
        adapter.setData(items, animate)
        binding.playlistFabUp.isVisible = items.size > 30
        binding.playlistFabDown.isVisible = items.size > 30
    }

    override fun setHeaderModel(model: PlaylistContract.Model) {

        Glide.with(requireContext())
            .run {
                if (model.imageUrl.startsWith("gs://")) load(imageProvider.makeRef(model.imageUrl))
                else load(model.imageUrl)
            }
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.playlistHeaderImage)
        binding.playlistCollapsingToolbar.title = model.title
        binding.playlistFabPlay.setImageResource(model.playIcon)
        playMenuItem.setIcon(model.playIcon)
        starMenuItem.setIcon(model.starredIcon)
        //playlist_items.setText("${model.items.size}")
        binding.playlistFlags.isVisible = model.isDefault
        binding.playlistFabPlaymode.setImageResource(model.loopModeIcon)
        lastPlayModeIndex = model.loopModeIndex
        if (!binding.playlistFabPlaymode.isVisible) {
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
        (binding.playlistList.layoutManager as LinearLayoutManager).run {
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
                binding.playlistList.scrollToPosition(useIndex)
            else {
                binding.playlistList.smoothScrollToPosition(useIndex)
            }
        }
    }

    override fun scrollToItem(index: Int) {
        (binding.playlistList.layoutManager as LinearLayoutManager).run {
            val useIndex = if (index > 0 && index < adapter.data.size) {
                index
            } else 0

            if (index !in this.findFirstCompletelyVisibleItemPosition()..this.findLastCompletelyVisibleItemPosition()) {
                binding.playlistList.scrollToPosition(useIndex)
            }
        }
    }

    override fun playLocal(media: MediaDomain) {
        YoutubeActivity.start(requireContext(), media.platformId)
    }

    override fun highlightPlayingItem(currentItemIndex: Int?) {
        adapter.highlightItem = currentItemIndex
        // todo map it properly
        _binding?.playlistItems?.setText("${currentItemIndex?.let { it + 1 }} / ${adapter.data.size}")
    }

    override fun setSubTitle(subtitle: String) {
        // todo make better in ui upgrade
        (activity as AppCompatActivity?)?.supportActionBar?.setTitle(subtitle)
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

    override fun onView(item: ItemContract.Model) {
        presenter.onItemViewClick(item)
    }

    override fun showItemDescription(itemWitId: PlaylistItemDomain) {
        itemWitId.id?.also { id ->
            adapter.getItemViewForId(id)?.let { view ->
                PlaylistFragmentDirections.actionGotoPlaylistItem(itemWitId.serialise())
                    .apply { findNavController().navigate(this, view.makeTransitionExtras()) }
            }
        }
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

    override fun showCastRouteSelectorDialog() {
        castDialogWrapper.showRouteSelector(childFragmentManager)
    }

    override fun setPlayState(state: PlaylistContract.PlayState) {
        when (state) {
            PLAYING -> {
                binding.playlistFabPlay.setImageResource(R.drawable.ic_baseline_playlist_close_24)
                playMenuItem.setIcon(R.drawable.ic_baseline_playlist_close_24)
                //binding.playlistFabPlay.showProgress(false)
            }
            NOT_CONNECTED -> {
                binding.playlistFabPlay.setImageResource(R.drawable.ic_baseline_playlist_play_24)
                playMenuItem.setIcon(R.drawable.ic_baseline_playlist_play_24)
                //binding.playlistFabPlay.showProgress(false)
            }
            CONNECTING -> {
                //binding.playlistFabPlay.showProgress(true)
                playMenuItem.setIcon(R.drawable.ic_notif_buffer_black)
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

    override fun onPlayStartClick(item: ItemContract.Model) {
        presenter.onPlayStartClick(item)
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

    override fun onShowChannel(item: ItemContract.Model) {
        presenter.onItemShowChannel(item)
    }

    override fun onStar(item: ItemContract.Model) {
        presenter.onItemStar(item)
    }

    override fun onShare(item: ItemContract.Model) {
        presenter.onItemShare(item)
    }

    //endregion

    companion object {
        fun makeNav(item: PlaylistItemDomain, play: Boolean, source: Source?): NavigationModel = NavigationModel(
            PLAYLIST_FRAGMENT, mapOf(
                PLAYLIST_ID to (item.playlistId ?: throw IllegalArgumentException("No Playlist Id")),
                PLAYLIST_ITEM_ID to (item.id ?: throw IllegalArgumentException("No Playlist tem Id")),
                PLAY_NOW to play,
                SOURCE to (source ?: Source.LOCAL)// todo enforce source
            )
        )

        fun makeNav(plId: Long?, plItemId: Long?, play: Boolean, source: Source?): NavigationModel = NavigationModel(
            PLAYLIST_FRAGMENT, mapOf(
                PLAYLIST_ID to (plId ?: throw IllegalArgumentException("No Playlist Id")),
                PLAYLIST_ITEM_ID to (plItemId ?: throw IllegalArgumentException("No Playlist tem Id")),
                PLAY_NOW to play,
                SOURCE to (source ?: Source.LOCAL)// todo enforce source
            )
        )

        private val CREATE_PLAYLIST_TAG = "pe_dialog"

    }

    // old code
//    private var onResumeGotArguments = false


//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        // crappy hack - sometime onResume is called before activity onStart so the fragment doesn't get the arguments
//        val plId = PLAYLIST_ID.getLong(arguments)
//        val hasArguments = plId?.let{ it>0L } ?:false
//        log.d("onAttach")
//        if (isResumed && hasArguments && !onResumeGotArguments) {
//            log.d("onAttach: got nav on args model = plid = $plId plitemId = ${PLAYLIST_ITEM_ID.getLong(arguments)} playNow = ${PLAY_NOW.getBoolean(arguments)}")
//            presenter.setPlaylistData(
//                plId,
//                PLAYLIST_ITEM_ID.getLong(arguments),
//                PLAY_NOW.getBoolean(arguments) ?: false
//            )
//        }
//    }

}
