package uk.co.sentinelweb.cuer.app.ui.filebrowser

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

interface FileBrowserContract {

    @Serializable
    data class State(
        var remoteId: GUID? = null,
        var node: RemoteNodeDomain? = null,
        var currentFolder: PlaylistAndChildrenDomain? = null
    )
}
