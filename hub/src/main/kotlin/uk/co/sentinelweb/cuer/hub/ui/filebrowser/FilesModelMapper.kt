package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.PlaylistAndSubsDomain

class FilesModelMapper {

    fun map(list: PlaylistAndSubsDomain) = FilesModel(list = list)
}
