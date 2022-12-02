package uk.co.sentinelweb.cuer.app.ui.playlist

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
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.FragmentPlaylistBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.play.PlayDialog
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.inteface.EmptyCommitHost
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.common.ktx.setMenuItemsColor
import uk.co.sentinelweb.cuer.app.ui.common.navigation.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.common.views.HeaderFooterDecoration
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract.Model.Item
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.cast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.image.ImageProvider
import uk.co.sentinelweb.cuer.app.util.image.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.SHOW_VIDEO_CARDS
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PlaylistMviFragment : Fragment(),
    ItemContract.Interactions,
    ItemBaseContract.ItemMoveInteractions,
    ShareContract.Committer,
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PlaylistMviFragment>()

    //    override val external: PlaylistContract.External
//        get() = presenter as PlaylistContract.External
    //private val presenter: PlaylistContract.Presenter by inject()
    private val controller: PlaylistMviController by inject()

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
    private val prefsWrapper: MultiPlatformPreferencesWrapper by inject()

    private var _adapter: PlaylistAdapter? = null
    private val adapter: PlaylistAdapter
        get() = _adapter ?: throw IllegalStateException("PlaylistFragment.adapter not bound")

    val linearLayoutManager get() = binding.playlistList.layoutManager as LinearLayoutManager

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding ?: throw Exception("FragmentPlaylistBinding not bound")

    private lateinit var viewProxy: PlaylistMviFragment.ViewProxy

    private val searchMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_search)

    private val cardsMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_view_cards)

    private val rowsMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_view_rows)

    private val helpMenuItem: MenuItem
        get() = binding.playlistToolbar.menu.findItem(R.id.playlist_help)

    private val isHeadless: Boolean
        get() = NavigationModel.Param.HEADLESS.getBoolean(arguments)

    private val isCards
        get() = prefsWrapper.getBoolean(SHOW_VIDEO_CARDS, true) && !isHeadless

    private var snackbar: Snackbar? = null
    private var dialogFragment: DialogFragment? = null

    private data class MenuState constructor(
        var isCollapsed: Boolean = false,
        var reloadHeaderAfterMenuInit: Boolean = false,
    )

    private val menuState = MenuState()

    private val imageUrlArg: String? by lazy {
        NavigationModel.Param.IMAGE_URL.getString(arguments)
    }

    private val saveCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
//            presenter.checkToSave()
            viewProxy.dispatch(OnCheckToSave)
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
            androidx.transition.TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
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
        //presenter.initialise()
        cardsMenuItem.isVisible = !isCards
        rowsMenuItem.isVisible = isCards
        ItemTouchHelper(ItemTouchHelperCallback(this)).apply {
            attachToRecyclerView(binding.playlistList)
        }
        binding.playlistToolbar.title = ""
