package uk.co.sentinelweb.cuer.app.ui.search.image

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.ktx.bindObserver
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageViewModel.UiEvent.Type.*
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.image.ImageSelectIntentHandler
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class SearchImageDialogFragment(private val config: SearchImageContract.Config) : DialogFragment(),
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<SearchImageDialogFragment>()
    private val viewModel: SearchImageViewModel by inject()
    private val log: LogWrapper by inject()
    private val toastWrapper: ToastWrapper by inject()
    private val imageSelectIntentHandler: ImageSelectIntentHandler by inject()

    init {
        log.tag(this)
    }

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Not bound")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        viewModel.setConfig(config)
        binding.composeView.setContent {
            SearchImageView(viewModel = viewModel)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindObserver(viewModel.getUiObservable(), ::observeUi)
    }

    private fun observeUi(model: SearchImageViewModel.UiEvent) {
        when (model.type) {
            ERROR -> toastWrapper.show(model.data as String)
            CLOSE -> dismiss()
            GOTO_LIBRARY -> imageSelectIntentHandler.launchImageChooser(this@SearchImageDialogFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageSelectIntentHandler.onResultReceived(requestCode, resultCode, data!!) {
            viewModel.onImageSelected(it)
        }
    }

    companion object {
        fun newInstance(config: SearchImageContract.Config): SearchImageDialogFragment {
            return SearchImageDialogFragment(config)
        }
    }
}