package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesComposeables.FileBrowserDesktopUi
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.FilesModel.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.State
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesViewModel
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.hub.ui.filebrowser.viewer.openFileInDefaultApp
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncher
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncherComposeables.ShowRemotesDialogIfNecessary
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository

class FilesUiCoordinator2(
    private val parent: HomeUiCoordinator,
    private val remotesRepository: RemotesRepository,
    private val log: LogWrapper
) : UiCoordinator<FilesContract.FilesModel>,
    DesktopScopeComponent,
    KoinComponent {

    init {
        log.tag(this)
    }

    override val scope: Scope = desktopScopeWithSource(this)

    val viewModel: FilesViewModel by scope.inject()

    override val modelObservable = MutableStateFlow(Initial)

    val remotesDialogLauncher: RemotesDialogContract.Launcher by scope.inject()

    override fun create() {
        viewModel.viewModelScope.launch {
            viewModel.init(null, null)
        }
    }

    fun init(node: NodeDomain) {
        viewModel.init(node, null)
    }

    override fun destroy() {

    }

    @Composable
    fun FileBrowserDesktopUi() {
            FileBrowserDesktopUi(this.viewModel)
            ShowRemotesDialogIfNecessary(this.remotesDialogLauncher as RemotesDialogLauncher)

    }

    private fun playMedia(item: PlaylistItemDomain) {
        parent.showPlayer(item, modelObservable.value.list?.playlist ?: error("No filelist present"))
    }

    private fun showFile(file: PlaylistItemDomain) {
//        showTextWindow(file)
        openFileInDefaultApp(file.media)
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { (parent: HomeUiCoordinator) -> FilesUiCoordinator2(parent, get(), get()) }
            scope(named<FilesUiCoordinator2>()) {
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
