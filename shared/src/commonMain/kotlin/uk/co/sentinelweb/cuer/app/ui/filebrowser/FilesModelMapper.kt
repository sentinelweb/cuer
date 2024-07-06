package uk.co.sentinelweb.cuer.app.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain

class FilesModelMapper {

    fun map(list: PlaylistAndChildrenDomain) = FilesModel(list = list)
}
