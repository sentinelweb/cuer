package uk.co.sentinelweb.cuer.app.ui.filebrowser

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

interface FileBrowserContract {

    @Serializable
    data class State(
        var sourceRemoteId: GUID? = null,
        var sourceNode: RemoteNodeDomain? = null,
        var path: String? = null,
        var currentFolder: PlaylistAndChildrenDomain? = null,
        // var remotePlayerConfig: PlayerNodeDomain? = null,
        var selectedFile: PlaylistItemDomain? = null,
    )

    data class AppFilesUiModel(
        val loading: Boolean
    )
}
