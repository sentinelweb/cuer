package uk.co.sentinelweb.cuer.app.ui.cast

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class CastDialogFragment : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<CastDialogFragment>()
    private val viewModel: CastDialogViewModel by inject()
    private val log: LogWrapper by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("BrowseFragment view not bound")

    init {
        log.tag(this)
    }

    // saves the data on back press (enabled in onResume)
    private val upCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            CastDialogComposeables.CastDialogUi(viewModel)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, upCallback)
        linkScopeToActivity()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(): CastDialogFragment {
            return CastDialogFragment()
        }

        @JvmStatic
        val fragmentModule = module {
            scope(named<CastDialogFragment>()) {
                scoped {
                    CastDialogViewModel(
                        castController = get(),
                        remotesLauncher = get(),
                        coroutines = get()
                    )
                }
            }
        }
    }

}