//        binding.playlistFabUp.setOnClickListener { presenter.scroll(Up) }
//        binding.playlistFabUp.setOnLongClickListener { presenter.scroll(Top);true }
//        binding.playlistFabDown.setOnClickListener { presenter.scroll(Down) }
//        binding.playlistFabDown.setOnLongClickListener { presenter.scroll(Bottom);true }
//        binding.playlistFabRefresh.setOnClickListener { presenter.refreshPlaylist() }
        binding.playlistUpdateButton.setOnClickListener {
            //presenter.updatePlaylist()
            viewProxy.dispatch(OnUpdate)
        }

        binding.playlistAppbar.addOnOffsetChangedListener(appBarOffsetChangedistener)
        compactPlayerScroll.addScrollListener(binding.playlistList, this)
        binding.playlistFabPlaymode.setOnClickListener {
            //presenter.onPlayModeChange()
            viewProxy.dispatch(OnPlayModeChange)
        }
        binding.playlistFabPlay.setOnClickListener {
//            presenter.onPlayPlaylist()
            viewProxy.dispatch(OnPlay)
        }
        binding.playlistEditButton.setOnClickListener {
            //presenter.onEdit()
            viewProxy.dispatch(OnEdit)
        }
        binding.playlistStarButton.setOnClickListener {
            //presenter.onStarPlaylist()
            viewProxy.dispatch(OnStar)

        }
        binding.playlistSwipe.setOnRefreshListener {
            //presenter.updatePlaylist()
            viewProxy.dispatch(OnUpdate)
        }
        if (isHeadless) {
            binding.playlistAppbar.isVisible = false
            binding.playlistFabPlay.isVisible = false
            binding.playlistFabPlaymode.isVisible = false
        }
        setupRecyclerView()
        imageUrlArg?.also { setImage(it) }

        viewProxy = ViewProxy()
        controller.onViewCreated(listOf(viewProxy), viewLifecycleOwner.essentyLifecycle())
    }

    fun showHelp() {
        OnboardingFragment.show(requireActivity(), playlistHelpConfig)
    }

    inner class ViewProxy : BaseMviView<PlaylistMviContract.View.Model, PlaylistMviContract.View.Event>(),
        PlaylistMviContract.View {

        override fun processLabel(label: Label) = when (label) {
            is Label.Error -> showError(label.message)
            is Label.Message -> showMessage(label.message)
            Label.Loading -> showRefresh()
            Label.Loaded -> hideRefresh()
            is Label.ItemRemoved -> Unit
            is Label.Navigate -> navigate(label.model)
            is Label.ShowPlaylistsSelector -> showPlaylistSelector(label.config)
            is Label.ShowUndo -> showUndo(label.message) {
                viewProxy.dispatch(OnUndo(label.undoType))
            }

            is Label.HighlightPlayingItem -> highlightPlayingItem(label.pos)
            is Label.ScrollToItem -> scrollToItem(label.pos)
            is Label.UpdateModelItem -> updateItemModel(label.model) // todo do i need this?
            is Label.Help -> showHelp()
            is Label.ResetItemState -> resetItemsState()
            is Label.ShowPlaylistsCreator -> showPlaylistCreateDialog()
        }.also { log.d(label.toString()) }

        //        override fun render(model: PlaylistMviContract.View.Model) {// todo diff, update item
//            setModel(model, animate = false)
//        }
        override val renderer: ViewRenderer<PlaylistMviContract.View.Model> =
            diff {
                var previousItems: List<Item> = listOf()
                diff(get = PlaylistMviContract.View.Model::isCards, set = {
                    val currentScrollIndex = getScrollIndex()
                    newAdapter()
                    setList(previousItems, false)
                    scrollToItem(currentScrollIndex)
                    //commitHost.isReady(true)
                    cardsMenuItem.isVisible = !it
                    rowsMenuItem.isVisible = it
                    binding.playlistToolbar.menu.setMenuItemsColor(
                        if (menuState.isCollapsed) R.color.actionbar_icon_collapsed_csl
                        else R.color.actionbar_icon_expanded_csl
                    )
                    log.d("is cards: $it")
                })
                diff(get = PlaylistMviContract.View.Model::items, set = {
                    val items = it ?: listOf()
                    setList(items, animate = false)
                    previousItems = items

                    log.d("items: ${it?.size}")
                })
                diff(get = PlaylistMviContract.View.Model::header, set = {
                    setHeaderModel(it)
                    commitHost.isReady(true)
                    log.d("header: ${it}")
                })
            }
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
        PlaylistAdapter(get(), this, isCards, get())

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.playlist_actionbar, menu)
//        newMenuItem.setOnMenuItemClickListener { presenter.onFilterNewItems() }
//        editMenuItem.setOnMenuItemClickListener { presenter.onEdit() }
//        filterMenuItem.setOnMenuItemClickListener { presenter.onFilterPlaylistItems() }
        cardsMenuItem.setOnMenuItemClickListener { viewProxy.dispatch(OnShowCards(true)); true }
        rowsMenuItem.setOnMenuItemClickListener { viewProxy.dispatch(OnShowCards(false)); true }
        // fixme check still needed
