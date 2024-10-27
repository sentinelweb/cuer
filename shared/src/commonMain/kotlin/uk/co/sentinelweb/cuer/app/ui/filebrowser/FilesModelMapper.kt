package uk.co.sentinelweb.cuer.app.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.ext.name

class FilesModelMapper {

    fun map(
        state: FilesContract.State,
        loading: Boolean,
    ) = FilesContract.FilesModel(
        loading = loading,
        subTitle = state.sourceNode?.let { it.name() + (state.path?.let { "/$it" } ?: "") },
        list = state.currentFolder,
    )
}
