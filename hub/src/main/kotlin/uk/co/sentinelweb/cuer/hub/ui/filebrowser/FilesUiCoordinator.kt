package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.FilesModel.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModelMapper
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.domain.NodeDomain
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
) : UiCoordinator<FilesContract.FilesModel>,
    DesktopScopeComponent,
    FilesContract.ViewModel,
    KoinComponent {

    init {
        log.tag(this)
    }

    override val scope: Scope = desktopScopeWithSource(this)

    private val mapper: FilesModelMapper by scope.inject()
    override val modelObservable = MutableStateFlow(Initial)

    private var currentFolder: String? = null // todo make state object
    private val state: FilesContract.State = FilesContract.State()
    override fun create() {
        refresh()
    }

    fun refresh() {
        getFolders.getFolderList(currentFolder)
            ?.let { modelObservable.value = mapper.map(state, false) }
    }

    override fun destroy() {

    }

    override fun onClickFolder(folder: PlaylistDomain) {
        currentFolder = folder.platformId
        log.tag("currentFolder; $currentFolder")
        refresh()
    }

    override fun onClickFile(file: PlaylistItemDomain) {
        if (listOf(VIDEO, AUDIO).contains(file.media.mediaType))
            playMedia(file)
        else if (FILE.equals(file.media.mediaType))
            showFile(file)

    }

    override fun onUpClick() {
        TODO("Not yet implemented")
    }

    override fun onBackClick() {
        TODO("Not yet implemented")
    }

    override fun init(remoteId: GUID, path: String?) {
        TODO("Not yet implemented")
    }

    override fun init(node: NodeDomain?, path: String?) {
        TODO("Not yet implemented")
    }

    private fun playMedia(item: PlaylistItemDomain) {
        parent.showPlayer(item, modelObservable.value.list?.playlist?:error("No playlist"))
    }

    override fun onRefreshClick() {
        TODO("Not yet implemented")
    }

    private fun showFile(file: PlaylistItemDomain) {
//        showTextWindow(file)
        openFileInDefaultApp(file.media)
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { (parent: HomeUiCoordinator) -> FilesUiCoordinator(parent, get(), get()) }
            scope(named<FilesUiCoordinator>()) {}
        }
    }
}
