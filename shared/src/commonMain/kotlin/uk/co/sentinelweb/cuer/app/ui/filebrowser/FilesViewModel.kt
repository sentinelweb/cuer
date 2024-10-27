package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.FilesModel.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Label
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.net.remote.RemoteFilesInteractor
import uk.co.sentinelweb.cuer.net.remote.RemotePlayerInteractor
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.locator

class FilesViewModel(
    private val state: FilesContract.State,
    private val filesInteractor: RemoteFilesInteractor,
    private val remotesRepository: RemotesRepository,
    private val mapper: FilesModelMapper,
    private val playerInteractor: RemotePlayerInteractor,
    private val log: LogWrapper,
    private val castController: CastController,
    private val remoteDialogLauncher: RemotesDialogContract.Launcher,
    private val cuerCastPlayerWatcher: CuerCastPlayerWatcher,
) : ViewModel(), FilesContract.ViewModel {

    override val modelObservable = MutableStateFlow(Initial)

    val _labels = MutableStateFlow<Label>(Label.Init)
    val labels: Flow<Label>
        get() = _labels

    init {
        log.tag(this)
    }

    override fun init(id: GUID, path: String?) {
        state.sourceRemoteId = id
        state.sourceNode = remotesRepository.getById(id)
        state.path = path
        loadCurrentPath()
    }

    override fun onBackClick() {
        state.currentFolder
            ?.takeIf { it.children.size > 0 && state.path != null }
            ?.also { onClickFolder(it.children[0]) }
            ?: run { _labels.value = Label.Up }
    }

    override fun onUpClick() {
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
            map(true)
            state.sourceNode?.apply {
                // fixme when this is possible on both platforms - need a broader check
                if (cuerCastPlayerWatcher.isWatching()) {
                    launchRemotePlayer(
                        cuerCastPlayerWatcher.remoteNode ?: throw IllegalStateException("No remote"),
                        cuerCastPlayerWatcher.screen ?: throw IllegalStateException("No remote screen")
                    )
                } else {
                    remoteDialogLauncher.launchRemotesDialog({ targetNode, screen ->
                        launchRemotePlayer(targetNode, screen ?: throw IllegalStateException("No screen selected"))
                    })
                }
            }
            map(false)
        }
    }

    override fun onRefreshClick() {
        loadCurrentPath()
    }

    private fun launchRemotePlayer(targetNode: RemoteNodeDomain, screen: PlayerNodeDomain.Screen) {
        viewModelScope.launch {
            map(true)
            playerInteractor.launchPlayerVideo(
                targetNode.locator(),
                state.selectedFile ?: throw IllegalStateException(),
                screen.index
            )
            // todo check if already connected to remote node
            // todo also check if the dialog has connected
            if (!castController.isConnected()) {
                // assumes here that sourecnode == targetnode
                castController.connectCuerCast(targetNode, screen)
            }
            remoteDialogLauncher.hideRemotesDialog()
            map(false)
        }
    }

    private fun showFile(file: PlaylistItemDomain) {

    }

    private fun loadCurrentPath() {
        viewModelScope.launch {
            map(true)
            state.sourceNode?.apply {
                state.currentFolder = filesInteractor.getFolderList(this.locator(), state.path).data
            }
            map(false)
        }
    }

    private fun map(loading: Boolean) {
        modelObservable.value = mapper.map(state = state, loading = loading)
    }
}
