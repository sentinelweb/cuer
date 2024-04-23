package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.PlaylistDomain

data class FilesModel(
    // todo map domain to model
    val list: PlaylistDomain
) {
    companion object {
        fun blankModel() = FilesModel(PlaylistDomain(title = "Blank"))
    }
}
