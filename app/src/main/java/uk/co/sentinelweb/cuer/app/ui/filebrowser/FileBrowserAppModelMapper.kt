package uk.co.sentinelweb.cuer.app.ui.filebrowser

import uk.co.sentinelweb.cuer.domain.ext.name

class FileBrowserAppModelMapper {
    fun map(state: FileBrowserContract.State, loading: Boolean) = FileBrowserContract.AppFilesUiModel(
        loading = loading,
        subTitle = state.sourceNode?.let { it.name() + (state.path?.let { "/$it" } ?: "") }
    )
}
