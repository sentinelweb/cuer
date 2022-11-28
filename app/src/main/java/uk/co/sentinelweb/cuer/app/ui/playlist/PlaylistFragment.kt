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
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.FragmentPlaylistBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.inteface.EmptyCommitHost
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.common.ktx.setMenuItemsColor
import uk.co.sentinelweb.cuer.app.ui.common.navigation.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.NAV_DONE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.views.HeaderFooterDecoration
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract.CastState.*
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract.ScrollDirection.*
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment.Companion.SEARCH_BOTTOMSHEET_TAG
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.util.cast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.image.ImageProvider
import uk.co.sentinelweb.cuer.app.util.image.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
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

    override val scope: Scope by fragmentScopeWithSource<PlaylistFragment>()
    override val external: PlaylistContract.External
        get() = presenter as PlaylistContract.External

    private val presenter: PlaylistContract.Presenter by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val toastWrapper: ToastWrapper by inject()
    private val log: LogWrapper by inject()
    private val alertDialogCreator: AlertDialogCreator by inject()
    private val imageProvider: ImageProvider by inject()
    private val castDialogWrapper: CastDialogWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val navigationProvider: NavigationProvider by inject()
    private val doneNavigation: DoneNavigation by inject()// from activity (see onAttach)
    private val commitHost: CommitHost by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val res: ResourceWrapper by inject()
    private val playlistHelpConfig: PlaylistHelpConfig by inject()

    private var _adapter: PlaylistAdapter? = null
    private val adapter: PlaylistAdapter
        get() = _adapter ?: throw IllegalStateException("PlaylistFragment.adapter not bound")

    val linearLayoutManager get() = binding.playlistList.layoutManager as LinearLayoutManager

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding ?: throw Exception("FragmentPlaylistBinding not bound")

    private val searchMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_search)

    private val cardsMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_view_cards)

    private val rowsMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_view_rows)

    private val helpMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_help)

    override val isHeadless: Boolean
        get() = HEADLESS.getBoolean(arguments)

    private var snackbar: Snackbar? = null
    private var dialogFragment: DialogFragment? = null

    private data class MenuState constructor(
        var isCollapsed: Boolean = false,
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

    var appBarOffsetScrollRange = -1
    val appBarOffsetChangedistener = object : AppBarLayout.OnOffsetChangedListener {

        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
            if (appBarOffsetScrollRange == -1) {
                appBarOffsetScrollRange = appBarLayout.getTotalScrollRange()
            }
            if (appBarOffsetScrollRange + verticalOffset == 0) {
                menuState.isCollapsed = true
                binding.playlistToolbar.menu.setMenuItemsColor(R.color.actionbar_icon_collapsed_csl)
                edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
            } else if (menuState.isCollapsed) {
                menuState.isCollapsed = false
                binding.playlistToolbar.menu.setMenuItemsColor(R.color.actionbar_icon_expanded_csl)
                edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
            }
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
        sharedElementEnterTransition =
            from(requireContext()).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        postponeEnterTransition()
        _binding = FragmentPlaylistBinding.inflate(layoutInflater)
        return binding.root
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveCallback.isEnabled = (commitHost !is EmptyCommitHost)
        binding.playlistToolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
        }
        presenter.initialise()
        cardsMenuItem.isVisible = !presenter.isCards
        rowsMenuItem.isVisible = presenter.isCards
        ItemTouchHelper(ItemTouchHelperCallback(this)).apply {
            attachToRecyclerView(binding.playlistList)
        }
        binding.playlistToolbar.title = ""
//        binding.playlistFabUp.setOnClickListener { presenter.scroll(Up) }
//        binding.playlistFabUp.setOnLongClickListener { presenter.scroll(Top);true }
//        binding.playlistFabDown.setOnClickListener { presenter.scroll(Down) }
//        binding.playlistFabDown.setOnLongClickListener { presenter.scroll(Bottom);true }
//        binding.playlistFabRefresh.setOnClickListener { presenter.refreshPlaylist() }
        binding.playlistUpdateButton.setOnClickListener { presenter.updatePlaylist() }

        binding.playlistAppbar.addOnOffsetChangedListener(appBarOffsetChangedistener)
        compactPlayerScroll.addScrollListener(binding.playlistList, this)
        binding.playlistFabPlaymode.setOnClickListener { presenter.onPlayModeChange() }
        binding.playlistFabPlay.setOnClickListener { presenter.onPlayPlaylist() }
        binding.playlistEditButton.setOnClickListener { presenter.onEdit() }
        binding.playlistStarButton.setOnClickListener { presenter.onStarPlaylist() }
        binding.playlistSwipe.setOnRefreshListener { presenter.updatePlaylist() }
        if (isHeadless) {
            binding.playlistAppbar.isVisible = false
            binding.playlistFabPlay.isVisible = false
            binding.playlistFabPlaymode.isVisible = false
        }
        setupRecyclerView()
        imageUrlArg?.also { setImage(it) }
    }

    override fun showHelp() {
        OnboardingFragment.show(requireActivity(), playlistHelpConfig)
    }


    private fun setupRecyclerView() {
        if (binding.playlistList.adapter != null) {
            _adapter = binding.playlistList.adapter as? PlaylistAdapter
        }
        if (_adapter == null) {
            newAdapter()
            binding.playlistList.layoutManager = LinearLayoutManager(context)
        }
        binding.playlistList.doOnPreDraw {
            startPostponedEnterTransition()
        }
        binding.playlistList.addItemDecoration(
            HeaderFooterDecoration(0, resources.getDimensionPixelSize(R.dimen.recyclerview_footer))
        )
    }

    private fun createAdapter() =
        PlaylistAdapter(get(), this, presenter.isCards, get())

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.playlist_actionbar, menu)
//        newMenuItem.setOnMenuItemClickListener { presenter.onFilterNewItems() }
//        editMenuItem.setOnMenuItemClickListener { presenter.onEdit() }
//        filterMenuItem.setOnMenuItemClickListener { presenter.onFilterPlaylistItems() }
        cardsMenuItem.setOnMenuItemClickListener { presenter.onShowCards(true) }
        rowsMenuItem.setOnMenuItemClickListener { presenter.onShowCards(false) }
        if (menuState.reloadHeaderAfterMenuInit) {
            presenter.reloadHeader()
            menuState.reloadHeaderAfterMenuInit = false
        }
        searchMenuItem.setOnMenuItemClickListener {
            val bottomSheetFragment = SearchBottomSheetFragment()
            bottomSheetFragment.show(childFragmentManager, SEARCH_BOTTOMSHEET_TAG)
            true
        }
        helpMenuItem.setOnMenuItemClickListener {
            presenter.onHelp()
            true
        }
        cardsMenuItem.isVisible = !presenter.isCards
        rowsMenuItem.isVisible = presenter.isCards
        binding.playlistToolbar.menu.setMenuItemsColor(R.color.actionbar_icon_expanded_csl)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, saveCallback)
        linkScopeToActivity()
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        if (isHeadless && isAdded) {
            binding.playlistAppbar.isVisible = false
            binding.playlistFabPlay.isVisible = false
            binding.playlistFabPlaymode.isVisible = false
            newAdapter()
        }
        activity?.apply { makeNavFromArguments()?.setPlaylistData() }
    }

    override fun onDestroyView() {
        presenter.destroy()
        _binding = null
        _adapter = null
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        compactPlayerScroll.raisePlayer(this)
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
        // todo review with: https://github.com/sentinelweb/cuer/issues/279
        // see issue as to why this is needed https://github.com/sentinelweb/cuer/issues/105
        (navigationProvider.checkForPendingNavigation(PLAYLIST)
            ?: let { makeNavFromArguments() })
            ?.apply {
                log.d("onResume: apply nav args model = $this")
                setPlaylistData()
                navigationProvider.clearPendingNavigation(PLAYLIST)
            } ?: run {// fixme i don't think we can get here
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

    override fun setList(items: List<PlaylistItemMviContract.Model.Item>, animate: Boolean) {
        binding.playlistSwipe.isRefreshing = false
        adapter.setData(items, animate)
//        val isListLarge = items.size > 30
//        binding.playlistFabUp.isVisible = isListLarge
//        binding.playlistFabDown.isVisible = isListLarge
//        binding.playlistFabRefresh.isVisible = isListLarge || items.size == 0
        binding.playlistEmpty.isVisible = items.size == 0
        binding.playlistSwipe.isVisible = items.size > 0
    }

    override fun updateItemModel(model: PlaylistItemMviContract.Model.Item) {
        adapter.updateItemModel(model)
    }

    override fun navigate(nav: NavigationModel) {
        when (nav.target) {
            NAV_DONE -> doneNavigation.navigateDone()
            else -> navRouter.navigate(nav)
        }
    }

    override fun newAdapter() {
        _adapter = createAdapter()
            .also { binding.playlistList.adapter = it }
    }

    override fun getScrollIndex(): Int {
        return linearLayoutManager.findFirstCompletelyVisibleItemPosition()
    }

    override fun hideRefresh() {
        commitHost.isReady(true)
        _binding?.apply { playlistSwipe.isRefreshing = false }
    }

    override fun showRefresh() {
        commitHost.isReady(false)
        binding.playlistSwipe.isRefreshing = true
    }

    override fun setHeaderModel(model: PlaylistContract.Model) {
        setImage(model.imageUrl)
        binding.playlistCollapsingToolbar.title = model.title
        binding.playlistFabPlay.setIconResource(model.playIcon)
        binding.playlistFabPlay.text = model.playText
        binding.playlistFabPlay.isVisible = model.canPlay
        binding.playlistFabPlaymode.isVisible = model.canPlay
        binding.playlistFabPlaymode.text = model.loopModeText
        binding.playlistStarButton.setIconResource(model.starredIcon)
        binding.playlistStarButton.setText(model.starredText)
        binding.playlistFlagStar.isVisible = model.isStarred
        binding.playlistFlagDefault.isVisible = model.isDefault
        binding.playlistFlagPlayStart.isVisible = model.isPlayFromStart
        binding.playlistFlagPinned.isVisible = model.isPinned
        binding.playlistFabPlaymode.setIconResource(model.loopModeIcon)
        binding.playlistFlagChildren.isVisible = model.hasChildren > 0
        binding.playlistFlagChildren.text = model.hasChildren.toString()
        binding.playlistFlagPlayable.isVisible = model.canPlay
        binding.playlistFlagDeletable.isVisible = model.canDelete
        binding.playlistFlagEditable.isVisible = model.canEdit
        binding.playlistFlagDeletableItems.isVisible = model.canDeleteItems
        binding.playlistFlagEditableItems.isVisible = model.canEditItems
        binding.playlistEditButton.isVisible = model.canEdit
        binding.playlistStarButton.isVisible = model.canEdit
        binding.playlistUpdateButton.isVisible = model.canUpdate
        binding.playlistAppbar.layoutParams.height = res.getDimensionPixelSize(
            if (model.canEdit || model.canPlay) R.dimen.app_bar_header_height_playlist
            else R.dimen.app_bar_header_height_playlist_no_actions
        )
        appBarOffsetScrollRange = -1
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
            snackbar = null
        }
        snackbar?.show()
    }

    override fun scrollTo(direction: PlaylistContract.ScrollDirection) {
        linearLayoutManager.run {
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
        linearLayoutManager.run {
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
        _binding?.playlistItems?.setText(mapPlaylistIndexAndSize(currentItemIndex))
    }

    private fun mapPlaylistIndexAndSize(currentItemIndex: Int?) =
        "${currentItemIndex?.let { it + 1 }} / ${adapter.data.size}"

    override fun setSubTitle(subtitle: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.setTitle(subtitle)
    }

    override fun showPlaylistSelector(model: PlaylistsMviDialogContract.Config) {
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
                    domain
                        ?.apply { presenter.onPlaylistSelected(this, true) }
                    dialogFragment?.dismissAllowingStateLoss()
                }
            }
        }
        dialogFragment?.show(childFragmentManager, CREATE_PLAYLIST_TAG)
    }

    override fun showItemDescription(modelId: Long, item: PlaylistItemDomain, source: Source) {
        adapter
            .getItemViewForId(modelId)
            ?.let { view ->
                PlaylistFragmentDirections.actionGotoPlaylistItem(
                    item.serialise(),
                    source.toString(),
                    -1,
                    (item.playlistId ?: 0) > 0
                ).apply { findNavController().navigate(this, view.makeTransitionExtras()) }
            }
    }

    override fun gotoEdit(id: Long, source: Source) {
        PlaylistFragmentDirections.actionGotoEditPlaylist(
            source.toString(),
            null,
            id
        ).apply { findNavController().navigate(this) }
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
                binding.playlistFabPlay.setIconResource(R.drawable.ic_playlist_close)
                binding.playlistFabPlay.text = getString(R.string.stop)
                adapter.notifyDataSetChanged()
            }

            NOT_CONNECTED -> {
                binding.playlistFabPlay.setIconResource(R.drawable.ic_playlist_play)
                binding.playlistFabPlay.text = getString(R.string.menu_play)
                adapter.notifyDataSetChanged()
            }

            CONNECTING -> {
                binding.playlistFabPlay.setIconResource(R.drawable.ic_notif_buffer_black)
                binding.playlistFabPlay.text = getString(R.string.buffering)
            }
        }
    }

    override fun showError(message: String) {
        if (isAdded) {
            snackbarWrapper.makeError(message).show()
        }
    }

    override fun showMessage(message: String) {
        toastWrapper.show(message)
    }

    override fun exit() {
        findNavController().popBackStack()
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
    override fun onItemIconClick(item: PlaylistItemMviContract.Model.Item) {
        presenter.onItemPlayClicked(item)
    }

    override fun onClick(item: PlaylistItemMviContract.Model.Item) {
        presenter.onItemViewClick(item)
    }

    override fun onPlayStartClick(item: PlaylistItemMviContract.Model.Item) {
        presenter.onPlayStartClick(item)
    }

    override fun onRightSwipe(item: PlaylistItemMviContract.Model.Item) {
        presenter.onItemSwipeRight(item)
    }

    override fun onLeftSwipe(item: PlaylistItemMviContract.Model.Item) {
        val playlistItemModel = item
        adapter.notifyItemRemoved(playlistItemModel.index)
        presenter.onItemSwipeLeft(playlistItemModel) // delays for animation
    }

    override fun onPlay(item: PlaylistItemMviContract.Model.Item, external: Boolean) {
        presenter.onItemPlay(item, external)
    }

    override fun onShowChannel(item: PlaylistItemMviContract.Model.Item) {
        presenter.onItemShowChannel(item)
    }

    override fun onStar(item: PlaylistItemMviContract.Model.Item) {
        presenter.onItemStar(item)
    }

    override fun onRelated(item: PlaylistItemMviContract.Model.Item) {
        presenter.onItemRelated(item)
    }

    override fun onShare(item: PlaylistItemMviContract.Model.Item) {
        presenter.onItemShare(item)
    }

    override fun onGotoPlaylist(item: PlaylistItemMviContract.Model.Item) {
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
