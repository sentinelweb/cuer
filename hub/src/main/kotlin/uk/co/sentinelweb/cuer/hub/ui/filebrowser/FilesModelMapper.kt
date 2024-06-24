package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain

class FilesModelMapper {

    fun map(list: PlaylistAndChildrenDomain) = FilesModel(list = list)
}
