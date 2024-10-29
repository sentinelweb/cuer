package uk.co.sentinelweb.cuer.app.ui.filebrowser

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.*

interface FilesContract {

    interface ViewModel {
        val modelObservable: Flow<FilesModel>

        fun onClickFolder(folder: PlaylistDomain)

        fun init(remoteId: GUID, path: String?)
        fun init(node: NodeDomain?, path: String?)
        fun onClickFile(file: PlaylistItemDomain)
        fun onUpClick()
        fun onBackClick()
        fun onRefreshClick()
    }

    @Serializable
    data class State(
        var sourceRemoteId: GUID? = null,
        var sourceNode: NodeDomain? = null,
        var path: String? = null,
        var currentFolder: PlaylistAndChildrenDomain? = null,
        var selectedFile: PlaylistItemDomain? = null,
    )


    data class FilesModel(
        val loading: Boolean,
        val nodeName: String?,
        val filePath: String?,
        val list: PlaylistAndChildrenDomain?,
    ) {
        companion object {
            val Initial = FilesModel(loading = false, nodeName = null, filePath = null, list = null)
        }
    }

    sealed class Label {
        object Init : Label()
        object Up : Label()
    }

    companion object {
        val module = module {
            factory { FilesModelMapper() }
        }
    }
}
