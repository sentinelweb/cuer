package uk.co.sentinelweb.cuer.app.ui.playlist

//import kotlinx.android.synthetic.main.view_playlist_item.view.*
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater.from
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.PlaylistFragmentBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.inteface.EmptyCommitHost
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.DoneNavigation
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.NAV_DONE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.views.HeaderFooterDecoration
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract.CastState.*
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract.ScrollDirection.*
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment.Companion.SEARCH_BOTTOMSHEET_TAG
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.util.cast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.image.ImageProvider
import uk.co.sentinelweb.cuer.app.util.image.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
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
    ItemBaseContract.ItemMoveInteractions,
    ShareContract.Committer,
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    override val external: PlaylistContract.External
        get() = presenter as PlaylistContract.External

    private val presenter: PlaylistContract.Presenter by inject()
    private val adapter: PlaylistAdapter by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val toastWrapper: ToastWrapper by inject()
    private val itemTouchHelper: ItemTouchHelper by inject()
    private val log: LogWrapper by inject()
    private val alertDialogCreator: AlertDialogCreator by inject()
    private val imageProvider: ImageProvider by inject()
    private val castDialogWrapper: CastDialogWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navMapper: NavigationMapper by inject()
    private val navigationProvider: NavigationProvider by inject()
    private val commitHost: CommitHost by inject()
    private val doneNavigation: DoneNavigation by inject()// from activity (see onAttach)

    // todo consider making binding null - getting crashes - or tighten up coroutine scope
    private var _binding: PlaylistFragmentBinding? = null
    private val binding get() = _binding!!

    private val starMenuItem: MenuItem?
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_star)
    private val playMenuItem: MenuItem?
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_play)
    private val editMenuItem: MenuItem?
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_edit)
    private val newMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_new)
    private val filterMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_filter)
    private val searchMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_search)
    private val childrenMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_children)
    private val modeMenuItems: List<MenuItem>
        get() = listOf( // same order as the enum in PlaylistDomain
            binding.playlistToolbar.menu.findItem(R.id.playlist_mode_single),
            binding.playlistToolbar.menu.findItem(R.id.playlist_mode_loop),
            binding.playlistToolbar.menu.findItem(R.id.playlist_mode_shuffle)
        )
    private val isHeadless: Boolean
        get() = HEADLESS.getBoolean(arguments)

    private var snackbar: Snackbar? = null
    private var dialogFragment: DialogFragment? = null

    private data class MenuState constructor(
        var isShow: Boolean = false,
        var isPlayable: Boolean = false,
        var lastPlayModeIndex: Int = 0,
        var reloadHeaderAfterMenuInit: Boolean = false,
    )

    private val menuState = MenuState()

    private val imageUrlArg: String? by lazy {
        IMAGE_URL.getString(arguments)
    }

    private val saveCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            presenter.checkToSave()
        }
    }

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedElementReturnTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        postponeEnterTransition()
        _binding = PlaylistFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveCallback.isEnabled = (commitHost !is EmptyCommitHost)
        binding.playlistList.doOnPreDraw {
            startPostponedEnterTransition()
        }
        binding.playlistToolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
        }
        presenter.initialise()
        binding.playlistList.layoutManager = LinearLayoutManager(context)
        binding.playlistList.adapter = adapter
        binding.playlistList.addItemDecoration(
            HeaderFooterDecoration(0, resources.getDimensionPixelSize(R.dimen.recyclerview_footer))
        )
        itemTouchHelper.attachToRecyclerView(binding.playlistList)
        binding.playlistFabUp.setOnClickListener { presenter.scroll(Up) }
        binding.playlistFabUp.setOnLongClickListener { presenter.scroll(Top);true }
        binding.playlistFabDown.setOnClickListener { presenter.scroll(Down) }
        binding.playlistFabDown.setOnLongClickListener { presenter.scroll(Bottom);true }
        binding.playlistFabRefresh.setOnClickListener { presenter.refreshPlaylist() }
        binding.playlistAppbar.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {

            //var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    menuState.isShow = true
                    // only show the menu items for the non-empty state
                    //modeMenuItems.forEachIndexed { i, item -> item.isVisible = i == menuState.lastPlayModeIndex }
                    updatePlayModeMenuItems()
                    playMenuItem?.isVisible = menuState.isPlayable
                    edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
                } else if (menuState.isShow) {
                    menuState.isShow = false
                    updatePlayModeMenuItems()
                    playMenuItem?.isVisible = false
                    edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
                }
            }
        })
        binding.playlistFabPlaymode.setOnClickListener { presenter.onPlayModeChange() }
        //playlist_fab_shownew.setOnClickListener { presenter.onFilterNewItems() }
        binding.playlistFabPlay.setOnClickListener { presenter.onPlayPlaylist() }
        binding.playlistSwipe.setOnRefreshListener { presenter.refreshPlaylist() }
        if (isHeadless) {
            binding.playlistAppbar.isVisible = false
            binding.playlistFabPlay.isVisible = false
            binding.playlistFabPlaymode.isVisible = false
        }
        imageUrlArg?.also { setImage(it) }
    }

    private fun updatePlayModeMenuItems() {
        val shouldShow = menuState.isShow && menuState.isPlayable
        modeMenuItems.forEachIndexed { i, item ->
            item.isVisible = (shouldShow && i == menuState.lastPlayModeIndex)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.playlist_actionbar, menu)
        modeMenuItems.forEach { it.isVisible = false }
        modeMenuItems.forEach { it.setOnMenuItemClickListener { presenter.onPlayModeChange() } }
        playMenuItem?.isVisible = false
        playMenuItem?.setOnMenuItemClickListener { presenter.onPlayPlaylist() }
        starMenuItem?.setOnMenuItemClickListener { presenter.onStarPlaylist() }
        newMenuItem.setOnMenuItemClickListener { presenter.onFilterNewItems() }
        editMenuItem?.setOnMenuItemClickListener { presenter.onEdit() }
        filterMenuItem.setOnMenuItemClickListener { presenter.onFilterPlaylistItems() }
        childrenMenuItem.setOnMenuItemClickListener { presenter.onShowChildren() }
        if (menuState.reloadHeaderAfterMenuInit) {
            presenter.reloadHeader()
            menuState.reloadHeaderAfterMenuInit = false
        }
        searchMenuItem.setOnMenuItemClickListener {
            val bottomSheetFragment = SearchBottomSheetFragment()
            bottomSheetFragment.show(childFragmentManager, SEARCH_BOTTOMSHEET_TAG)
            true
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, saveCallback)
        linkScopeToActivity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        if (isHeadless) {
            binding.playlistAppbar.isVisible = false
            binding.playlistFabPlay.isVisible = false
            binding.playlistFabPlaymode.isVisible = false
        }
        activity?.apply { makeNavFromArguments()?.setPlaylistData() }
    }

    override fun onDestroyView() {
        presenter.destroy()
        _binding = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
        // todo clean up after im sure it works for all cases
        // see issue as to why this is needed https://github.com/sentinelweb/cuer/issues/105
        (navigationProvider.checkForPendingNavigation(PLAYLIST)
            ?: let { makeNavFromArguments() })
            ?.apply {
                log.d("onResume: apply nav args model = $this")
                setPlaylistData()
                navigationProvider.clearPendingNavigation(PLAYLIST)
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

    override fun onStop() {
        super.onStop()
        dialogFragment?.dismissAllowingStateLoss()
    }

    private fun NavigationModel.setPlaylistData() {
        presenter.setPlaylistData(
            params[PLAYLIST_ID] as Long?,
            params[PLAYLIST_ITEM_ID] as Long?,
            params[PLAY_NOW] as Boolean? ?: false,
            params[SOURCE] as Source
        )
        params[PLAYLIST_PARENT]?.apply { presenter.setAddPlaylistParent(this as Long) }
    }

    private fun makeNavFromArguments(): NavigationModel? {
        val plId = PLAYLIST_ID.getLong(arguments)
        val source: Source? = SOURCE.getEnum<Source>(arguments)
        val plItemId = PLAYLIST_ITEM_ID.getLong(arguments)
        val playNow = PLAY_NOW.getBoolean(arguments)
        val addPlaylistParent =
            PLAYLIST_PARENT.getLong(arguments)
                ?.takeIf { it > 0 }
        arguments?.putBoolean(PLAY_NOW.name, false)
        log.d("onResume: got arguments pl=$plId, item=$plItemId, src=$source, addPlaylistParent=$addPlaylistParent")
        val onResumeGotArguments = plId?.let { it != -1L } ?: false
        return if (onResumeGotArguments) {
            PlaylistContract.makeNav(plId, plItemId, playNow, source, addPlaylistParent)
        } else null
    }
    // endregion

    // region PlaylistContract.View
    override fun setModel(model: PlaylistContract.Model, animate: Boolean) {
        commitHost.isReady(true)
        setHeaderModel(model)
        // update list
        model.items?.apply { setList(this, animate) }
    }

    override fun setList(items: List<ItemContract.Model>, animate: Boolean) {
        binding.playlistSwipe.isRefreshing = false
        adapter.setData(items, animate)
        val isListLarge = items.size > 30
        binding.playlistFabUp.isVisible = isListLarge
        binding.playlistFabDown.isVisible = isListLarge
        binding.playlistFabRefresh.isVisible = isListLarge || items.size == 0
        binding.playlistEmpty.isVisible = items.size == 0
        binding.playlistSwipe.isVisible = items.size > 0
    }

    override fun updateItemModel(model: ItemContract.Model) {
        adapter.updateItemModel(model)
    }

    override fun navigate(nav: NavigationModel) {
        when (nav.target) {
            NAV_DONE -> doneNavigation.navigateDone()
            else -> navMapper.navigate(nav)
        }
    }

    override fun hideRefresh() {
        commitHost.isReady(true)
        binding.playlistSwipe.isRefreshing = false
    }

    override fun showRefresh() {
        commitHost.isReady(false)
        binding.playlistSwipe.isRefreshing = true
    }

    override fun setHeaderModel(model: PlaylistContract.Model) {
        setImage(model.imageUrl)
        binding.playlistCollapsingToolbar.title = model.title
        binding.playlistFabPlay.setImageResource(model.playIcon)
        binding.playlistFabPlay.isVisible = model.canPlay
        binding.playlistFabPlaymode.isVisible = model.canPlay
        playMenuItem?.setIcon(model.playIcon) ?: run { menuState.reloadHeaderAfterMenuInit = false }
        playMenuItem?.setEnabled(model.canPlay)
        starMenuItem?.setIcon(model.starredIcon) ?: run {
            menuState.reloadHeaderAfterMenuInit = false
        }
        editMenuItem?.isVisible = model.canEdit
        starMenuItem?.isVisible = model.canEdit
        childrenMenuItem.isVisible = model.hasChildren > 0
        binding.playlistFlags.isVisible =
            model.isDefault || model.isPlayFromStart || model.isPinned || model.hasChildren > 0
        binding.playlistFlagDefault.isVisible = model.isDefault
        binding.playlistFlagPlayStart.isVisible = model.isPlayFromStart
        binding.playlistFlagPinned.isVisible = model.isPinned
        binding.playlistFabPlaymode.setImageResource(model.loopModeIcon)
        binding.playlistFlagChildren.isVisible = model.hasChildren > 0
        binding.playlistFlagChildren.text = model.hasChildren.toString()
        menuState.lastPlayModeIndex = model.loopModeIndex
        menuState.isPlayable = model.canPlay
        updatePlayModeMenuItems()
    }

    private fun setImage(url: String) {
        Glide.with(requireContext())
            .loadFirebaseOrOtherUrl(url, imageProvider)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.playlistHeaderImage)
    }

    override fun showUndo(msg: String, undoFunction: () -> Unit) {
        snackbar = snackbarWrapper.make(msg, length = Snackbar.LENGTH_LONG, actionText = "UNDO") {
            undoFunction()
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

    override fun highlightPlayingItem(currentItemIndex: Int?) {
        adapter.highlightItem = currentItemIndex
        // todo map it properly
        val text = "${currentItemIndex?.let { it + 1 }} / ${adapter.data.size}"
        _binding?.playlistItems?.setText(text)
    }

    override fun setSubTitle(subtitle: String) {
        // todo make better in ui upgrade
        (activity as AppCompatActivity?)?.supportActionBar?.setTitle(subtitle)
    }

    override fun showPlaylistSelector(model: PlaylistsDialogContract.Config) {
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment =
            PlaylistsDialogFragment.newInstance(model)
        dialogFragment?.show(childFragmentManager, SELECT_PLAYLIST_TAG)
    }

    override fun showPlaylistCreateDialog() {// add playlist
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment = PlaylistEditFragment.newInstance().apply {
            listener = object : PlaylistEditFragment.Listener {
                override fun onPlaylistCommit(domain: PlaylistDomain?) {
                    domain?.apply { presenter.onPlaylistSelected(this, true) }
                    dialogFragment?.dismissAllowingStateLoss()
                }
            }
        }
        dialogFragment?.show(childFragmentManager, CREATE_PLAYLIST_TAG)
    }

    override fun showItemDescription(modelId: Long, item: PlaylistItemDomain, source: Source) {
        adapter.getItemViewForId(modelId)?.let { view ->
            PlaylistFragmentDirections.actionGotoPlaylistItem(item.serialise(), source.toString())
                .apply { findNavController().navigate(this, view.makeTransitionExtras()) }
        }
    }

    override fun gotoEdit(id: Long, source: Source) {
        PlaylistFragmentDirections.actionGotoEditPlaylist(
            id,
            source.toString(),
            null
        )
            .apply { findNavController().navigate(this) }
    }

    override fun showAlertDialog(model: AlertDialogModel) {
        alertDialogCreator.create(model).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun resetItemsState() {
        adapter.notifyDataSetChanged()
    }

    override fun showCastRouteSelectorDialog() {
        castDialogWrapper.showRouteSelector(childFragmentManager)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setCastState(state: PlaylistContract.CastState) {
        when (state) {
            PLAYING -> {
                binding.playlistFabPlay.setImageResource(R.drawable.ic_baseline_playlist_close_24)
                playMenuItem?.setIcon(R.drawable.ic_baseline_playlist_close_24)
                adapter.notifyDataSetChanged()
            }
            NOT_CONNECTED -> {
                binding.playlistFabPlay.setImageResource(R.drawable.ic_baseline_playlist_play_24)
                playMenuItem?.setIcon(R.drawable.ic_baseline_playlist_play_24)
                adapter.notifyDataSetChanged()
            }
            CONNECTING -> {
                playMenuItem?.setIcon(R.drawable.ic_notif_buffer_black)
            }
        }
    }

    override fun showError(message: String) {
        snackbarWrapper.makeError(message).show()
    }

    override fun exit() = TODO()
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
    override fun onItemIconClick(item: ItemContract.Model) {
        presenter.onItemPlayClicked(item)
    }

    override fun onClick(item: ItemContract.Model) {
        presenter.onItemViewClick(item)
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

    override fun onRelated(item: ItemContract.Model) {
        presenter.onItemRelated(item)
    }

    override fun onShare(item: ItemContract.Model) {
        presenter.onItemShare(item)
    }

    override fun onGotoPlaylist(item: ItemContract.Model) {
        presenter.onItemGotoPlaylist(item)
    }
    // endregion

    // region ShareContract.Committer
    override suspend fun commit(onCommit: ShareContract.Committer.OnCommit) {
        presenter.commitPlaylist(onCommit)
    }
    // endregion

    companion object {
        private const val CREATE_PLAYLIST_TAG = "pe_dialog"
        private const val SELECT_PLAYLIST_TAG = "pdf_dialog"
    }

}
