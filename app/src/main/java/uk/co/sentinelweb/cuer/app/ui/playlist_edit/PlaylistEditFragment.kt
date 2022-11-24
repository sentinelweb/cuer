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
import uk.co.sentinelweb.cuer.app.ui.common.ktx.scaleDrawableLeftSize
import uk.co.sentinelweb.cuer.app.ui.common.ktx.setMenuItemsColor
import uk.co.sentinelweb.cuer.app.ui.common.navigation.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.Flag.*
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.UiEvent.Type.*
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsVMDialogFragment
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageContract
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageDialogFragment
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
    private val res: ResourceWrapper by inject()
    private val playlistEditHelpConfig: PlaylistEditHelpConfig by inject()

    internal var listener: Listener? = null

    private var _binding: FragmentPlaylistEditBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("FragmentPlaylistEditBinding not bound")

    private var dialogFragment: DialogFragment? = null

    private val helpMenuItem: MenuItem
        get() = binding.peToolbar.menu.findItem(R.id.pe_help)

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
        savedInstanceState
            ?.getString(STATE_KEY)
            ?.apply { viewModel.restoreState(this) }
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

    private val onOffsetChangedListener = object : AppBarLayout.OnOffsetChangedListener {

        var isShow = false
        var scrollRange = -1

        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
            if (scrollRange == -1) {
                scrollRange = appBarLayout.getTotalScrollRange()
            }
            if (scrollRange + verticalOffset == 0) {
                isShow = true
                // only show the menu items for the non-empty state
                binding.peToolbar.menu.setMenuItemsColor(R.color.actionbar_icon_collapsed_csl)
                edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
            } else if (isShow) {
                isShow = false
                binding.peToolbar.menu.setMenuItemsColor(R.color.actionbar_icon_expanded_csl)
                edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.peStarFab.setOnClickListener { viewModel.onStarClick() }
        binding.pePinFab.setOnClickListener { viewModel.onPinClick() }
        val isDialog = this.dialog != null
        viewModel.setIsDialog(isDialog)
        binding.peClickPrompt.isVisible = !isDialog
        binding.peToolbar.title = ""
        binding.peTitleEdit.doAfterTextChanged { text -> viewModel.onTitleChanged(text.toString()) }
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

        binding.pePlayable.scaleDrawableLeftSize(1f)
        binding.peDeletable.scaleDrawableLeftSize(0.8f)
        binding.pePlayable.scaleDrawableLeftSize(0.8f)
        binding.peEditableItems.scaleDrawableLeftSize(0.7f)
        binding.peDeletableItems.scaleDrawableLeftSize(0.7f)
        binding.peDefault.scaleDrawableLeftSize(0.8f)
        binding.pePlayStart.scaleDrawableLeftSize(0.8f)
        binding.peParentLabel.scaleDrawableLeftSize(0.7f)

        binding.peAppbar.addOnOffsetChangedListener(onOffsetChangedListener)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        helpMenuItem.setOnMenuItemClickListener {
            OnboardingFragment.show(requireActivity(), playlistEditHelpConfig)
            true
        }
        binding.peToolbar.menu.setMenuItemsColor(R.color.actionbar_icon_expanded_csl)
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
        binding.peAppbar.removeOnOffsetChangedListener(onOffsetChangedListener)
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
        binding.peCollapsingToolbar.title = getString(R.string.edit_prefix) + model.titleDisplay
        binding.peClickPrompt.isVisible = !model.isDialog

        binding.peStarFab.setIconResource(
            if (model.starred) R.drawable.ic_starred
            else R.drawable.ic_starred_off
        )
        binding.peStarFab.setText(
            if (model.starred) R.string.menu_unstar
            else R.string.menu_star
        )
        binding.pePinFab.setIconResource(
            if (model.pinned) R.drawable.ic_push_pin_on
            else R.drawable.ic_push_pin_off
        )
        binding.pePinFab.setText(
            if (model.pinned) R.string.menu_unpin
            else R.string.menu_pin
        )

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

        binding.peDividerOtherActions.isVisible = !model.isCreate
        binding.peOtherActionsTitle.isVisible = !model.isCreate
        binding.peWatchAll.isVisible = !model.isCreate

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
            is PlaylistsMviDialogContract.Config -> {
                dialogFragment =
                    PlaylistsVMDialogFragment.newInstance(model)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_KEY, viewModel.serializeState())
    }

    companion object {
        private const val STATE_KEY = "playlist_edit_state"
        fun newInstance(): PlaylistEditFragment {
            return PlaylistEditFragment()
        }
    }
}
