package uk.co.sentinelweb.cuer.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.dialog.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class SearchBottomSheetFragment : BottomSheetDialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val viewModel: SearchViewModel by inject()
    private val navMapper: NavigationMapper by inject()
    private val datePickerCreator: DatePickerCreator by inject()
    private val enumPickerCreator: EnumValuesDialogCreator by inject()
    private val log: LogWrapper by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Not bound")

    private var dialogFragment: DialogFragment? = null

    init {
        log.tag(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        binding.composeView.setContent {
            SearchView(viewModel = viewModel)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDialog()
        observeNavigation()
    }

    override fun onStop() {
        super.onStop()
        dialogFragment?.dismissAllowingStateLoss()
    }

    private fun observeDialog() {
        viewModel.getDialogObservable().observe(this.viewLifecycleOwner,
            object : Observer<DialogModel> {
                override fun onChanged(model: DialogModel) {
                    hideDialogFragment()
                    when (model) {
                        is PlaylistsDialogContract.Config -> {
                            dialogFragment = PlaylistsDialogFragment.newInstance(model)
                                .also { it.show(childFragmentManager, SELECT_PLAYLIST_TAG) }
                        }
                        is DateRangePickerDialogModel -> {
                            dialogFragment = datePickerCreator.createDateRangePicker(model)
                                .also { it.show(childFragmentManager, SELECT_DATES_TAG) }
                        }
                        is EnumValuesDialogModel<*> -> {
                            log.d("Show order")
                            enumPickerCreator.create(model).show()
                        }
                        is DialogModel.DismissDialogModel -> {
                            hideDialogFragment()
                        }
                        else -> Unit
                    }
                }
            }
        )
    }

    private fun observeNavigation() {
        viewModel.getNavigationObservable().observe(this.viewLifecycleOwner,
            object : Observer<NavigationModel> {
                override fun onChanged(nav: NavigationModel) {
                    when (nav.target) {
                        else -> navMapper.navigate(nav)
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

    companion object {
        const val SEARCH_BOTTOMSHEET_TAG = "searchBottomSheetFragmentTag"
        private const val SELECT_PLAYLIST_TAG = "pdf_dialog"
        private const val SELECT_DATES_TAG = "date_picker"
    }
}