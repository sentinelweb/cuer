package uk.co.sentinelweb.cuer.app.ui.filebrowser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class FileBrowserFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<FileBrowserFragment>()
    private val log: LogWrapper by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navigationProvider: NavigationProvider by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val interactions: FilesContract.Interactions by inject()
    //private val remotesHelpConfig: RemotesHelpConfig by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("BrowseFragment view not bound")

    private var dialogFragment: DialogFragment? = null

    init {
        log.tag(this)
    }

    // saves the data on back press (enabled in onResume)
    private val upCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //remotesMviView.dispatch(OnUpClicked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // todo make a factory to create the controller here move this to onViewCreated see playlistsMviFrag
        //controller.onViewCreated(listOf(remotesMviView), lifecycle.asEssentyLifecycle())
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
            FilesComposeables.FilesUi(interactions = interactions)
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
        //OnboardingFragment.showIntro(this@FileBrowserFragment, remotesHelpConfig)
        compactPlayerScroll.raisePlayer(this)
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
        //controller.onRefresh()
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

    }


    companion object {
//        private val CONFIG_FRAGMENT_TAG = "config_fragment_tag"
//
//        @JvmStatic
//        val fragmentModule = module {
//            scope(named<FileBrowserFragment>()) {
//                scoped {
//                    RemotesController(
//                        storeFactory = get(),
//                        modelMapper = get(),
//                        lifecycle = get<FileBrowserFragment>().lifecycle.asEssentyLifecycle(),
//                        log = get(),
//                        wifiStateProvider = get(),
//                        remotesRepository = get(),
//                        coroutines = get(),
//                        localRepository = get()
//                    )
//                }
//                scoped {
//                    RemotesStoreFactory(
////                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
//                        storeFactory = DefaultStoreFactory(),
//                        strings = get(),
//                        log = get(),
//                        prefs = get(),
//                        remoteServerManager = get(),
//                        coroutines = get(),
//                        localRepository = get(),
//                        remoteStatusInteractor = get(),
//                        remotesRepository = get(),
//                        locationPermissionLaunch = get(),
//                        wifiStateProvider = get(),
//                        getPlaylistsFromDeviceUseCase = get(),
//                        playlistsOrchestrator = get(),
//                    )
//                }
//                scoped { RemotesModelMapper(get(), get()) }
//                scoped { RemotesMviViewProxy(get(), get()) }
//                scoped { navigationRouter(true, this.getFragmentActivity()) }
//                scoped { RemotesHelpConfig(get()) }
//                scoped<LocationPermissionLaunch> { LocationPermissionOpener(this.getFragmentActivity(), get()) }
//            }
//        }
    }
}
