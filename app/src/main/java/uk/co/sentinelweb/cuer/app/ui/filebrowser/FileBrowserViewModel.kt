package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FileBrowserContract.AppFilesUiModel
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FileBrowserContract.Label
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModel.Companion.blankModel
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
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
    private val cuerCastPlayerWatcher: CuerCastPlayerWatcher,
    private val appModelMapper: FileBrowserAppModelMapper,
) : FilesContract.Interactions, ViewModel() {

    override val modelObservable = MutableStateFlow(blankModel())
    val appModelObservable = MutableStateFlow(AppFilesUiModel.BLANK)

    val _labels = MutableStateFlow<Label>(Label.Init)
    val labels: Flow<Label>
        get() = _labels

    init {
        log.tag(this)
    }

    fun init(id: GUID, path: String?) {
        state.sourceRemoteId = id
        state.sourceNode = remotesRepository.getById(id)
        state.path = path
        loadCurrentPath()
    }

    fun onBackClick() {
        state.currentFolder
            ?.takeIf { it.children.size > 0 && state.path != null }
            ?.also { onClickFolder(it.children[0]) }
            ?: run { _labels.value = Label.Up }
    }

    fun onUpClick() {
        _labels.value = Label.Up
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
            appModelObservable.value = appModelMapper.map(state = state, loading = true)
            state.sourceNode?.apply {
                // fixme when this is possible on both platforms - need a broader check
                if (cuerCastPlayerWatcher.isWatching()) {
                    launchRemotePlayer(
                        cuerCastPlayerWatcher.remoteNode ?: throw IllegalStateException("No remote"),
                        cuerCastPlayerWatcher.screen ?: throw IllegalStateException("No remote screen")
                    )
                } else {
                    remoteDialogLauncher.launchRemotesDialog({ remoteNode, screen ->
                        launchRemotePlayer(remoteNode, screen)
                    })
                }
                appModelObservable.value = appModelMapper.map(state = state, loading = false)
            }
        }
    }

    private fun launchRemotePlayer(remoteNode: RemoteNodeDomain, screen: PlayerNodeDomain.Screen) {
        viewModelScope.launch {
            appModelObservable.value = appModelMapper.map(state = state, loading = true)
            playerInteractor.launchPlayerVideo(
                remoteNode.locator(),
                state.selectedFile ?: throw IllegalStateException(),
                screen.index
            )
            // todo check if already connected to remote node
            // todo also check if the dialog has connected
            if (!castController.isConnected()) {
                // assumes here that sourecnode == targetnode
                castController.connectCuerCast(state.sourceNode, screen)
            }
            remoteDialogLauncher.hideRemotesDialog()
            appModelObservable.value = appModelMapper.map(state = state, loading = false)
        }
    }

    private fun showFile(file: PlaylistItemDomain) {

    }

    private fun loadCurrentPath() {
        viewModelScope.launch {
            appModelObservable.value = appModelMapper.map(state = state, loading = true)
            state.sourceNode?.apply {
                state.currentFolder = filesInteractor.getFolderList(this.locator(), state.path).data
                state.currentFolder?.apply {
                    modelObservable.value = mapper.map(this)
                }
                appModelObservable.value = appModelMapper.map(state = state, loading = false)
            }
        }
    }
}
