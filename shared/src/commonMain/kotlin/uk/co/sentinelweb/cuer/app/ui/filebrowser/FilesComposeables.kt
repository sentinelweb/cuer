package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.shared.generated.resources.*

object FilesComposeables {
    @Composable
    fun FilesUi(coordinator: FilesContract.Interactions) {
        val state = coordinator.modelObservable.collectAsState(initial = FilesModel.blankModel())
        FilesView(state.value, coordinator)
    }

    @Composable
    private fun FilesView(model: FilesModel, view: FilesContract.Interactions) {
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.padding(8.dp).verticalScroll(scrollState)) {
            model.list.children.forEach {
                Row(modifier = Modifier.clickable { view.clickFolder(it) }) {
                    val icon = if (it.title.equals("..")) Res.drawable.ic_up else Res.drawable.ic_folder
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xaa000000)), //, blendMode = BlendMode.Multiply
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(text = it.title, modifier = Modifier.padding(16.dp))
                }
            }
            model.list.playlist.items.forEach {
                Row(modifier = Modifier.clickable {
                    view.clickFile(it)
                }) {
                    val icon = when (it.media.mediaType) {
                        VIDEO -> Res.drawable.ic_video
                        AUDIO -> Res.drawable.ic_mic
                        WEB -> Res.drawable.ic_browse // not valid here
                        FILE -> Res.drawable.ic_file
                    }
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xaa000000)), //, blendMode = BlendMode.Multiply
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(text = it.media.title ?: "No title", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
