package uk.co.sentinelweb.cuer.app.ui.remotes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.local.LocalFragment
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event.OnUpClicked
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class RemotesFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<RemotesFragment>()
    private val controller: RemotesController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val remotesMviView: RemotesMviViewProxy by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navigationProvider: NavigationProvider by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val browseHelpConfig: RemotesHelpConfig by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("BrowseFragment view not bound")

    private var dialogFragment: DialogFragment? = null

    init {
        log.tag(this)
    }

    // saves the data on back press (enabled in onResume)
    private val upCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            remotesMviView.dispatch(OnUpClicked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // todo make a factory to create the controller here move this to onViewCreated see playlistsMviFrag
        controller.onViewCreated(listOf(remotesMviView), lifecycle.asEssentyLifecycle())
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
            RemotesComposables.RemotesUi(remotesMviView)
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
        OnboardingFragment.showIntro(this@RemotesFragment, browseHelpConfig)
        compactPlayerScroll.raisePlayer(this)
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
    }

    override fun onStop() {
        super.onStop()
        //playerView.showPlayer()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun observeLabels() {
        remotesMviView.labelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<RemotesContract.MviStore.Label> {
                override fun onChanged(label: RemotesContract.MviStore.Label) {
                    when (label) {
                        ActionSettings -> navigationProvider.navigate(R.id.navigation_settings_root)
                        ActionSearch -> {
                            SearchBottomSheetFragment()
                                .show(childFragmentManager, SearchBottomSheetFragment.SEARCH_BOTTOMSHEET_TAG)
                        }

                        ActionPasteAdd -> (requireActivity() as? MainActivity)?.checkIntentAndPasteAdd()
                        ActionHelp -> OnboardingFragment.showHelp(this@RemotesFragment, browseHelpConfig)
                        Up -> log.d("Up")
                        is Message -> snackbarWrapper.make(label.msg)
                        ActionConfig -> showConfigFragment()
                    }
                }
            })
    }

    private fun showConfigFragment() {
        dialogFragment = LocalFragment.newInstance()
        (dialogFragment as LocalFragment).onDismissListener = { remotesMviView.dispatch(Event.OnRefresh) }
        dialogFragment?.show(childFragmentManager, CONFIG_FRAGMENT_TAG)
    }

    companion object {
        private val CONFIG_FRAGMENT_TAG = "config_fragment_tag"

        @JvmStatic
        val fragmentModule = module {
            scope(named<RemotesFragment>()) {
                scoped {
                    RemotesController(
                        storeFactory = get(),
                        modelMapper = get(),
                        lifecycle = get<RemotesFragment>().lifecycle.asEssentyLifecycle(),
                        log = get()
                    )
                }
                scoped {
                    RemotesStoreFactory(
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(),
                        strings = get(),
                        log = get(),
                        prefs = get(),
                        remoteServerManager = get(),
                        coroutines = get(),
                        localRepository = get()
                    )
                }
                scoped { RemotesModelMapper(get(), get()) }
                scoped { RemotesMviViewProxy(get(), get()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped { RemotesHelpConfig(get()) }
            }
        }
    }
}