//        if (menuState.reloadHeaderAfterMenuInit) {
//            presenter.reloadHeader()
//            menuState.reloadHeaderAfterMenuInit = false
//        }
        searchMenuItem.setOnMenuItemClickListener {
            val bottomSheetFragment = SearchBottomSheetFragment()
            bottomSheetFragment.show(childFragmentManager, SearchBottomSheetFragment.SEARCH_BOTTOMSHEET_TAG)
            true
        }
        helpMenuItem.setOnMenuItemClickListener {
            //presenter.onHelp()
            viewProxy.dispatch(OnHelp)
            true
        }
        cardsMenuItem.isVisible = !isCards
        rowsMenuItem.isVisible = isCards
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
        //presenter.destroy()
        controller.onViewDestroyed()
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
        (navigationProvider.checkForPendingNavigation(Target.PLAYLIST) ?: makeNavFromArguments())
            ?.apply {
                log.d("onResume: apply nav args model = $this")
                setPlaylistData()
                navigationProvider.clearPendingNavigation(Target.PLAYLIST)
            } ?: run {// fixme i don't think we can get here
            log.d("onResume: got no nav args")
            //presenter.setPlaylistData()
            viewProxy.dispatch(OnSetPlaylistData())
        }
        //presenter.onResume()
        //viewProxy.dispatch(OnResume)
        controller.onRefresh()
    }

    override fun onPause() {
        super.onPause()
        //presenter.onPause()
        viewProxy.dispatch(OnPause)
    }

    override fun onStop() {
        super.onStop()
        dialogFragment?.dismissAllowingStateLoss()
    }

    private fun NavigationModel.setPlaylistData() {
//        presenter.setPlaylistData(
//            params[NavigationModel.Param.PLAYLIST_ID] as Long?,
//            params[NavigationModel.Param.PLAYLIST_ITEM_ID] as Long?,
//            params[NavigationModel.Param.PLAY_NOW] as Boolean? ?: false,
//            params[NavigationModel.Param.SOURCE] as OrchestratorContract.Source
//        )
        viewProxy.dispatch(
            OnSetPlaylistData(
                params[NavigationModel.Param.PLAYLIST_ID] as Long?,
                params[NavigationModel.Param.PLAYLIST_ITEM_ID] as Long?,
                params[NavigationModel.Param.PLAY_NOW] as Boolean? ?: false,
                params[NavigationModel.Param.SOURCE] as OrchestratorContract.Source,
                params[NavigationModel.Param.PLAYLIST_PARENT] as Long?
            )
        )
//        params[NavigationModel.Param.PLAYLIST_PARENT]?.apply {
//            presenter.setAddPlaylistParent(this as Long)
//        }
    }

    private fun makeNavFromArguments(): NavigationModel? {
        val plId = NavigationModel.Param.PLAYLIST_ID.getLong(arguments)
        val source: OrchestratorContract.Source? =
            NavigationModel.Param.SOURCE.getEnum<OrchestratorContract.Source>(arguments)
        val plItemId = NavigationModel.Param.PLAYLIST_ITEM_ID.getLong(arguments)
        val playNow = NavigationModel.Param.PLAY_NOW.getBoolean(arguments)
        val addPlaylistParent =
            NavigationModel.Param.PLAYLIST_PARENT.getLong(arguments)
                ?.takeIf { it > 0 }
        arguments?.putBoolean(NavigationModel.Param.PLAY_NOW.name, false)
        log.d("onResume: got arguments pl=$plId, item=$plItemId, src=$source, addPlaylistParent=$addPlaylistParent")
        val onResumeGotArguments = plId?.let { it != -1L } ?: false
        return if (onResumeGotArguments) {
            PlaylistContract.makeNav(plId, plItemId, playNow, source, addPlaylistParent)
        } else null
    }
    // endregion

    // region PlaylistContract.View
