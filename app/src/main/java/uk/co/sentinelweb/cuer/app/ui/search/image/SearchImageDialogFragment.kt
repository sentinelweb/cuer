package uk.co.sentinelweb.cuer.app.ui.search.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageViewModel.UiEvent.Type.ERROR
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class SearchImageDialogFragment(private val config: SearchImageContract.Config) : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val viewModel: SearchImageViewModel by inject()
    private val log: LogWrapper by inject()
    private val toastWrapper: ToastWrapper by inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()

    init {
        log.tag(this)
    }

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        viewModel.setConfig(config)
        binding.composeView.setContent {
            SearchImageView(viewModel = viewModel)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUi()
    }

    private fun observeUi() {
        viewModel.getUiObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<SearchImageViewModel.UiEvent> {
                override fun onChanged(model: SearchImageViewModel.UiEvent) {
                    when (model.type) {
                        ERROR -> toastWrapper.show(model.data as String)
                        else -> Unit
                    }
                }
            })
    }


    companion object {

        fun newInstance(config: SearchImageContract.Config): SearchImageDialogFragment {
            return SearchImageDialogFragment(config)
        }

    }
}