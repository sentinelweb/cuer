package uk.co.sentinelweb.cuer.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.databinding.FragmentSearchBinding

class SearchBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: SearchViewModel by currentScope.inject()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

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
}