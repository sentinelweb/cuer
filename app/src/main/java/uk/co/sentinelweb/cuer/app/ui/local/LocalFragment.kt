package uk.co.sentinelweb.cuer.app.ui.local

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Event.OnUpClicked
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class LocalFragment : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<LocalFragment>()
    private val controller: LocalController by inject()
    private val log: LogWrapper by inject()
    private val localMviView: LocalMviViewProxy by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("BrowseFragment view not bound")

    init {
        log.tag(this)
    }

    var onDismissListener: (() -> Unit)? = null

    // saves the data on back press (enabled in onResume)
    private val upCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            localMviView.dispatch(OnUpClicked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // todo make a factory to create the controller here move this to onViewCreated see playlistsMviFrag
        controller.onViewCreated(listOf(localMviView), lifecycle.asEssentyLifecycle())
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
            LocalComposables.RemotesUi(localMviView)
        }
        observeLabels()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, upCallback)
        linkScopeToActivity()
    }

    override fun onStart() {
        super.onStart()
        //OnboardingFragment.showIntro(this@LocalFragment, browseHelpConfig)
        compactPlayerScroll.raisePlayer(this)
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun observeLabels() {
        localMviView.labelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<Label> {
                override fun onChanged(label: Label) {
                    when (label) {
                        Up -> dismiss()
                        is Message -> snackbarWrapper.make(label.msg)
                        is Saved -> {
                            onDismissListener?.invoke()
                            dismiss()
                        }
                    }
                }
            })
    }

    companion object {
        fun newInstance(): LocalFragment {
            return LocalFragment()
        }

        @JvmStatic
        val fragmentModule = module {
            scope(named<LocalFragment>()) {
                scoped {
                    LocalController(
                        storeFactory = get(),
                        modelMapper = get(),
                        lifecycle = get<LocalFragment>().lifecycle.asEssentyLifecycle(),
                        log = get()
                    )
                }
                scoped {
                    LocalStoreFactory(
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(),
                        log = get(),
                        remoteServerManager = get(),
                        localRepository = get(),
                        connectivityWrapper = get(),
                        wifiStateProvider = get(),
                    )
                }
                scoped { LocalModelMapper(get(), get()) }
                scoped { LocalMviViewProxy(get(), get()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
            }
        }
    }

}