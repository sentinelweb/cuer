package uk.co.sentinelweb.cuer.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.databinding.FragmentSearchBinding
import uk.co.sentinelweb.cuer.app.ui.common.dialog.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class SearchBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: SearchViewModel by currentScope.inject()
    private val navMapper: NavigationMapper by currentScope.inject()
    private val datePickerCreator: DatePickerCreator by currentScope.inject()
    private val enumPickerCreator: EnumValuesDialogCreator by currentScope.inject()
    private val log: LogWrapper by inject()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var dialogFragment: DialogFragment? = null

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(layoutInflater)
        binding.searchBottomSheet.setContent {
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
        private val SELECT_PLAYLIST_TAG = "pdf_dialog"
        private val SELECT_DATES_TAG = "date_picker"
    }
}