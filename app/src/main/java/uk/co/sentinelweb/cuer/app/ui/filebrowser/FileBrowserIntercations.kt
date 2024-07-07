package uk.co.sentinelweb.cuer.app.ui.filebrowser

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class FileBrowserIntercations : FilesContract.Interactions {
    override val modelObservable: Flow<FilesModel>
        get() = TODO("Not yet implemented")

    override fun clickFolder(folder: PlaylistDomain) {
        TODO("Not yet implemented")
    }

    override fun clickFile(file: PlaylistItemDomain) {
        TODO("Not yet implemented")
    }
}