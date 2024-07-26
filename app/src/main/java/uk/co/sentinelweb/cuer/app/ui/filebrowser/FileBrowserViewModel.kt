package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FileBrowserContract.AppFilesUiModel
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModel.Companion.blankModel
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
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
    private val remoteDialogLauncher: RemotesDialogContract.Launcher,
) : FilesContract.Interactions, ViewModel() {

    override val modelObservable = MutableStateFlow(blankModel())
    val appModelObservable = MutableStateFlow(AppFilesUiModel(loading = false))

    sealed class Label {
        object Init : Label()
        object Up : Label()
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

    fun onBackClick() {
        state.currentFolder
            ?.takeIf { it.children.size > 0 && state.path != null }
            ?.also { onClickFolder(it.children[0]) }
            ?: run { labels.value = Label.Up }
    }

    fun onUpClick() {
        labels.value = Label.Up
    }

    override fun onClickFolder(folder: PlaylistDomain) {
        state.path = folder.platformId
        loadCurrentPath()
    }

    override fun onClickFile(file: PlaylistItemDomain) {
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
                // show remotes dialog and wait for selection
                // todo check if connected and just launch video directly - need scren ref in castController
                remoteDialogLauncher.launchRemotesDialog({ remoteNode, screen ->
                    launchRemotePlayer(remoteNode, screen)
                })
                appModelObservable.value = AppFilesUiModel(loading = false)
            }
        }
    }

    private fun launchRemotePlayer(remoteNode: RemoteNodeDomain, screen: PlayerNodeDomain.Screen) {
        viewModelScope.launch {
            appModelObservable.value = AppFilesUiModel(loading = true)
            playerInteractor.launchPlayerVideo(
                remoteNode.locator(),
                state.selectedFile ?: throw IllegalStateException(),
                screen.index
            )
            castController.connectCuerCast(state.node)
            remoteDialogLauncher.hideRemotesDialog()
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
