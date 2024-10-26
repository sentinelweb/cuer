package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModel
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesViewModel
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
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository

class FilesUiCoordinator2(
    private val parent: HomeUiCoordinator,
    private val remotesRepository: RemotesRepository,
    private val log: LogWrapper
) : UiCoordinator<FilesModel>,
    DesktopScopeComponent,
//    FilesContract.Interactions,
    KoinComponent {

    init {
        log.tag(this)
    }

    override val scope: Scope = desktopScopeWithSource(this)

    val viewModel: FilesViewModel by scope.inject()

    //override lateinit var modelObservable: MutableStateFlow<FilesModel>
    override val modelObservable = MutableStateFlow(FilesModel.blankModel())

    override fun create() {
        viewModel.viewModelScope.launch {
            viewModel.init(remotesRepository.getByName("airy")?.id?.id!!, null)
        }
        //modelObservable = viewModel.modelObservable
        //refresh()
    }

//    fun refresh() {
//        getFolders.getFolderList(currentFolder)
//            ?.let { modelObservable.value = mapper.map(it) }
//    }

    override fun destroy() {

    }

//    override fun onClickFolder(folder: PlaylistDomain) {
//        currentFolder = folder.platformId
//        log.tag("currentFolder; $currentFolder")
//        refresh()
//    }
//
//    override fun onClickFile(file: PlaylistItemDomain) {
//        if (listOf(VIDEO, AUDIO).contains(file.media.mediaType))
//            playMedia(file)
//        else if (FILE.equals(file.media.mediaType))
//            showFile(file)
//
//    }

    private fun playMedia(item: PlaylistItemDomain) {
        parent.showPlayer(item, modelObservable.value.list.playlist)
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

            }
            factory {
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
