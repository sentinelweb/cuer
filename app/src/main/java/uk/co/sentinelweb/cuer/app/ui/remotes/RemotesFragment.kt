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
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.REMOTE_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.FOLDER_LIST
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.local.LocalFragment
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event.OnUpClicked
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.NodesDialogContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.permission.LocationPermissionLaunch
import uk.co.sentinelweb.cuer.app.util.permission.LocationPermissionOpener
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.StatusBarColorWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.name

class RemotesFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<RemotesFragment>()
    private val controller: RemotesController by inject()
    private val log: LogWrapper by inject()
    private val remotesMviView: RemotesMviViewProxy by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navigationProvider: NavigationProvider by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val remotesHelpConfig: RemotesHelpConfig by inject()
    private val remotesDialogLauncher: NodesDialogContract.Launcher by inject()
    private val cuerCastPlayerWatcher: CuerCastPlayerWatcher by inject()
    private val statusBarColor: StatusBarColorWrapper by inject()

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
            RemotesComposables.RemotesAppUi(remotesMviView)
        }
        statusBarColor.setStatusBarColorResource(R.color.blue_grey_900)
        observeLabels()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, upCallback)
        linkScopeToActivity()
    }

    override fun onStart() {
        super.onStart()
        OnboardingFragment.showIntro(this@RemotesFragment, remotesHelpConfig)
        compactPlayerScroll.raisePlayer(this)
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
        controller.onRefresh()
    }

    override fun onStop() {
        super.onStop()
        //playerView.showPlayer()
    }

    override fun onDestroyView() {
        _binding = null
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment = null
        super.onDestroyView()
    }

    private fun observeLabels() {
        remotesMviView.labelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<RemotesContract.MviStore.Label> {
                override fun onChanged(value: RemotesContract.MviStore.Label) {
                    when (value) {
                        ActionSettings -> navigationProvider.navigate(R.id.navigation_settings_root)
                        ActionSearch -> {
                            SearchBottomSheetFragment()
                                .show(childFragmentManager, SearchBottomSheetFragment.SEARCH_BOTTOMSHEET_TAG)
                        }

                        ActionPasteAdd -> (requireActivity() as? MainActivity)?.checkIntentAndPasteAdd()
                        ActionHelp -> OnboardingFragment.showHelp(this@RemotesFragment, remotesHelpConfig)
                        Up -> log.d("Up")
                        is Message -> snackbarWrapper.make(value.msg)
                        ActionConfig -> showConfigFragment()
                        is ActionFolders -> navigationProvider.navigate(
                            NavigationModel(
                                FOLDER_LIST,
                                mapOf(REMOTE_ID to (value.node.id?.id?.value ?: error("No Id")))
                            )
                        )

                        is CuerConnected ->
                            snackbarWrapper.make(
                                getString(
                                    R.string.remotes_cuer_connected_screen,
                                    value.remote.name(),
                                    value.screen?.index ?: 0
                                )
                            ).show()
                        is Error ->
                            snackbarWrapper.make(value.message).show()
                        is CuerSelectScreen ->
                            remotesDialogLauncher.launchRemotesDialog(
                                { nodeDomain, screen ->
                                    if (nodeDomain is RemoteNodeDomain) {
                                        remotesMviView.dispatch(
                                            Event.OnActionCuerConnectScreen(
                                                nodeDomain,
                                                screen
                                            )
                                        )
                                    }
                                    remotesDialogLauncher.hideRemotesDialog()
                                },
                                value.node
                            )

                        is CuerSelectSendTo ->
                            remotesDialogLauncher.launchRemotesDialog(
                                { nodeDomain, screen ->
                                    if (nodeDomain is RemoteNodeDomain) {
                                        remotesMviView.dispatch(
                                            Event.OnActionSendToSelected(
                                                value.sendNode,
                                                nodeDomain
                                            )
                                        )
                                    }
                                    remotesDialogLauncher.hideRemotesDialog()
                                },
                                null
                            )

                        None -> Unit
                    }
                }
            })
    }

    private fun showConfigFragment() {
        dialogFragment?.dismissAllowingStateLoss()
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
                        log = get(),
                        wifiStateProvider = get(),
                        remotesRepository = get(),
                        coroutines = get(),
                        localRepository = get()
                    )
                }
                scoped {
                    RemotesStoreFactory(
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(),
                        log = get(),
                        remoteServerManager = get(),
                        coroutines = get(),
                        localRepository = get(),
                        remoteStatusInteractor = get(),
                        remotesRepository = get(),
                        locationPermissionLaunch = get(),
                        wifiStateProvider = get(),
                        getPlaylistsFromDeviceUseCase = get(),
                        castController = get(),
                    )
                }
                scoped { RemotesMviViewProxy(get(), get()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped { RemotesHelpConfig(get()) }
                scoped<LocationPermissionLaunch> { LocationPermissionOpener(this.getFragmentActivity(), get()) }
            }
        }
    }
}
