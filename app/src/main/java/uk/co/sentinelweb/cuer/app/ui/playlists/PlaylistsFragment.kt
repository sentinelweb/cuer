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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.FragmentPlaylistsBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.common.ktx.setMenuItemsColor
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.views.HeaderFooterDecoration
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment.Companion.SEARCH_BOTTOMSHEET_TAG
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.image.ImageProvider
import uk.co.sentinelweb.cuer.app.util.image.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlaylistsFragment :
    Fragment(),
    PlaylistsContract.View,
    ItemContract.Interactions,
    ItemBaseContract.ItemMoveInteractions,
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PlaylistsFragment>()
    private val presenter: PlaylistsContract.Presenter by inject()
    private val adapter: PlaylistsAdapter
        get() = _adapter ?: throw IllegalStateException("FragmentPlaylistEditBinding not bound")
    private var _adapter: PlaylistsAdapter? = null
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val imageProvider: ImageProvider by inject()
    private val log: LogWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val res: ResourceWrapper by inject()
    private val playlistsHelpConfig: PlaylistsHelpConfig by inject()

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

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
        binding.playlistsSwipe.setOnRefreshListener { presenter.refreshList() }
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

        postponeEnterTransition()
        binding.playlistsList.doOnPreDraw {
            startPostponedEnterTransition()
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
            presenter.onCreatePlaylist()
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
        presenter.onResume(PLAYLIST_ID.getLong(arguments))
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
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
        binding.playlistsCollapsingToolbar.title = model.title
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun repaint() {
        adapter.notifyDataSetChanged()
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

    override fun showError(msg: String) {
        snackbar?.dismiss()
        snackbar = snackbarWrapper.makeError(msg)
        snackbar?.show()
    }

//    override fun gotoPlaylist(id: Long, play: Boolean, source: Source) {
//        PlaylistsFragmentDirections.actionGotoPlaylist(id, play, source.toString())
//            .apply { findNavController().navigate(this) }
//
//    }
//
//    override fun gotoEdit(id: Long, source: Source) {
//        PlaylistsFragmentDirections.actionEditPlaylist(id, source.toString())
//            .apply { findNavController().navigate(this) }
//    }

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

    override fun hideRefresh() {
        binding.playlistsSwipe.isRefreshing = false
    }

    override fun showPlaylistSelector(model: PlaylistsDialogContract.Config) {
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment = PlaylistsDialogFragment.newInstance(model)
        dialogFragment?.show(childFragmentManager, "PlaylistsSelector")
    }

    override fun navigate(nav: NavigationModel, sourceView: ItemContract.ItemView?) {
        when (nav.target) {
            NavigationModel.Target.PLAYLIST ->
                sourceView?.let { view ->
                    PlaylistsFragmentDirections.actionGotoPlaylist(
                        (nav.params[SOURCE] as OrchestratorContract.Source).toString(),
                        nav.params[IMAGE_URL] as String?,
                        nav.params[PLAYLIST_ID] as Long,
                        nav.params[PLAY_NOW] as Boolean,
                    )
                        .apply { findNavController().navigate(this, view.makeTransitionExtras()) }
                }

            NavigationModel.Target.PLAYLIST_EDIT ->
                sourceView?.let { view ->
                    PlaylistsFragmentDirections.actionEditPlaylist(
                        (nav.params[SOURCE] as OrchestratorContract.Source).toString(),
                        nav.params[IMAGE_URL] as String?,
                        nav.params[PLAYLIST_ID] as Long,
                    )
                        .apply { findNavController().navigate(this, view.makeTransitionExtras()) }
                }

            else -> navRouter.navigate(nav)
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
    override fun onClick(item: ItemMviContract.Model, sourceView: ItemContract.ItemView) {
        presenter.performOpen(item, sourceView)
    }

    override fun onRightSwipe(item: ItemMviContract.Model) {
        presenter.performMove(item)
    }

    override fun onLeftSwipe(item: ItemMviContract.Model) {
        adapter.notifyItemRemoved(adapter.data.indexOf(item))
        presenter.performDelete(item) // delays for animation
    }

    override fun onPlay(
        item: ItemMviContract.Model,
        external: Boolean,
        sourceView: ItemContract.ItemView
    ) {
        presenter.performPlay(item, external, sourceView)
    }

    override fun onStar(item: ItemMviContract.Model) {
        presenter.performStar(item)
    }

    override fun onShare(item: ItemMviContract.Model) {
        presenter.performShare(item)
    }

    override fun onMerge(item: ItemMviContract.Model) {
        presenter.performMerge(item)
    }

    override fun onImageClick(item: ItemMviContract.Model, sourceView: ItemContract.ItemView) {
        presenter.onItemImageClicked(item, sourceView)
    }

    override fun onEdit(item: ItemMviContract.Model, sourceView: ItemContract.ItemView) {
        presenter.performEdit(item, sourceView)
    }

    override fun onDelete(item: ItemMviContract.Model, sourceView: ItemContract.ItemView) {
        presenter.performDelete(item) // delays for animation
    }

    override fun notifyItemRemoved(model: ItemMviContract.Model) {
        adapter.notifyItemRemoved(adapter.data.indexOf(model))
    }
    //endregion
}
