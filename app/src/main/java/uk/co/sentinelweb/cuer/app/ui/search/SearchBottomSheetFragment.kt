package uk.co.sentinelweb.cuer.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.databinding.FragmentSearchBinding
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel.Type.PLAYLIST_FULL
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment

class SearchBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: SearchViewModel by currentScope.inject()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var dialogFragment: DialogFragment? = null

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
                    when (model.type) {
                        PLAYLIST_FULL -> {
                            dialogFragment =
                                PlaylistsDialogFragment.newInstance(model as PlaylistsDialogContract.Config)
                            dialogFragment?.show(childFragmentManager, SELECT_PLAYLIST_TAG)
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

    companion object {
        private val SELECT_PLAYLIST_TAG = "pdf_dialog"
    }
}