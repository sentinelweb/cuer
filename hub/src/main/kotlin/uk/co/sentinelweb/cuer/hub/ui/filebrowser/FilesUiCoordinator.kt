package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModel
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModel.Companion.blankModel
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModelMapper
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.hub.ui.filebrowser.viewer.openFileInDefaultApp
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class FilesUiCoordinator(
    private val parent: HomeUiCoordinator,
    private val getFolders: GetFolderListUseCase,
    private val log: LogWrapper
) : UiCoordinator<FilesModel>,
    DesktopScopeComponent,
    FilesContract.Interactions,
    KoinComponent {

    init {
        log.tag(this)
    }
    override val scope: Scope = desktopScopeWithSource(this)

    private val mapper: FilesModelMapper by scope.inject()
    override val modelObservable = MutableStateFlow(blankModel())

    private var currentFolder: String? = null // todo make state object

    override fun create() {
        refresh()
    }

    fun refresh() {
        getFolders.getFolderList(currentFolder)
            ?.let { modelObservable.value = mapper.map(it) }
    }

    override fun destroy() {

    }

    override fun clickFolder(folder: PlaylistDomain) {
        currentFolder = folder.platformId
        log.tag("currentFolder; $currentFolder")
        refresh()
    }

    override fun clickFile(file: PlaylistItemDomain) {
        if (listOf(VIDEO, AUDIO).contains(file.media.mediaType))
            playVideo(file)
        else if (FILE.equals(file.media.mediaType)) {
            showFile(file)
        }
    }

    private fun playVideo(item: PlaylistItemDomain) {
        parent.showPlayer(item, modelObservable.value.list.playlist)
    }

    private fun showFile(file: PlaylistItemDomain) {
//        showTextWindow(file)
        openFileInDefaultApp(file.media)
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { (parent: HomeUiCoordinator) -> FilesUiCoordinator(parent, get(), get()) }
            scope(named<FilesUiCoordinator>()) {
                scoped { FilesModelMapper() }
            }
        }
    }
}
