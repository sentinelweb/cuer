package uk.co.sentinelweb.cuer.app.ui.filebrowser

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface FilesContract {

    interface Interactions {
        val modelObservable: Flow<FilesModel>

        fun onClickFolder(folder: PlaylistDomain)

        fun onClickFile(file: PlaylistItemDomain)
    }
}
