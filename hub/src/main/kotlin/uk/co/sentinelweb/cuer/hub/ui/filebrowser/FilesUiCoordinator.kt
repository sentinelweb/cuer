package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import androidx.compose.runtime.Composable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesComposeables.FileBrowserDesktopUi
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Label.ErrorMessage
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Model.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.State
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesViewModel
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncher
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncherComposeables.ShowRemotesDialogIfNecessary
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class FilesUiCoordinator(
    private val parent: HomeUiCoordinator,
    private val log: LogWrapper
) : UiCoordinator<FilesContract.Model>,
    DesktopScopeComponent,
    KoinComponent {// fixme check KoinComponent

    init {
        log.tag(this)
    }

    private val supervisorJob = SupervisorJob()
    private val coordinatorScope = CoroutineScope(Dispatchers.Main + supervisorJob)

    override val scope: Scope = desktopScopeWithSource(this)
    override val modelObservable = MutableStateFlow(Initial)

    private val viewModel: FilesViewModel by scope.inject()
    private val remotesDialogLauncher: RemotesDialogContract.Launcher by scope.inject()

    override fun create() {
        bindLabels()
        viewModel.init(null, null)
    }

    private fun bindLabels() {
        coordinatorScope.launch {
            viewModel.labels.collectLatest {
                when (it) {
                    is ErrorMessage -> parent.showError(it.message)
                    else -> Unit
                }
            }
        }
    }

    fun openNode(node: NodeDomain?) {
        viewModel.init(node, null)
    }

    override fun destroy() {

    }

    @Composable
    fun FileBrowserDesktopUi() {
        FileBrowserDesktopUi(this.viewModel)
        ShowRemotesDialogIfNecessary(this.remotesDialogLauncher as RemotesDialogLauncher)
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { (parent: HomeUiCoordinator) -> FilesUiCoordinator(parent, get()) }
            scope(named<FilesUiCoordinator>()) {
                scoped {
                    FilesViewModel(
                        state = State(),
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
                        localPlayerLaunchHost = get(),
                        localPlayerStatus = get(),
                    )
                }
                scoped {
                    CastController(
                        cuerCastPlayerWatcher = get(),
                        chromeCastHolder = get(),
                        chromeCastDialogWrapper = get(),
                        chromeCastWrapper = get(),
                        floatingManager = get(),
                        playerControls = get(),
                        castDialogLauncher = get(),
                        ytServiceManager = get(),
                        coroutines = get(),
                        log = get(),
                    )
                }
            }
        }
    }
}
