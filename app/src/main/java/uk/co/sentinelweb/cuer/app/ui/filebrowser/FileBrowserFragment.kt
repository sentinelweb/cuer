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
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.ktx.bindFlow
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.BACK_PARAMS
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.NAV_BACK
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.navigation.getString
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FileBrowserViewModel.Label
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.toGUID

class FileBrowserFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<FileBrowserFragment>()
    private val viewModel: FileBrowserViewModel by inject()
    private val log: LogWrapper by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val navRouter: NavigationRouter by inject()
    //private val remotesHelpConfig: RemotesHelpConfig by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("BrowseFragment view not bound")

    private var dialogFragment: DialogFragment? = null

    private val remoteIdArg: GUID? by lazy {
        NavigationModel.Param.REMOTE_ID.getString(arguments)?.toGUID()
    }

    init {
        log.tag(this)
    }

    // saves the data on back press (enabled in onResume)
    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //remotesMviView.dispatch(OnUpClicked)
            viewModel.onBackClick()
        }
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
            FileBrowserAppComposeables.FileBrowserAppWrapperUi(
                appModelObservable = viewModel.appModelObservable,
                viewModel = viewModel
            )
        }
        bindFlow(viewModel.labels, ::observeLabels)
    }

    private fun observeLabels(label: Label) {
        when (label) {
            Label.Init -> {}
            Label.Up -> navRouter.navigate(NavigationModel(NAV_BACK, mapOf(BACK_PARAMS to R.id.navigation_remotes)))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
        linkScopeToActivity()
    }

    override fun onStart() {
        super.onStart()
        //OnboardingFragment.showIntro(this@FileBrowserFragment, remotesHelpConfig)
        compactPlayerScroll.raisePlayer(this)
        remoteIdArg?.apply { viewModel.init(this) }
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
        // controller.onRefresh()
    }

    override fun onStop() {
        super.onStop()
        // playerView.showPlayer()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<FileBrowserFragment>()) {
                scoped {
                    FileBrowserViewModel(
                        state = FileBrowserContract.State(),
                        filesInteractor = get(),
                        remotesRepository = get(),
                        mapper = get(),
                        playerInteractor = get(),
                        log = get(),
                        castController = get(),
                        remoteDialogLauncher = get(),
                        cuerCastPlayerWatcher = get(),
                    )
                }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
            }
        }
    }
}
