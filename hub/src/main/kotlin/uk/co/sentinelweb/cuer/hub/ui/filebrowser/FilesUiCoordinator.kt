package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.hub.ui.filebrowser.FilesModel.Companion.blankModel
import uk.co.sentinelweb.cuer.hub.ui.filebrowser.viewer.openFileInDefaultApp
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class FilesUiCoordinator(
    private val parent: HomeUiCoordinator,
    private val getFolders: GetFolderListUseCase
) : UiCoordinator<FilesModel>,
    DesktopScopeComponent,
    KoinComponent {
    override val scope: Scope = desktopScopeWithSource(this)

    private val mapper: FilesModelMapper by scope.inject()
    override val modelObservable = MutableStateFlow<FilesModel>(blankModel())

    private var currentFolder: String? = null

    override fun create() {
        refresh()
    }

    fun refresh() {
        getFolders.getFolderList(currentFolder)
            ?.let { modelObservable.value = mapper.map(it) }
    }

    override fun destroy() {

    }

    fun loadFolder(folder: PlaylistDomain) {
        currentFolder = folder.platformId
        refresh()
    }

    fun playVideo(item: PlaylistItemDomain) {
        parent.showPlayer(item)
    }

    fun showFile(file: MediaDomain) {
//        showTextWindow(file)
        openFileInDefaultApp(file)
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { (parent: HomeUiCoordinator) -> FilesUiCoordinator(parent, get()) }
            scope(named<FilesUiCoordinator>()) {
                scoped { FilesModelMapper() }
            }
        }
    }
}