//    private fun setModel(model: PlaylistMviContract.View.Model, animate: Boolean) {
//        commitHost.isReady(true)
//        setHeaderModel(model.header)
//        // update list
//        model.items?.apply { setList(this, animate) }
//    }

    private fun setList(items: List<Item>, animate: Boolean) {
        binding.playlistSwipe.isRefreshing = false
        adapter.setData(items, animate)
//        val isListLarge = items.size > 30
//        binding.playlistFabUp.isVisible = isListLarge
//        binding.playlistFabDown.isVisible = isListLarge
//        binding.playlistFabRefresh.isVisible = isListLarge || items.size == 0
        binding.playlistEmpty.isVisible = items.size == 0
        binding.playlistSwipe.isVisible = items.size > 0
    }

    private fun updateItemModel(model: Item) {
        adapter.updateItemModel(model)
    }

    private fun navigate(nav: NavigationModel) {
        when (nav.target) {
            Target.PLAYLIST_EDIT -> PlaylistFragmentDirections.actionGotoEditPlaylist(
                nav.params[NavigationModel.Param.SOURCE].toString(),
                null,
                nav.params[NavigationModel.Param.PLAYLIST_ID] as Long
            ).apply { findNavController().navigate(this) }

            Target.NAV_DONE -> doneNavigation.navigateDone()
            else -> navRouter.navigate(nav)
        }
    }

    private fun newAdapter() {
        _adapter = createAdapter()
            .also { binding.playlistList.adapter = it }
    }

    private fun getScrollIndex(): Int {
        return linearLayoutManager.findFirstCompletelyVisibleItemPosition()
    }

    private fun hideRefresh() {
        commitHost.isReady(true)
        _binding?.apply { playlistSwipe.isRefreshing = false }
    }

    private fun showRefresh() {
        commitHost.isReady(false)
        binding.playlistSwipe.isRefreshing = true
    }

    private fun setHeaderModel(model: PlaylistMviContract.View.Header) {
        setImage(model.imageUrl)
        binding.playlistCollapsingToolbar.title = model.title
        binding.playlistFabPlay.setIconResource(res.getDrawableResourceId(model.playIcon))
        binding.playlistFabPlay.text = model.playText
        binding.playlistFabPlay.isVisible = model.canPlay
        binding.playlistFabPlaymode.isVisible = model.canPlay
        binding.playlistFabPlaymode.text = model.loopModeText
        binding.playlistStarButton.setIconResource(res.getDrawableResourceId(model.starredIcon))
        binding.playlistStarButton.setText(model.starredText)
        binding.playlistFlagStar.isVisible = model.isStarred
        binding.playlistFlagDefault.isVisible = model.isDefault
        binding.playlistFlagPlayStart.isVisible = model.isPlayFromStart
        binding.playlistFlagPinned.isVisible = model.isPinned
        binding.playlistFabPlaymode.setIconResource(res.getDrawableResourceId(model.loopModeIcon))
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

    private fun showUndo(msg: String, undoFunction: () -> Unit) {
        snackbar = snackbarWrapper.make(msg, length = Snackbar.LENGTH_LONG, actionText = "UNDO") {
            undoFunction()
            snackbar?.dismiss()
            snackbar = null
        }
        snackbar?.show()
    }

    private fun scrollTo(direction: PlaylistContract.ScrollDirection) {
        linearLayoutManager.run {
            val itemsOnScreen =
                this.findLastCompletelyVisibleItemPosition() - this.findFirstCompletelyVisibleItemPosition()

            val useIndex = when (direction) {
                PlaylistContract.ScrollDirection.Up -> max(
                    this.findFirstCompletelyVisibleItemPosition() - itemsOnScreen,
                    0
                )

                PlaylistContract.ScrollDirection.Down ->
                    min(
                        this.findLastCompletelyVisibleItemPosition() + itemsOnScreen,
                        adapter.data.size - 1
                    )

                PlaylistContract.ScrollDirection.Top -> 0
                PlaylistContract.ScrollDirection.Bottom -> adapter.data.size - 1
            }
            if (abs(this.findLastCompletelyVisibleItemPosition() - useIndex) > 20)
                binding.playlistList.scrollToPosition(useIndex)
            else {
                binding.playlistList.smoothScrollToPosition(useIndex)
            }
        }
    }

    private fun scrollToItem(index: Int) {
        linearLayoutManager.run {
            val useIndex = if (index > 0 && index < adapter.data.size) {
                index
            } else 0

            if (index !in this.findFirstCompletelyVisibleItemPosition()..this.findLastCompletelyVisibleItemPosition()) {
                binding.playlistList.scrollToPosition(useIndex)
            }
        }
    }

    private fun highlightPlayingItem(currentItemIndex: Int?) {
        adapter.highlightItem = currentItemIndex
        _binding?.playlistItems?.setText(mapPlaylistIndexAndSize(currentItemIndex))
    }

    private fun mapPlaylistIndexAndSize(currentItemIndex: Int?) =
        "${currentItemIndex?.let { it + 1 }} / ${adapter.data.size}"

    private fun setSubTitle(subtitle: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.setTitle(subtitle)
    }

    private fun showPlaylistSelector(model: PlaylistsMviDialogContract.Config) {
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment =
            PlaylistsDialogFragment.newInstance(model)
        dialogFragment?.show(childFragmentManager, SELECT_PLAYLIST_TAG)
    }

    private fun showPlaylistCreateDialog() {// add playlist
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment = PlaylistEditFragment.newInstance().apply {
            listener = object : PlaylistEditFragment.Listener {
                override fun onPlaylistCommit(domain: PlaylistDomain?) {
                    domain
                        ?.apply {
                            //presenter.onPlaylistSelected(this, true)
                            viewProxy.dispatch(OnPlaylistSelected(this))
                        }
                    dialogFragment?.dismissAllowingStateLoss()
                }
            }
        }
        dialogFragment?.show(childFragmentManager, CREATE_PLAYLIST_TAG)
    }

    private fun showItemDescription(modelId: Long, item: PlaylistItemDomain, source: OrchestratorContract.Source) {
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

//    private fun gotoEdit(id: Long, source: OrchestratorContract.Source) {
//        PlaylistFragmentDirections.actionGotoEditPlaylist(
//            source.toString(),
//            null,
//            id
//        ).apply { findNavController().navigate(this) }
//    }

    private fun showAlertDialog(model: AlertDialogModel) {
        alertDialogCreator.create(model).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun resetItemsState() {
        adapter.notifyDataSetChanged()
    }

    private fun showCastRouteSelectorDialog() {
        castDialogWrapper.showRouteSelector(childFragmentManager)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setCastState(state: PlaylistContract.CastState) {
        when (state) {
            PlaylistContract.CastState.PLAYING -> {
                binding.playlistFabPlay.setIconResource(R.drawable.ic_playlist_close)
                binding.playlistFabPlay.text = getString(R.string.stop)
                adapter.notifyDataSetChanged()
            }

            PlaylistContract.CastState.NOT_CONNECTED -> {
                binding.playlistFabPlay.setIconResource(R.drawable.ic_playlist_play)
                binding.playlistFabPlay.text = getString(R.string.menu_play)
                adapter.notifyDataSetChanged()
            }

            PlaylistContract.CastState.CONNECTING -> {
                binding.playlistFabPlay.setIconResource(R.drawable.ic_notif_buffer_black)
                binding.playlistFabPlay.text = getString(R.string.buffering)
            }
        }
    }

    private fun showError(message: String) {
        if (isAdded) {
            snackbarWrapper.makeError(message).show()
        }
    }

    private fun showMessage(message: String) {
        toastWrapper.show(message)
    }

    private fun exit() {
        findNavController().popBackStack()
    }
    //endregion

    // region ItemContract.ItemMoveInteractions
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
//        presenter.moveItem(fromPosition, toPosition)
        viewProxy.dispatch(OnMove(fromPosition, toPosition))
        // shows the move while dragging
        adapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemClear() {
        viewProxy.dispatch(OnClearMove)
//        presenter.commitMove()
    }
    //endregion

    // region ItemContract.Interactions
    override fun onItemIconClick(item: Item) {
//        presenter.onItemPlayClicked(item)
        viewProxy.dispatch(OnPlayItem(item))
    }

    override fun onClick(item: Item) {
        //presenter.onItemViewClick(item)
        viewProxy.dispatch(OnShowItem(item))
    }

    override fun onPlayStartClick(item: Item) {
        //presenter.onPlayStartClick(item)
        viewProxy.dispatch(OnPlayItem(item, start = true))
    }

    override fun onRightSwipe(item: Item) {
//        presenter.onItemSwipeRight(item)
        viewProxy.dispatch(OnMoveSwipe(item))
    }

    override fun onLeftSwipe(item: Item) {
        val playlistItemModel = item
        adapter.notifyItemRemoved(playlistItemModel.index)
        //presenter.onItemSwipeLeft(playlistItemModel) // delays for animation
        log.d("onLeftSwipe")
        viewProxy.dispatch(OnDeleteItem(item))
    }

    override fun onPlay(item: Item, external: Boolean) {
        //presenter.onItemPlay(item, external)
        viewProxy.dispatch(OnPlayItem(item, external = true))
    }

    override fun onShowChannel(item: Item) {
//        presenter.onItemShowChannel(item)
        viewProxy.dispatch(OnShowChannel)
    }

    override fun onStar(item: Item) {
        //presenter.onItemStar(item)
        viewProxy.dispatch(OnStarItem(item))
    }

    override fun onRelated(item: Item) {
        //presenter.onItemRelated(item)
        viewProxy.dispatch(OnRelatedItem(item))
    }

    override fun onShare(item: Item) {
        //presenter.onItemShare(item)
        viewProxy.dispatch(OnShareItem(item))
    }

    override fun onGotoPlaylist(item: Item) {
        //presenter.onItemGotoPlaylist(item)
        viewProxy.dispatch(OnGotoPlaylist(item))
    }
    // endregion

    // region ShareContract.Committer
    override suspend fun commit(onCommit: ShareContract.Committer.OnCommit) {
        //presenter.commitPlaylist(onCommit)
        viewProxy.dispatch(OnCommit)// tod fire call back somehow
    }
    // endregion

    companion object {
        private const val CREATE_PLAYLIST_TAG = "pe_dialog"
        private const val SELECT_PLAYLIST_TAG = "pdf_dialog"

        val fragmentModule = module {
            scope(named<PlaylistMviFragment>()) {
                scoped {
                    PlaylistMviController(
                        store = get(),
                        modelMapper = get(),
                        lifecycle = get<PlaylistMviFragment>().lifecycle.asEssentyLifecycle(),
                        log = get(),
                        playlistOrchestrator = get(),
                        playlistItemOrchestrator = get(),
                        mediaOrchestrator = get(),
                    )
                }
                scoped {
                    PlaylistMviStoreFactory(
                        // fixme circular ref in playlistTreeDomain toString
                        //storeFactory = LoggingStoreFactory(DefaultStoreFactory()),
                        storeFactory = DefaultStoreFactory(),
                        playlistOrchestrator = get(),
                        playlistStatsOrchestrator = get(),
                        coroutines = get(),
                        log = get(),
                        prefsWrapper = get(),
//                        platformLauncher = get(),
//                        shareWrapper = get(),
                        playlistItemOrchestrator = get(),
                        mediaOrchestrator = get(),
                        playlistUpdateUsecase = get(),
                        playlistOrDefaultUsecase = get(),
                        dbInit = get(),
                        recentLocalPlaylists = get(),
                        queue = get(),
                        appPlaylistInteractors = get(),
                        playlistMutator = get(),
                        modelMapper = get(),
                        itemModelMapper = get(),
                        util = get(),
                        playUseCase = get(),
                        strings = get(),
                        timeProvider = get()
                    ).create()
                }
                scoped { PlaylistMviModelMapper(get(), get(), get(), get(), get(), get(), get()) }
                scoped { PlaylistMviItemModelMapper(get(), get(), get(), get()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(this.getFragmentActivity(), get()) }
                scoped<PlatformLaunchWrapper> { YoutubeJavaApiWrapper(this.getFragmentActivity(), get()) }
                scoped<ShareWrapper> { AndroidShareWrapper(this.getFragmentActivity()) }
                scoped { ItemFactory(get(), get(), get()) }
                scoped { ItemModelMapper(get(), get(), get(), get()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped { PlaylistHelpConfig(get()) }
                scoped {
                    PlayUseCase(
                        queue = get(),
                        ytCastContextHolder = get(),
                        prefsWrapper = get(),
                        coroutines = get(),
                        floatingService = get(),
                        playDialog = get(),
                        strings = get()
                    )
                }
                scoped<PlayUseCase.Dialog> {
                    PlayDialog(
                        get<PlaylistMviFragment>(),
                        itemFactory = get(),
                        itemModelMapper = get(),
                        navigationRouter = get(),
                        castDialogWrapper = get(),
                        floatingService = get(),
                        log = get(),
                        alertDialogCreator = get(),
                        youtubeApi = get()
                    )
                }
                scoped {
                    PlaylistMviUtil(
                        queue = get(),
                        ytCastContextHolder = get(),
                        multiPrefs = get(),
                    )
                }
            }
        }
    }
}
