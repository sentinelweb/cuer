package uk.co.sentinelweb.cuer.app.ui.filebrowser

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.*

interface FilesContract {

    interface Interactions {
        val modelObservable: Flow<FilesModel>

        fun onClickFolder(folder: PlaylistDomain)

        fun onClickFile(file: PlaylistItemDomain)
    }

    @Serializable
    data class State(
        var sourceRemoteId: GUID? = null,
        var sourceNode: RemoteNodeDomain? = null,
        var path: String? = null,
        var currentFolder: PlaylistAndChildrenDomain? = null,
        var selectedFile: PlaylistItemDomain? = null,
    )

    data class AppFilesUiModel(
        val loading: Boolean,
        val subTitle: String?
    ) {
        companion object {
            val BLANK = AppFilesUiModel(loading = false, subTitle = null)
        }
    }

    sealed class Label {
        object Init : Label()
        object Up : Label()
    }
}
