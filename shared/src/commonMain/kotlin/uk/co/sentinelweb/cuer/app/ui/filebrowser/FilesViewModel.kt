package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Label
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Label.ErrorMessage
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Label.None
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Model
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Model.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Alpha
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Time
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.NodesDialogContract
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase.Companion.PARENT_FOLDER_TEXT
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.remote.RemoteFilesInteractor
import uk.co.sentinelweb.cuer.net.remote.RemotePlayerInteractor
import uk.co.sentinelweb.cuer.remote.interact.PlayerLaunchHost
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.locator
import uk.co.sentinelweb.cuer.shared.generated.resources.Res
import uk.co.sentinelweb.cuer.shared.generated.resources.files_error_empty
import uk.co.sentinelweb.cuer.shared.generated.resources.files_error_loading_folder

class FilesViewModel(
    private val state: FilesContract.State,
    private val filesInteractor: RemoteFilesInteractor,
    private val remotesRepository: RemotesRepository,
    private val mapper: FilesModelMapper,
    private val playerInteractor: RemotePlayerInteractor,
    private val log: LogWrapper,
    private val castController: CastController,
    private val remoteDialogLauncher: NodesDialogContract.Launcher,
    private val cuerCastPlayerWatcher: CuerCastPlayerWatcher,
    private val getFolderListUseCase: GetFolderListUseCase,
    private val localRepository: LocalRepository,
    private val localPlayerLaunchHost: PlayerLaunchHost,
    private val localPlayerStatus: PlayerContract.LocalStatus,
) : ViewModel(), FilesContract.ViewModel {

    private val _modelObservable = MutableStateFlow(Initial)
    override val modelObservable: Flow<Model> = _modelObservable

    private val _labels = MutableStateFlow<Label>(None)
    override val labels: Flow<Label> = _labels

    init {
        log.tag(this)
    }

    // fixme: used by app could make remoteId=null for local
    override fun init(remoteId: GUID, path: String?) {
        resetState()
        state.sourceRemoteId = remoteId
        state.sourceNode = remotesRepository.getById(remoteId)
        state.path = path
        loadCurrentPath()
    }

    override fun init(node: NodeDomain?, path: String?) {
        resetState()
        state.sourceNode = node ?: localRepository.localNode
        state.path = path
        loadCurrentPath()
    }

    override fun onBackClick() {
        state.upListItem
            ?.also { onClickFolder(it.second) }
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
        if (listOf(VIDEO, AUDIO).contains(file.media.mediaType)) {
            playMedia(file)
        } else if (FILE.equals(file.media.mediaType)) {
            showFile(file)
        }
    }

    override fun onRefreshClick() {
        loadCurrentPath()
    }

    override fun onSort(type: FilesContract.Sort) {
        when (type) {
            Alpha -> {
                state.sortAcending = if (state.sort == Alpha) !state.sortAcending else true
                state.sort = Alpha
            }

            Time -> {
                state.sortAcending = if (state.sort == Time) !state.sortAcending else false
                state.sort = Time
            }
        }
        map(false)
    }

    override fun onSettings() {
        viewModelScope.launch {
            _labels.value = Label.Settings
            delay(1)
            _labels.value = None
        }
    }

    private fun playMedia(file: PlaylistItemDomain) {
        state.selectedFile = file
        viewModelScope.launch {
            map(true)
            state.sourceNode?.apply {
                if (localPlayerStatus.isPlayerActive()) {
                    launchLocalPlayer(null, null, file)
                } else if (cuerCastPlayerWatcher.isWatching()) {
                    launchRemotePlayer(
                        cuerCastPlayerWatcher.remoteNode ?: throw IllegalStateException("No remote"),
                        cuerCastPlayerWatcher.screen ?: throw IllegalStateException("No remote screen")
                    )
                } else {
                    remoteDialogLauncher.launchRemotesDialog({ targetNode, screen ->
                        when (targetNode) {
                            is RemoteNodeDomain ->
                                launchRemotePlayer(
                                    targetNode,
                                    screen ?: throw IllegalStateException("No screen selected")
                                )

                            is LocalNodeDomain -> launchLocalPlayer(targetNode, screen, file)
                        }
                    })
                }
            }
            map(false)
        }
    }

    private fun showFile(file: PlaylistItemDomain) = Unit // stub

    private fun launchLocalPlayer(
        targetNode: LocalNodeDomain?,
        screen: PlayerNodeDomain.Screen?,
        file: PlaylistItemDomain
    ) {
        localPlayerLaunchHost.launchPlayerVideo(file, screen?.index)
        remoteDialogLauncher.hideRemotesDialog()
    }

    private fun launchRemotePlayer(
        targetNode: RemoteNodeDomain, screen: PlayerNodeDomain.Screen
    ) {
        viewModelScope.launch {
            map(true)
            playerInteractor.launchPlayerVideo(
                targetNode.locator(),
                state.selectedFile ?: throw IllegalStateException(),
                screen.index
            )
            if (!castController.isConnected()) {
                castController.connectCuerCast(targetNode, screen)
            }
            remoteDialogLauncher.hideRemotesDialog()
            map(false)
        }
    }

    private fun loadCurrentPath() = viewModelScope.launch {
        map(true)
        state.sourceNode?.apply {
            val folder = when (this) {
                is RemoteNodeDomain ->
                    filesInteractor.getFolderList(this.locator(), state.path)
                        .let {
                            log.d(it.data.toString())
                            when (it) {
                                is NetResult.Data -> it.data
                                is NetResult.Error -> {
                                    _labels.value = ErrorMessage(
                                        getString(
                                            Res.string.files_error_loading_folder, it.code ?: "", it.msg ?: ""
                                        )
                                    )
                                    null
                                }
                            }
                        }

                is LocalNodeDomain -> try {
                    getFolderListUseCase.getFolderList(state.path)
                } catch (e: Exception) {
                    _labels.value =
                        ErrorMessage(getString(Res.string.files_error_loading_folder, "", e.message ?: ""))
                    null
                }

                else -> throw IllegalStateException("Not supported")
            }

            folder
                ?.takeIf { it.children.isEmpty() && it.playlist.items.isEmpty() }
                ?.also { _labels.value = ErrorMessage(getString(Res.string.files_error_empty)) }

            folder
                ?.takeIf { it.children.isNotEmpty() || it.playlist.items.isNotEmpty() }
                ?.also { folderOk ->
                    val upItem = folderOk.children.find { PARENT_FOLDER_TEXT.equals(it.title) }
                    state.upListItem = upItem?.let { mapper.mapParentItem(it) }
                    state.currentFolder =
                        folderOk.copy(children = upItem?.let { folderOk.children.minus(it) } ?: folderOk.children)

                    state.currentListItems = state.currentFolder?.let { mapper.mapToIntermediate(it) }
                } ?: also {
                state.upListItem = null
                state.currentFolder = null
                state.currentListItems = null
            }
        }
        map(false)
    }

    private fun map(loading: Boolean) {
        _modelObservable.value = mapper.map(state = state, loading = loading)
    }

    private fun resetState() {
        state.upListItem = null
        state.currentFolder = null
        state.currentListItems = null
        state.selectedFile = null
        state.sourceRemoteId = null
        state.sourceNode = null
        state.path = null
    }
}
