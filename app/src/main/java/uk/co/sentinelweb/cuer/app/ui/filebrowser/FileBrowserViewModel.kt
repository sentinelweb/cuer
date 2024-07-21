package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FileBrowserContract.AppFilesUiModel
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModel.Companion.blankModel
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.net.remote.RemoteFilesInteractor
import uk.co.sentinelweb.cuer.net.remote.RemotePlayerInteractor
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.locator

class FileBrowserViewModel(
    private val state: FileBrowserContract.State,
    private val filesInteractor: RemoteFilesInteractor,
    private val remotesRepository: RemotesRepository,
    private val mapper: FilesModelMapper,
    private val playerInteractor: RemotePlayerInteractor,
    private val log: LogWrapper,
    private val castController: CastController,
) : FilesContract.Interactions, ViewModel() {

    override val modelObservable = MutableStateFlow(blankModel())
    val appModelObservable = MutableStateFlow(AppFilesUiModel(loading = false))

    sealed class Label {
        object Init : Label()
    }

    val labels = MutableStateFlow<Label>(Label.Init)

    init {
        log.tag(this)
    }

    fun init(id: GUID) {
        state.remoteId = id
        state.node = remotesRepository.getById(id)
        loadCurrentPath()
    }

    override fun clickFolder(folder: PlaylistDomain) {
        state.path = folder.platformId
        loadCurrentPath()
    }

    override fun clickFile(file: PlaylistItemDomain) {
        if (listOf(VIDEO, AUDIO).contains(file.media.mediaType))
            playMedia(file)
        else if (FILE.equals(file.media.mediaType))
            showFile(file)
    }

    private fun playMedia(file: PlaylistItemDomain) {
        state.selectedFile = file
        viewModelScope.launch {
            appModelObservable.value = AppFilesUiModel(loading = true)
            state.node?.apply {
                state.remotePlayerConfig = playerInteractor.getPlayerConfig(this.locator()).data
                state.remotePlayerConfig?.apply {
                    // modelObservable.value = mapper.map(this)
                    log.d("remotePlayerConfig: $this")
                    if (this.screens.size == 1) {
                        launchRemotePlayer(this.screens[0])
                    } else {
                        // fixme implement this later
                        //selectScreenDialog()
                        launchRemotePlayer(this.screens[this.screens.size - 1])
                    }
                }
                appModelObservable.value = AppFilesUiModel(loading = false)
            }
        }
    }

    private fun selectScreenDialog() {
        // show screen selection dialog
    }

    private fun launchRemotePlayer(screen: PlayerNodeDomain.Screen) {
        viewModelScope.launch {
            appModelObservable.value = AppFilesUiModel(loading = true)
            playerInteractor.launchPlayerVideo(
                state.node?.locator()!!,
                state.selectedFile!!,
                screen.index
            )
            castController.connectCuerCast(state.node)
            appModelObservable.value = AppFilesUiModel(loading = false)
        }
    }

    private fun showFile(file: PlaylistItemDomain) {

    }

    private fun loadCurrentPath() {
        viewModelScope.launch {
            appModelObservable.value = AppFilesUiModel(loading = true)
            state.node?.apply {
                state.currentFolder = filesInteractor.getFolderList(this.locator(), state.path).data
                state.currentFolder?.apply {
                    modelObservable.value = mapper.map(this)
                }
                appModelObservable.value = AppFilesUiModel(loading = false)
            }
        }
    }
}