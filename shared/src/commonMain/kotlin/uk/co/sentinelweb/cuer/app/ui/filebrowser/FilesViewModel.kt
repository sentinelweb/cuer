package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Label
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Model.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Alpha
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Time
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.net.remote.RemoteFilesInteractor
import uk.co.sentinelweb.cuer.net.remote.RemotePlayerInteractor
import uk.co.sentinelweb.cuer.remote.interact.PlayerLaunchHost
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
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
    private val getFolderListUseCase: GetFolderListUseCase,
    private val localRepository: LocalRepository,
    private val localPlayerLaunchHost: PlayerLaunchHost,
) : ViewModel(), FilesContract.ViewModel {

    // fixme expose immutable
    override val modelObservable = MutableStateFlow(Initial)

    val _labels = MutableStateFlow<Label>(Label.Init)
    val labels: Flow<Label>
        get() = _labels

    init {
        log.tag(this)
    }

    // fixme: used by app could make remoteId=null for local
    override fun init(remoteId: GUID, path: String?) {
        state.sourceRemoteId = remoteId
        state.sourceNode = remotesRepository.getById(remoteId)
        state.path = path
        loadCurrentPath()
    }

    override fun init(node: NodeDomain?, path: String?) {
        state.sourceNode = node ?: localRepository.localNode
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
        if (listOf(VIDEO, AUDIO).contains(file.media.mediaType)) {
            playMedia(file)
        } else if (FILE.equals(file.media.mediaType)) {
            showFile(file)
        }
    }

    private fun showFile(file: PlaylistItemDomain) = Unit // stub

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
        log.d("sort: $type: ${state.sortAcending}")
        map(false)
    }

    private fun launchLocalPlayer(
        targetNode: LocalNodeDomain?,
        screen: PlayerNodeDomain.Screen?,
        file: PlaylistItemDomain
    ) {
        localPlayerLaunchHost.launchVideo(file, screen?.index)
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

    private fun loadCurrentPath() {
        viewModelScope.launch {
            map(true)
            state.sourceNode?.apply {
                val folder = when (this) {
                    is RemoteNodeDomain ->
                        filesInteractor.getFolderList(this.locator(), state.path).data

                    is LocalNodeDomain -> getFolderListUseCase.getFolderList(state.path)
                    else -> throw IllegalStateException("not supported")
                }
                val upItem = folder?.children?.find { "..".equals(it.title) }
                state.upListItem = upItem?.let { mapper.mapParentItem(it) }
                state.currentFolder = folder?.copy(
                    children = upItem?.let { folder.children.minus(it) } ?: folder.children,
                )

                state.currentListItems = state.currentFolder?.let { mapper.mapToIntermediate(it) }
            }
            map(false)
        }
    }

    private fun map(loading: Boolean) {
        log.d("map: $loading")
        modelObservable.value = mapper.map(state = state, loading = loading).also {
            it.list?.forEach { (i, d)-> log.d("map: ${i.title}")}
        }
    }
}
