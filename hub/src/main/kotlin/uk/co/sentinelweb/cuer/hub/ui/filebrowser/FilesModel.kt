package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

data class FilesModel(
    // todo map domain to model
    val list: PlaylistAndChildrenDomain
) {
    companion object {
        fun blankModel() = FilesModel(PlaylistAndChildrenDomain(PlaylistDomain(title = "Blank")))
    }
}
