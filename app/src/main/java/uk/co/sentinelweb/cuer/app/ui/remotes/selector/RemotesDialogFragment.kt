package uk.co.sentinelweb.cuer.app.ui.remotes.selector

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
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

class RemotesDialogFragment(
    private val selectedListener: (NodeDomain, PlayerNodeDomain.Screen?) -> Unit,
    private val selectedNode: RemoteNodeDomain?,
    private val isSelectNodeOnly: Boolean,
) : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<RemotesDialogFragment>()
    private val viewModel: NodesDialogViewModel by inject()
    private val log: LogWrapper by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("RemotesDialogFragment view not bound")

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
        viewModel.listener = selectedListener
        if (isSelectNodeOnly) {
            viewModel.setSelectNodeOnly()
        } else {
            selectedNode
                ?.apply { viewModel.onNodeSelected(this) }
        }
        binding.composeView.setContent {
            NodesDialogComposeables.RemotesDialogUi(viewModel)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, upCallback)
        linkScopeToActivity()
    }

    override fun onStart() {
        super.onStart()
        compactPlayerScroll.raisePlayer(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(
            selected: (NodeDomain, PlayerNodeDomain.Screen?) -> Unit,
            selectedNode: RemoteNodeDomain?,
            isSelectNodeOnly: Boolean
        ): RemotesDialogFragment {
            return RemotesDialogFragment(selected, selectedNode, isSelectNodeOnly)
        }

        @JvmStatic
        val fragmentModule = module {
            scope(named<RemotesDialogFragment>()) {
                scoped {
                    NodesDialogViewModel(
                        state = NodesDialogContract.State(),
                        remotesRepository = get(),
                        mapper = get(),
                        coroutines = get(),
                        playerInteractor = get(),
                        localRepository = get()
                    )
                }
            }
        }
    }
}
