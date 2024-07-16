package uk.co.sentinelweb.cuer.app.ui.filebrowser

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.*

interface FileBrowserContract {

    @Serializable
    data class State(
        var remoteId: GUID? = null,
        var node: RemoteNodeDomain? = null,
        var path: String? = null,
        var currentFolder: PlaylistAndChildrenDomain? = null,
        var remotePlayerConfig: PlayerNodeDomain? = null,
        var selectedFile: PlaylistItemDomain? = null
    )

    data class AppFilesUiModel(
        val loading: Boolean
    )
}
