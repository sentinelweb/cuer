package uk.co.sentinelweb.cuer.app.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain
import uk.co.sentinelweb.cuer.domain.ext.name

class FilesModelMapper {
    fun map(state: FilesContract.State, loading: Boolean) = FilesContract.AppFilesUiModel(
        loading = loading,
        subTitle = state.sourceNode?.let { it.name() + (state.path?.let { "/$it" } ?: "") }
    )

    fun map(list: PlaylistAndChildrenDomain) = FilesModel(list = list)
}
