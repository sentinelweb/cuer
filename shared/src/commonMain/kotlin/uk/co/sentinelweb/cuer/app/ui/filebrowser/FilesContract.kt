package uk.co.sentinelweb.cuer.app.ui.filebrowser

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.*
import kotlin.random.Random

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
        fun onSort(type: Sort)
    }

    data class State(
        var sourceRemoteId: GUID? = null,
        var sourceNode: NodeDomain? = null,
        var path: String? = null,
        var currentFolder: PlaylistAndChildrenDomain? = null,
        var currentListItems: Map<ListItem, Domain>? = null,
        var upListItem: Pair<ListItem, PlaylistDomain>? = null,
        var selectedFile: PlaylistItemDomain? = null,
        var sort: Sort = Sort.Alpha,
        var sortAcending: Boolean = false
    ) {

    }

    enum class Sort { Alpha, Time }

    data class ListItem(
        val title: String,
        val dateModified: Instant?,
        val isDirectory: Boolean,
        val tags: List<String>,
        val type: ListItemType,
        val ext: String? = null,
        val season: String? = null,
        val timeSince: String? = null,
    )

    enum class ListItemType {
        VIDEO, AUDIO, WEB, FILE, FOLDER, UP
    }

    data class Model(
        val loading: Boolean,
        val nodeName: String?,
        val filePath: String?,
        val list: List<Pair<ListItem, Domain>>?,
        var upListItem: Pair<ListItem, PlaylistDomain>? = null,
    ) {

        companion object {
            val Initial = Model(loading = false, nodeName = null, filePath = null, list = null)
        }
    }

    sealed class Label {
        object None : Label()
        object Init : Label()
        object Up : Label()
        data class ErrorMessage(
            val message: String,
            val unique: Int = Random.nextInt()
        ) : Label()
    }

    companion object {
        val module = module {
            factory { FilesModelMapper(get()) }
        }
    }
}
