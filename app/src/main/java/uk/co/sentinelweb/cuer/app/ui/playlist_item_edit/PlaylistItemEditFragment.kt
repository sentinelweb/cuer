package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.context.GlobalContext.get
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.PlaylistItemEditFragmentBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.dialog.*
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.NAV_DONE
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditViewModel.UiEvent.Type.*
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.util.cast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistItem

class PlaylistItemEditFragment : Fragment(), ShareContract.Committer, AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val viewModel: PlaylistItemEditViewModel by inject()
    private val log: LogWrapper by inject()
    private val navMapper: NavigationMapper by inject()
    private val selectDialogCreator: SelectDialogCreator by inject()
    private val res: ResourceWrapper by inject()
    private val castDialogWrapper: CastDialogWrapper by inject()
    private val alertDialogCreator: AlertDialogCreator by inject()
    private val doneNavigation: PlaylistItemEditContract.DoneNavigation by inject()// from activity (see onAttach)
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val commitHost: CommitHost by inject()

    private lateinit var binding: PlaylistItemEditFragmentBinding

    private val starMenuItem: MenuItem
        get() = binding.pleToolbar.menu.findItem(R.id.plie_star)
    private val playMenuItem: MenuItem
        get() = binding.pleToolbar.menu.findItem(R.id.plie_play)
    private val editMenuItem: MenuItem
        get() = binding.pleToolbar.menu.findItem(R.id.plie_play)
    private val launchMenuItem: MenuItem
        get() = binding.pleToolbar.menu.findItem(R.id.plie_launch)

    private var dialog: AppCompatDialog? = null
    private var dialogFragment: DialogFragment? = null

    private object menuState {
        var modelEmpty = false
        var scrolledDown = false
    }

    private val itemArg: PlaylistItemDomain? by lazy {
        PLAYLIST_ITEM.getString(arguments)?.let { deserialisePlaylistItem(it) }
    }

    private val sourceArg: Source by lazy {
        SOURCE.getEnum<Source>(arguments) ?: Source.LOCAL
    }

    private val parentArg: Long? by lazy {
        PLAYLIST_PARENT.getLong(arguments)
    }

    init {
        log.tag(this)
    }

    // saves the data on back press (enabled in onResume)
    private val saveCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.checkToSave()
        }
    }

    // todo something gone wrong with transition - so check to move this to on start or something - check values send in intenet
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        log.d("onCreate id = ${itemArg?.id}")
        itemArg?.id?.apply {
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //postponeEnterTransition()
        binding = PlaylistItemEditFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.pleScroll.doOnPreDraw {
//            startPostponedEnterTransition()
//        }
        binding.pleToolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
        }
        //ple_play_button.setOnClickListener { viewModel.onPlayVideoLocal() }
        //ple_star_fab.setOnClickListener { viewModel.onStarClick() }
        binding.plePlayFab.setOnClickListener { viewModel.onPlayVideo() }
        starMenuItem.isVisible = true
        playMenuItem.isVisible = false
        binding.pleDescription.interactions = viewModel
        binding.pleToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.plie_star -> {
                    viewModel.onStarClick()
                    true
                }
                R.id.plie_edit -> {
                    viewModel.onEditClick()
                    true
                }
                R.id.plie_play -> {
                    viewModel.onPlayVideo()
                    true
                }
                R.id.plie_launch -> {
                    viewModel.onLaunchVideo()
                    true
                }
                else -> false
            }
        }
        binding.pleSwipe.setOnRefreshListener { viewModel.refreshMediaBackground() }
        binding.pleAppbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    // only show the menu items for the non-empty state
                    //starMenuItem.isVisible = !menuState.modelEmpty
                    playMenuItem.isVisible = !menuState.modelEmpty
                    edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
                } else if (isShow) {
                    isShow = false
                    //starMenuItem.isVisible = false
                    playMenuItem.isVisible = false
                    edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
                }
                menuState.scrolledDown = isShow
            }
        })

        // setup data for fragment transition
        itemArg?.apply {
            saveCallback.isEnabled = true
            if (id != null) { // fixme needs new flag assumes transition
                media.image?.apply {
                    Glide.with(requireContext())
                        .load(this.url)
                        .into(binding.pleImage)
                }
                binding.pleDescription.channelImageVisible(false)
                binding.pleTitlePos.isVisible = false
                binding.pleTitleBg.isVisible = false
                playMenuItem.isVisible = false
            }
            viewModel.setData(this, sourceArg, parentArg)
        }

        observeUi()
        observeModel()
        observeNavigation()
        observeDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.playlist_item_edit_actionbar, menu)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, saveCallback)
        linkScopeToActivity()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /* init */ viewModel
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
    }

    override fun onStop() {
        super.onStop()
        dialogFragment?.dismissAllowingStateLoss()
    }

    private fun observeUi() {
        viewModel.getUiObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistItemEditViewModel.UiEvent> {
                override fun onChanged(model: PlaylistItemEditViewModel.UiEvent) {
                    when (model.type) {
                        REFRESHING -> {
                            val refreshing = model.data as Boolean
                            binding.pleSwipe.isRefreshing = refreshing
                            if (refreshing) {
                                commitHost.isReady(false)
                            }
                        }
                        ERROR -> snackbarWrapper.makeError(model.data as String).show()
                        UNPIN -> snackbarWrapper
                            .make("Unpin playlist?", actionText = "UNPIN", action = { viewModel.onUnPin() })
                            .show()
                    }
                }
            })
    }

    private fun observeModel() {
        viewModel.getModelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistItemEditContract.Model> {
                override fun onChanged(model: PlaylistItemEditContract.Model) {
                    binding.pleTitleBg.isVisible = true
                    binding.pleTitlePos.isVisible = !model.empty
                    binding.pleDuration.isVisible = !model.empty
                    //ple_star_fab.isVisible = !model.empty
                    if (menuState.scrolledDown) {
                        //starMenuItem.isVisible = !model.empty
                        playMenuItem.isVisible = !model.empty
                    } else {
                        //starMenuItem.isVisible = false
                        playMenuItem.isVisible = false
                    }
                    val imageUrl = model.imageUrl
                    setImage(imageUrl)

                    binding.pleCollapsingToolbar.title = model.description.title
                    binding.pleToolbar.title = model.description.title
                    menuState.modelEmpty = model.empty
                    binding.pleSwipe.isRefreshing = false
                    commitHost.isReady(!model.empty)
                    if (model.empty) {
                        return
                    }

                    binding.pleDescription.setModel(model.description)
                    binding.pleDuration.text = model.durationText
                    binding.pleDuration.setBackgroundColor(res.getColor(model.infoTextBackgroundColor))

                    model.position?.let { ratio ->
                        binding.pleTitlePos.layoutParams.width =
                            (ratio * binding.pleTitleBg.width).toInt()
                    } ?: binding.pleTitlePos.apply { isVisible = false }
                    val starIconResource =
                        if (model.starred) R.drawable.ic_button_starred_white
                        else R.drawable.ic_button_unstarred_white
                    starMenuItem.setIcon(starIconResource)
                }
            })
    }

    private fun setImage(imageUrl: String?) {
        Glide.with(requireContext())
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.pleImage)
    }

    private fun observeNavigation() {
        viewModel.getNavigationObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<NavigationModel> {
                override fun onChanged(nav: NavigationModel) {
                    when (nav.target) {
                        NAV_DONE -> doneNavigation.navigateDone()//navigateDone()
                        else -> navMapper.navigate(nav)
                    }
                }
            }
        )
    }

    private fun observeDialog() {
        viewModel.getDialogObservable().observe(this.viewLifecycleOwner,
            object : Observer<DialogModel> {
                override fun onChanged(model: DialogModel) {
                    dialog?.dismiss()
                    hideDialogFragment()
                    when (model.type) {
                        DialogModel.Type.PLAYLIST_FULL -> {
                            dialogFragment =
                                PlaylistsDialogFragment.newInstance(model as PlaylistsDialogContract.Config)
                            dialogFragment?.show(childFragmentManager, SELECT_PLAYLIST_TAG)
                        }
                        DialogModel.Type.PLAYLIST_ADD -> {
                            // todo need a callback to select the parent in the add dialog i.e. another type DialogModel.Type.PLAYLIST_SELECT_PARENT ??
                            // todo also to select the image
                            dialogFragment = PlaylistEditFragment.newInstance()
                                .apply {
                                    listener = object : PlaylistEditFragment.Listener {
                                        override fun onPlaylistCommit(domain: PlaylistDomain?) {
                                            domain?.apply { viewModel.onPlaylistCreated(this) }
                                            dialogFragment?.dismissAllowingStateLoss()
                                            hideDialogFragment()
                                        }
                                    }
                                }
                            dialogFragment?.show(childFragmentManager, CREATE_PLAYLIST_TAG)
                        }
                        DialogModel.Type.SELECT_ROUTE -> {
                            castDialogWrapper.showRouteSelector(childFragmentManager)
                        }
                        DialogModel.Type.CONFIRM -> {
                            alertDialogCreator.create(model as AlertDialogModel).show()
                        }
                        DialogModel.Type.PLAYLIST -> {
                            selectDialogCreator
                                .createMulti(model as SelectDialogModel)
                                .apply { show() }
                        }
                        DialogModel.Type.PLAYLIST_ITEM_SETTNGS -> {
                            selectDialogCreator
                                .createMulti(model as SelectDialogModel)
                                .apply { show() }
                        }

                        else -> Unit
                    }
                }
            }
        )
    }

    private fun hideDialogFragment() {
        dialogFragment?.let {
            val ft = childFragmentManager.beginTransaction()
            ft.hide(it)
            ft.commit()
        }
        dialogFragment = null
    }

    override suspend fun commit(onCommit: ShareContract.Committer.OnCommit) {
        viewModel.commitPlaylistItems(onCommit)
    }

    companion object {
        private val CREATE_PLAYLIST_TAG = "pe_dialog"
        private val SELECT_PLAYLIST_TAG = "pdf_dialog"

        val TRANS_IMAGE by lazy { get().get<ResourceWrapper>().getString(R.string.playlist_item_trans_image) }
        val TRANS_TITLE by lazy { get().get<ResourceWrapper>().getString(R.string.playlist_item_trans_title) }
    }
}
