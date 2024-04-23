package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class FilesModelMapper {

    fun map(list:PlaylistDomain) = FilesModel(list = list)
}
