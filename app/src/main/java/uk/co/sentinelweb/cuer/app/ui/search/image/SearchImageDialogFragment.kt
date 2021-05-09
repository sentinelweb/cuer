package uk.co.sentinelweb.cuer.app.ui.search.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.databinding.FragmentImageSearchBinding
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class SearchImageDialogFragment(private val config: SearchImageContract.Config) : DialogFragment() {

    private val viewModel: SearchImageViewModel by currentScope.inject()
    private val log: LogWrapper by inject()

    init {
        log.tag(this)
    }

    private var _binding: FragmentImageSearchBinding? = null
    private val binding get() = _binding!!
//private var _binding: FragmentComposeBinding? = null
//    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImageSearchBinding.inflate(layoutInflater)
        viewModel.setConfig(config)
//        binding.composeView.setContent {
//            SearchImageView(viewModel = viewModel)
//        }
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//    }

    companion object {

        fun newInstance(config: SearchImageContract.Config): SearchImageDialogFragment {
            return SearchImageDialogFragment(config)
        }

    }
}