package uk.co.sentinelweb.cuer.app.ui.filebrowser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.ktx.bindFlow
import uk.co.sentinelweb.cuer.app.ui.common.navigation.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.BACK_PARAMS
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.NAV_BACK
import uk.co.sentinelweb.cuer.app.ui.exoplayer.ExoPlayerActivity
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Label
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.StatusBarColorWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.toGUID
import uk.co.sentinelweb.cuer.remote.interact.PlayerLaunchHost

class FileBrowserFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<FileBrowserFragment>()
    private val viewModel: FilesViewModel by inject()
    private val log: LogWrapper by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val navRouter: NavigationRouter by inject()
    private val navigationProvider: NavigationProvider by inject()
    private val statusBarColor: StatusBarColorWrapper by inject()
    //private val remotesHelpConfig: RemotesHelpConfig by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("BrowseFragment view not bound")

    //private var dialogFragment: DialogFragment? = null

    private val remoteIdArg: GUID? by lazy {
        NavigationModel.Param.REMOTE_ID.getString(arguments)?.toGUID()
    }

    private val filePathArg: String? by lazy {
        NavigationModel.Param.FILE_PATH.getString(arguments)
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
            FilesComposeables.FileBrowserAppUi(
                viewModel = viewModel
            )
        }
        statusBarColor.setStatusBarColorResource(R.color.black)
        bindFlow(viewModel.labels, ::observeLabels)
        remoteIdArg?.apply { viewModel.init(this, filePathArg) }
    }

    private fun observeLabels(label: Label) {
        when (label) {
            Label.Init -> {}
            Label.Up -> {
                navRouter.navigate(NavigationModel(NAV_BACK, mapOf(BACK_PARAMS to R.id.navigation_remotes)))
            }
            Label.Settings -> navigationProvider.navigate(R.id.navigation_settings_root)
            else -> Unit
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
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
        // controller.onRefresh()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<FileBrowserFragment>()) {
                viewModel {
                    FilesViewModel(
                        state = FilesContract.State(),
                        filesInteractor = get(),
                        remotesRepository = get(),
                        mapper = get(),
                        playerInteractor = get(),
                        log = get(),
                        castController = get(),
                        remoteDialogLauncher = get(),
                        cuerCastPlayerWatcher = get(),
                        getFolderListUseCase = get(),
                        localRepository = get(),
                        localPlayerLaunchHost = get()
                    )
                }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped<PlayerLaunchHost> {
                    object : PlayerLaunchHost {
                        override fun launchVideo(item: PlaylistItemDomain, screenIndex: Int?) {
                            val activity = get<FileBrowserFragment>().activity
                            Intent(activity,  ExoPlayerActivity::class.java)
                                .let { activity!!.startActivity(it) }
                        }
                    }
                }
            }
        }

    }
}
