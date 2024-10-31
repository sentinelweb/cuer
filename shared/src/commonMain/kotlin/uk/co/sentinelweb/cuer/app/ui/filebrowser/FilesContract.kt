package uk.co.sentinelweb.cuer.app.ui.filebrowser

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.*

interface FilesContract {

    interface ViewModel {
        val modelObservable: Flow<Model>

        fun onClickFolder(folder: PlaylistDomain)

        fun init(remoteId: GUID, path: String?)
        fun init(node: NodeDomain?, path: String?)
        fun onClickFile(file: PlaylistItemDomain)
        fun onUpClick()
        fun onBackClick()
        fun onRefreshClick()
    }

    data class State(
        var sourceRemoteId: GUID? = null,
        var sourceNode: NodeDomain? = null,
        var path: String? = null,
        var currentFolder: PlaylistAndChildrenDomain? = null,
        var currentListItems: Map<ListItem, Domain>? = null,
        var selectedFile: PlaylistItemDomain? = null,
    )

    data class ListItem(
        val title: String,
        val dateModified: Instant?,
        val isDirectory: Boolean,
        val tags: List<String>,
        val type: ListItemType
    )

    enum class ListItemType {
        VIDEO, AUDIO, WEB, FILE, FOLDER, UP
    }

    data class Model(
        val loading: Boolean,
        val nodeName: String?,
        val filePath: String?,
        val list: Map<ListItem, Domain>?,
    ) {

        companion object {
            val Initial = Model(loading = false, nodeName = null, filePath = null, list = null)
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
