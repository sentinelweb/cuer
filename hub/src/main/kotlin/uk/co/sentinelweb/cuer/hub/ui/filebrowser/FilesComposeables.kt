package uk.co.sentinelweb.cuer.hub.ui.filebrowser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.hub.ui.common.image.ImageSvg

object FilesComposeables {
    @Composable
    fun FilesUi(coordinator: FilesUiCoordinator) {
        val state = coordinator.modelObservable.collectAsState(initial = FilesModel.blankModel())
        FilesView(state.value, coordinator)
    }

    @Composable
    private fun FilesView(model: FilesModel, view: FilesUiCoordinator) {
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.padding(8.dp).verticalScroll(scrollState)) {
            model.list.subPlaylists.forEach {
                Row(modifier = Modifier.clickable { view.loadFolder(it) }) {
                    val icon = if (it.title.equals("..")) "drawable/ic_up.svg" else "drawable/ic_folder.svg"
                    ImageSvg(icon, modifier = Modifier.padding(16.dp))
                    Text(text = it.title, modifier = Modifier.padding(16.dp))
                }
            }
            model.list.playlist.items.forEach {
                Row {
                    val icon = when (it.media.mediaType) {
                        MediaDomain.MediaTypeDomain.VIDEO -> "drawable/ic_video.svg"
                        MediaDomain.MediaTypeDomain.AUDIO -> "drawable/ic_mic.svg"
                        MediaDomain.MediaTypeDomain.WEB -> "drawable/ic_browse.svg"
                        MediaDomain.MediaTypeDomain.FILE -> "drawable/ic_file.svg"
                    }
                    ImageSvg(icon, modifier = Modifier.padding(16.dp))
                    Text(text = it.media.title ?: "No title", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
