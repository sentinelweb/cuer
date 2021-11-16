package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.roche.mdas.util.wrapper.SoftKeyboardWrapper
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.PlaylistEditFragmentBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel.Type.PLAYLIST_FULL
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.Flag.*
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.UiEvent.Type.ERROR
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.UiEvent.Type.MESSAGE
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageContract
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageDialogFragment
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.image.ImageProvider
import uk.co.sentinelweb.cuer.app.util.image.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistEditFragment : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val viewModel: PlaylistEditViewModel by inject()
    private val chipCreator: ChipCreator by inject()
    private val log: LogWrapper by inject()
    private val imageProvider: ImageProvider by inject()
    private val softKeyboard: SoftKeyboardWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val toastWrapper: ToastWrapper by inject()
    private val navMapper: NavigationMapper by inject()

    private val starMenuItem: MenuItem
        get() = binding.peToolbar.menu.findItem(R.id.pe_star)
    private val pinMenuItem: MenuItem
        get() = binding.peToolbar.menu.findItem(R.id.pe_pin)

    internal var listener: Listener? = null

    private var _binding: PlaylistEditFragmentBinding? = null
    private val binding get() = _binding!!

    private var dialogFragment: DialogFragment? = null

    interface Listener {
        fun onPlaylistCommit(domain: PlaylistDomain?)
    }

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlaylistEditFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            viewModel.onFlagChanged(
                b,
                PLAY_START
            )
        }
        binding.peDefault.setOnCheckedChangeListener { v, b -> viewModel.onFlagChanged(b, DEFAULT) }
        binding.pePlayable.setOnCheckedChangeListener { v, b ->
            viewModel.onFlagChanged(
                b,
                PLAYABLE
            )
        }
        binding.peDeletable.setOnCheckedChangeListener { v, b ->
            viewModel.onFlagChanged(
                b,
                DELETABLE
            )
        }
        binding.peEditableItems.setOnCheckedChangeListener { v, b ->
            viewModel.onFlagChanged(
                b,
                EDIT_ITEMS
            )
        }
        binding.peDeletableItems.setOnCheckedChangeListener { v, b ->
            viewModel.onFlagChanged(
                b,
                DELETE_ITEMS
            )
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

        observeUi()
        observeModel()
        observeDomain()
        observeDialog()
        observeNavigation()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /* init */ viewModel
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
        (PLAYLIST_ID.getLong(arguments) to (SOURCE.getEnum(arguments) ?: Source.LOCAL)).apply {
            viewModel.setData(first?.takeIf { it > 0 }, second)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!(dialogFragment is SearchImageDialogFragment)) {
            dialogFragment?.dismissAllowingStateLoss()
        }
    }

    private fun observeUi() {
        viewModel.getUiObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistEditViewModel.UiEvent> {
                override fun onChanged(model: PlaylistEditViewModel.UiEvent) {
                    when (model.type) {
                        ERROR -> snackbarWrapper.makeError(model.data as String).show()
                        MESSAGE -> toastWrapper.show(model.data as String)
                    }
                }
            })
    }

    private fun observeModel() {
        viewModel.getModelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistEditContract.Model> {
                override fun onChanged(model: PlaylistEditContract.Model) {
                    if (binding.peTitleEdit.text.toString() != model.titleEdit) {
                        binding.peTitleEdit.setText(model.titleEdit)
                        binding.peTitleEdit.setSelection(model.titleEdit.length)
                    }
                    binding.peCollapsingToolbar.title = model.titleDisplay
                    binding.peClickPrompt.isVisible = !model.isDialog
                    val starIconResource =
                        if (model.starred) R.drawable.ic_button_starred_white
                        else R.drawable.ic_button_unstarred_white
                    starMenuItem.setIcon(starIconResource)
                    binding.peStarFab.setImageResource(starIconResource)

                    val pinIconResource =
                        if (model.pinned) R.drawable.ic_push_pin_on_24
                        else R.drawable.ic_push_pin_off_24
                    pinMenuItem.setIcon(pinIconResource)
                    binding.pePinFab.setImageResource(pinIconResource)

                    model.buttonText?.apply { binding.peCommitButton.text = this }
                    model.imageUrl?.let {
                        Glide.with(binding.peImage.context)
                            .loadFirebaseOrOtherUrl(it, imageProvider)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(binding.peImage)
                    }
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
            })
    }

    private fun observeDialog() =
        viewModel.getDialogObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<DialogModel> {
                override fun onChanged(model: DialogModel) {
                    dialog?.dismiss()
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
            }
        )

    private fun observeNavigation() {
        viewModel.getNavigationObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<NavigationModel> {
                override fun onChanged(nav: NavigationModel) {
                    navMapper.navigate(nav)
                }
            }
        )
    }

    private fun observeDomain() {
        viewModel.getDomainObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistDomain> {
                override fun onChanged(domain: PlaylistDomain) {
                    softKeyboard.hideSoftKeyboard(binding.peTitleEdit)
                    listener?.onPlaylistCommit(domain)
                        ?: findNavController().popBackStack()
                }
            })
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
