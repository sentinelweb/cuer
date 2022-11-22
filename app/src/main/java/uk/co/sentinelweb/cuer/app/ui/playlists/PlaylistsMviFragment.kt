package uk.co.sentinelweb.cuer.app.ui.playlists

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.core.view.BaseMviView
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
import uk.co.sentinelweb.cuer.app.databinding.FragmentPlaylistsBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.common.ktx.setMenuItemsColor
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.views.HeaderFooterDecoration
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment.Companion.SEARCH_BOTTOMSHEET_TAG
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.image.ImageProvider
import uk.co.sentinelweb.cuer.app.util.image.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlaylistsMviFragment :
    Fragment(),
    ItemContract.Interactions,
    ItemBaseContract.ItemMoveInteractions,
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PlaylistsMviFragment>()
    private val controller: PlaylistsMviController by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val imageProvider: ImageProvider by inject()
    private val log: LogWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val playlistsHelpConfig: PlaylistsHelpConfig by inject()

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewProxy: ViewProxy

    private val adapter: PlaylistsAdapter
        get() = _adapter ?: throw IllegalStateException("PlaylistsAdapter not bound")
    private var _adapter: PlaylistsAdapter? = null

    private val searchMenuItem: MenuItem
        get() = binding.playlistsToolbar.menu.findItem(R.id.playlists_search)
    private val addMenuItem: MenuItem
        get() = binding.playlistsToolbar.menu.findItem(R.id.playlists_add)

    private val helpMenuItem: MenuItem
        get() = binding.playlistsToolbar.menu.findItem(R.id.playlists_help)

    private var snackbar: Snackbar? = null
    private var dialogFragment: DialogFragment? = null

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sharedElementReturnTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(layoutInflater)
        return binding.root
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _adapter = PlaylistsAdapter(get(), this)
        binding.playlistsToolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
        }
        binding.playlistsToolbar.title = ""
        binding.playlistsList.layoutManager = LinearLayoutManager(context)
        binding.playlistsList.adapter = adapter
        binding.playlistsList.addItemDecoration(
            HeaderFooterDecoration(0, resources.getDimensionPixelSize(R.dimen.recyclerview_footer))
        )
        ItemTouchHelper(ItemTouchHelperCallback(this))
            .apply { attachToRecyclerView(binding.playlistsList) }
        binding.playlistsSwipe.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        compactPlayerScroll.addScrollListener(binding.playlistsList, this)
        binding.playlistsSwipe.setOnRefreshListener { viewProxy.dispatch(Event.OnRefresh) }
        binding.playlistsSwipe.isRefreshing = true
        binding.playlistsAppbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    binding.playlistsToolbar.menu.setMenuItemsColor(R.color.actionbar_icon_collapsed_csl)
                    edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
                } else if (isShow) {
                    isShow = false
                    binding.playlistsToolbar.menu.setMenuItemsColor(R.color.actionbar_icon_expanded_csl)
                    edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
                }
            }
        })
        viewProxy = ViewProxy()
        controller.onViewCreated(listOf(viewProxy), viewLifecycleOwner.essentyLifecycle())

        postponeEnterTransition()
        binding.playlistsList.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    inner class ViewProxy : BaseMviView<Model, Event>(), PlaylistsMviContract.View {

        override fun processLabel(label: Label) = when (label) {
            is Label.Error -> showError(msg = label.message)
            is Label.Message -> showMessage(msg = label.message)
            Label.Repaint -> repaint()
            is Label.ShowUndo -> showUndo(msg = label.message, undoType = label.undoType)
            is Label.ShowPlaylistsSelector -> showPlaylistSelector(config = label.config)
            is Label.Navigate -> navigate(nav = label.model, label.view as ItemContract.ItemView?)
            is Label.ItemRemoved -> notifyItemRemoved(label.model)
        }.also { log.d(label.toString()) }

        override fun render(model: Model) {
            setList(model)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        linkScopeToActivity()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.playlists_actionbar, menu)
        searchMenuItem.setOnMenuItemClickListener {
            val bottomSheetFragment = SearchBottomSheetFragment()
            bottomSheetFragment.show(childFragmentManager, SEARCH_BOTTOMSHEET_TAG)
            true
        }
        addMenuItem.setOnMenuItemClickListener {
            viewProxy.dispatch(Event.OnCreatePlaylist)
            true
        }
        helpMenuItem.setOnMenuItemClickListener {
            OnboardingFragment.show(requireActivity(), playlistsHelpConfig)
            true
        }
        binding.playlistsToolbar.menu.setMenuItemsColor(R.color.actionbar_icon_expanded_csl)
    }

    override fun onStart() {
        super.onStart()
        compactPlayerScroll.raisePlayer(this)
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
        viewProxy.dispatch(Event.OnRefresh)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        dialogFragment?.dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        snackbar = null
        _adapter = null
        dialogFragment = null
        controller.onViewDestroyed()

    }

    override fun onDestroy() {
        super.onDestroy()
    }
    // endregion

    // region PlaylistContract.View
    private fun setList(model: Model) {
        Glide.with(requireContext())
            .loadFirebaseOrOtherUrl(model.imageUrl, imageProvider)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.playlistsHeaderImage)
        binding.playlistsSwipe.isRefreshing = false
        binding.playlistsItems.text = "${model.items.size}"
        adapter.currentPlaylistId = model.currentPlaylistId
        adapter.setData(model.items, false)
        binding.playlistsSwipe.setOnRefreshListener { viewProxy.dispatch(Event.OnRefresh) }
        binding.playlistsCollapsingToolbar.title = model.title
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun repaint() {
        adapter.notifyDataSetChanged()
    }

    private fun showUndo(msg: String, undoType: PlaylistsMviContract.UndoType) {
        snackbar?.dismiss()
        snackbar = snackbarWrapper.make(msg, length = Snackbar.LENGTH_LONG, actionText = "UNDO") {
            viewProxy.dispatch(Event.OnUndo(undoType))
            snackbar?.dismiss()
        }
        snackbar?.show()
    }

    private fun showMessage(msg: String) {
        snackbar?.dismiss()
        snackbar = snackbarWrapper.make(msg, length = Snackbar.LENGTH_LONG)
            .apply { show() }
    }

    private fun showError(msg: String) {
        snackbar?.dismiss()
        snackbar = snackbarWrapper.makeError(msg)
        snackbar?.show()
    }

    private fun showPlaylistSelector(config: PlaylistsMviDialogContract.Config) {
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment = PlaylistsDialogFragment.newInstance(config)
        dialogFragment?.show(childFragmentManager, "PlaylistsSelector")
    }

    private fun navigate(nav: NavigationModel, sourceView: ItemContract.ItemView?) {
        when (nav.target) {
            NavigationModel.Target.PLAYLIST ->
                sourceView?.let { view ->
                    PlaylistsMviFragmentDirections.actionGotoPlaylist(
                        (nav.params[SOURCE] as OrchestratorContract.Source).toString(),
                        nav.params[IMAGE_URL] as String?,
                        nav.params[PLAYLIST_ID] as Long,
                        (nav.params[PLAY_NOW] ?: false) as Boolean,
                    )
                        .apply { findNavController().navigate(this, view.makeTransitionExtras()) }
                }

            NavigationModel.Target.PLAYLIST_EDIT ->
                sourceView?.let { view ->
                    PlaylistsMviFragmentDirections.actionEditPlaylist(
                        (nav.params[SOURCE] as OrchestratorContract.Source).toString(),
                        nav.params[IMAGE_URL] as String?,
                        nav.params[PLAYLIST_ID] as Long,
                    )
                        .apply { findNavController().navigate(this, view.makeTransitionExtras()) }
                }

            else -> navRouter.navigate(nav)
        }
    }

    /*override*/ fun notifyItemRemoved(model: PlaylistsItemMviContract.Model) {
        adapter.notifyItemRemoved(adapter.data.indexOf(model))
    }
    //endregion

    // region ItemContract.ItemMoveInteractions
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        viewProxy.dispatch(Event.OnMove(fromPosition, toPosition))
        // shows the move while dragging
        adapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemClear() {
        viewProxy.dispatch(Event.OnClearMove)
    }
    //endregion

    // region ItemContract.Interactions
    override fun onClick(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {
        viewProxy.dispatch(Event.OnOpenPlaylist(item, sourceView))
    }

    override fun onRightSwipe(item: PlaylistsItemMviContract.Model) {
        viewProxy.dispatch(Event.OnMoveSwipe(item))
    }

    override fun onLeftSwipe(item: PlaylistsItemMviContract.Model) {
        adapter.notifyItemRemoved(adapter.data.indexOf(item))
        viewProxy.dispatch(Event.OnDelete(item))
    }

    override fun onPlay(
        item: PlaylistsItemMviContract.Model,
        external: Boolean,
        sourceView: ItemContract.ItemView
    ) {
        viewProxy.dispatch(Event.OnPlay(item, external, sourceView))
    }

    override fun onStar(item: PlaylistsItemMviContract.Model) {
        viewProxy.dispatch(Event.OnStar(item))
    }

    override fun onShare(item: PlaylistsItemMviContract.Model) {
        viewProxy.dispatch(Event.OnShare(item))
    }

    override fun onMerge(item: PlaylistsItemMviContract.Model) {
        viewProxy.dispatch(Event.OnMerge(item))
    }

    override fun onImageClick(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {
        viewProxy.dispatch(Event.OnOpenPlaylist(item, sourceView))
    }

    override fun onEdit(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {
        viewProxy.dispatch(Event.OnEdit(item, sourceView))
    }

    override fun onDelete(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {
        viewProxy.dispatch(Event.OnDelete(item))
    }
    //endregion

    class PlaylistsMviStrings(private val rw: ResourceWrapper) : PlaylistsMviContract.Strings() {
        override val playlists_section_app: String
            get() = rw.getString(R.string.playlists_section_app)
        override val playlists_section_recent: String
            get() = rw.getString(R.string.playlists_section_recent)
        override val playlists_section_starred: String
            get() = rw.getString(R.string.playlists_section_starred)
        override val playlists_section_all: String
            get() = rw.getString(R.string.playlists_section_all)

    }

    companion object {
        val fragmentModule = module {
            scope(named<PlaylistsMviFragment>()) {
                scoped {
                    PlaylistsMviController(
                        store = get(),
                        modelMapper = get(),
                        lifecycle = get<PlaylistsMviFragment>().lifecycle.asEssentyLifecycle(),
                        log = get(),
                        playlistOrchestrator = get(),
                    )
                }
                scoped {
                    PlaylistsMviStoreFactory(
                        // fixme circular ref in playlistTreeDomain toString
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory()),
                        storeFactory = DefaultStoreFactory(),
                        playlistOrchestrator = get(),
                        playlistStatsOrchestrator = get(),
                        coroutines = get(),
                        log = get(),
                        prefsWrapper = get(),
                        newMedia = get(),
                        recentItems = get(),
                        localSearch = get(),
                        remoteSearch = get(),
                        recentLocalPlaylists = get(),
                        starredItems = get(),
                        unfinishedItems = get(),
                        strings = get(),
                        platformLauncher = get()
                    ).create()
                }
                scoped<PlaylistsMviContract.Strings> { PlaylistsMviStrings(get()) }
                scoped { PlaylistsMviModelMapper(get()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(this.getFragmentActivity(), get()) }
                scoped<PlatformLaunchWrapper> { YoutubeJavaApiWrapper(this.getFragmentActivity(), get()) }
                scoped { ShareWrapper(this.getFragmentActivity()) }
                scoped { ItemFactory(get()) }
                scoped { ItemModelMapper(get(), get()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped { PlaylistsHelpConfig(get()) }
            }
        }
    }
}
