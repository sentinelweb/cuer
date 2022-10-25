package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.roche.mdas.util.wrapper.SoftKeyboardWrapper
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.FragmentPlaylistEditBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel.Type.PLAYLIST_FULL
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.ktx.bindObserver
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.Flag.*
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.UiEvent.Type.*
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageContract
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageDialogFragment
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.image.ImageProvider
import uk.co.sentinelweb.cuer.app.util.image.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistEditFragment : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PlaylistEditFragment>()
    private val viewModel: PlaylistEditViewModel by inject()
    private val chipCreator: ChipCreator by inject()
    private val log: LogWrapper by inject()
    private val imageProvider: ImageProvider by inject()
    private val softKeyboard: SoftKeyboardWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val toastWrapper: ToastWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val commitHost: CommitHost by inject()

    private val starMenuItem: MenuItem
        get() = binding.peToolbar.menu.findItem(R.id.pe_star)
    private val pinMenuItem: MenuItem
        get() = binding.peToolbar.menu.findItem(R.id.pe_pin)

    internal var listener: Listener? = null

    private var _binding: FragmentPlaylistEditBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("FragmentPlaylistEditBinding not bound")

    private var dialogFragment: DialogFragment? = null

    private val playlistIdArg: Long? by lazy {
        PLAYLIST_ID.getLong(arguments)?.takeIf { it > 0 || it < -1 }
    }

    private val imageUrlArg: String? by lazy {
        IMAGE_URL.getString(arguments)
    }

    interface Listener {
        fun onPlaylistCommit(domain: PlaylistDomain?)
    }

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        playlistIdArg?.apply {
            sharedElementEnterTransition =
                TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //postponeEnterTransition()
        _binding = FragmentPlaylistEditBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.peScroll.doOnPreDraw {
//            startPostponedEnterTransition()
//        }
        binding.peStarFab.setOnClickListener { viewModel.onStarClick() }
        binding.pePinFab.setOnClickListener { viewModel.onPinClick() }
        starMenuItem.isVisible = false
        pinMenuItem.isVisible = false
        val isDialog = this.dialog != null
        viewModel.setIsDialog(isDialog)
        binding.peClickPrompt.isVisible = !isDialog
        binding.peToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.pe_star -> {
                    viewModel.onStarClick()
                    true
                }
                R.id.pe_pin -> {
                    viewModel.onPinClick()
                    true
                }
                else -> false
            }
        }

        binding.peImage.setOnTouchListener { iv, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    viewModel.onImageClick(e.x > iv.width / 2)
                    true
                }
                else -> true
            }
        }
        binding.peCommitButton.setOnClickListener { viewModel.onCommitClick() }
        binding.peWatchAll.setOnClickListener { viewModel.onWatchAllClick() }
        binding.pePlayStart.setOnCheckedChangeListener { v, b ->
            viewModel.onFlagChanged(b, PLAY_START)
        }
        binding.peDefault.setOnCheckedChangeListener { v, b -> viewModel.onFlagChanged(b, DEFAULT) }
        binding.pePlayable.setOnCheckedChangeListener { v, b ->
            viewModel.onFlagChanged(b, PLAYABLE)
        }
        binding.peDeletable.setOnCheckedChangeListener { v, b ->
            viewModel.onFlagChanged(b, DELETABLE)
        }
        binding.peEditableItems.setOnCheckedChangeListener { v, b ->
            viewModel.onFlagChanged(b, EDIT_ITEMS)
        }
        binding.peDeletableItems.setOnCheckedChangeListener { v, b ->
            viewModel.onFlagChanged(b, DELETE_ITEMS)
        }
        binding.peTitleEdit.doAfterTextChanged { text -> viewModel.onTitleChanged(text.toString()) }
        binding.peAppbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    // only show the menu items for the non-empty state
                    starMenuItem.isVisible = binding.peStarFab.isVisible
                    pinMenuItem.isVisible = binding.pePinFab.isVisible
                    edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
                } else if (isShow) {
                    isShow = false
                    starMenuItem.isVisible = false
                    pinMenuItem.isVisible = false
                    edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
                }
            }
        })
        binding.peInfo.setMovementMethod(object : LinkMovementMethod() {
            override fun handleMovementKey(
                widget: TextView?,
                buffer: Spannable?,
                keyCode: Int,
                movementMetaState: Int,
                event: KeyEvent?
            ): Boolean {
                buffer?.run { viewModel.onLinkClick(this.toString()) }
                return true
            }
        })
        compactPlayerScroll.addScrollListener(binding.peScroll, this)
        bindObserver(viewModel.getUiObservable(), ::observeUi)
        bindObserver(viewModel.getModelObservable(), ::observeModel)
        bindObserver(viewModel.getDomainObservable(), ::observeDomain)
        bindObserver(viewModel.getDialogObservable(), ::observeDialog)
        bindObserver(viewModel.getNavigationObservable(), ::observeNavigation)

        imageUrlArg?.also { setImage(it) }
        (playlistIdArg to (SOURCE.getEnum(arguments) ?: Source.LOCAL))
            .apply { viewModel.setData(first, second) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        linkScopeToActivity()
    }

    override fun onStart() {
        super.onStart()
        compactPlayerScroll.raisePlayer(this)
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
    }

    override fun onStop() {
        super.onStop()
        if (!(dialogFragment is SearchImageDialogFragment)) {
            dialogFragment?.dismissAllowingStateLoss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun observeUi(model: PlaylistEditViewModel.UiEvent) =
        when (model.type) {
            ERROR -> snackbarWrapper.makeError(model.data as String).show()
            MESSAGE -> toastWrapper.show(model.data as String)
            IMAGE -> setImage(model.data as String?)
        }


    private fun observeModel(model: PlaylistEditContract.Model) {
        if (binding.peTitleEdit.text.toString() != model.titleEdit) {
            binding.peTitleEdit.setText(model.titleEdit)
            binding.peTitleEdit.setSelection(model.titleEdit.length)
        }
        binding.peCollapsingToolbar.title = model.titleDisplay
        binding.peClickPrompt.isVisible = !model.isDialog
        val starIconResource =
            if (model.starred) R.drawable.ic_starred
            else R.drawable.ic_starred_off
        starMenuItem.setIcon(starIconResource)
        binding.peStarFab.setImageResource(starIconResource)

        val pinIconResource =
            if (model.pinned) R.drawable.ic_push_pin_on
            else R.drawable.ic_push_pin_off
        pinMenuItem.setIcon(pinIconResource)
        binding.pePinFab.setImageResource(pinIconResource)

        model.buttonText?.apply { binding.peCommitButton.text = this }
        model.imageUrl?.also { setImage(it) }
        binding.peWatchAll.setText(model.watchAllText)
        binding.peWatchAll.setIconResource(model.watchAllIIcon)
        binding.peDefault.isChecked = model.default
        binding.peDefault.isVisible = model.showDefault
        binding.peInfo.text = Html.fromHtml(model.info, Html.FROM_HTML_MODE_LEGACY)
        binding.pePlayStart.isChecked = model.playFromStart
        binding.pePlayable.isChecked = model.config.playable
        binding.peDeletable.isChecked = model.config.deletable
        binding.peDeletableItems.isChecked = model.config.deletableItems
        binding.peEditableItems.isChecked = model.config.editableItems
        binding.peParentChip.removeAllViews()
        chipCreator.create(model.chip, binding.peParentChip).apply {
            binding.peParentChip.addView(this)
            when (model.chip.type) {
                ChipModel.Type.PLAYLIST_SELECT -> {
                    setOnClickListener { viewModel.onSelectParent() }
                }
                ChipModel.Type.PLAYLIST -> {
                    setOnCloseIconClickListener { viewModel.onRemoveParent() }
                }
            }
        }
        commitHost.isReady(false)
        model.validation?.apply {
            binding.peCommitButton.isEnabled = valid
            if (!valid) {
                fieldValidations.forEach {
                    when (it.field) {
                        PlaylistValidator.PlaylistField.TITLE ->
                            binding.peTitle.error = getString(it.error)
                    }
                }
            } else {
                binding.peTitle.error = null
            }
        }
    }

    private fun setImage(url: String?) {
        url?.let {
            Glide.with(binding.peImage.context)
                .loadFirebaseOrOtherUrl(it, imageProvider)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.peImage)
        }
    }

    private fun observeDialog(model: DialogModel) {
        //dialog?.dismiss() // removed 278
        hideDialogFragment()
        when (model) {
            is PlaylistsDialogContract.Config -> {
                dialogFragment =
                    PlaylistsDialogFragment.newInstance(model)
                dialogFragment?.show(childFragmentManager, PLAYLIST_FULL.toString())
            }
            is SearchImageContract.Config -> {
                dialogFragment =
                    SearchImageDialogFragment.newInstance(model)
                dialogFragment?.show(childFragmentManager, PLAYLIST_FULL.toString())
            }
            is DialogModel.DismissDialogModel -> dialogFragment?.dismiss()
            else -> Unit
        }
    }

    private fun observeNavigation(nav: NavigationModel) = navRouter.navigate(nav)

    private fun observeDomain(domain: PlaylistDomain) {
        softKeyboard.hideSoftKeyboard(binding.peTitleEdit)
        listener
            ?.onPlaylistCommit(domain)
            ?: findNavController().popBackStack()
    }

    private fun hideDialogFragment() {
        dialogFragment?.let {
            val ft = childFragmentManager.beginTransaction()
            ft.hide(it)
            ft.commit()
        }
        dialogFragment = null
    }

    companion object {
        fun newInstance(): PlaylistEditFragment {
            return PlaylistEditFragment()
        }
    }
}
