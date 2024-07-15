package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModel.Companion.blankModel
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.net.remote.RemoteFilesInteractor
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.locator

class FileBrowserViewModel(
    private val state: FileBrowserContract.State,
    private val filesInteractor: RemoteFilesInteractor,
    private val remotesRepository: RemotesRepository,
    private val mapper: FilesModelMapper
) : FilesContract.Interactions, ViewModel() {

    override val modelObservable = MutableStateFlow(blankModel())

    fun init(id: GUID) {
        state.remoteId = id
        state.node = remotesRepository.getById(id)
        viewModelScope.launch {
            state.node?.apply {
                state.currentFolder = filesInteractor.getFolderList(this.locator(), null).data
                state.currentFolder?.apply {
                    modelObservable.value = mapper.map(this)
                }
            }
        }
    }

    override fun clickFolder(folder: PlaylistDomain) {

    }

    override fun clickFile(file: PlaylistItemDomain) {
        TODO("Not yet implemented")
    }
}
