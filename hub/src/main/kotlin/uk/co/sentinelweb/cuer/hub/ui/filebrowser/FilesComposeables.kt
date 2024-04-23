package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object FilesComposeables {
    @Composable
    fun FilesUi(coordinator: FilesUiCoordinator) {
        val state = coordinator.modelObservable.collectAsState(initial = FilesModel.blankModel())
        FilesView(state.value, coordinator)
    }

    @Composable
    private fun FilesView(model: FilesModel, view: FilesUiCoordinator) {

        Column(modifier = Modifier.padding(8.dp)) {
            model.list.subPlaylists.forEach {
                Text(text = it.title, modifier = Modifier.padding(16.dp).clickable { view.loadFolder(it) })
            }
            model.list.items.forEach {
                Text(text = it.media.title?:"No title", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
