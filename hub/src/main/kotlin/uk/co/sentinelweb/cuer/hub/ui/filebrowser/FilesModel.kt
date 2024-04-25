package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.PlaylistAndSubsDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

data class FilesModel(
    // todo map domain to model
    val list: PlaylistAndSubsDomain
) {
    companion object {
        fun blankModel() = FilesModel(PlaylistAndSubsDomain(PlaylistDomain(title = "Blank")))
    }
}
