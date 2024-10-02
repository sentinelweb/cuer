package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.shared.generated.resources.*

object FilesComposeables {

    @Composable
    fun FilesUi(interactions: FilesContract.Interactions) {
        val state = interactions.modelObservable.collectAsState(initial = FilesModel.blankModel())
        FilesView(state.value, interactions)
    }

    @Composable
    private fun FilesView(model: FilesModel, view: FilesContract.Interactions) {
        Column(
            modifier = Modifier.padding(8.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxHeight()
        ) {
            model.list.children.forEach {
                Row(modifier = Modifier.clickable { view.onClickFolder(it) }) {
                    val icon = if (it.title.equals("..")) Res.drawable.ic_up else Res.drawable.ic_folder
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface), //, blendMode = BlendMode.Multiply
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(text = it.title, modifier = Modifier.padding(16.dp))
                }
            }
            model.list.playlist.items.forEach {
                Row(modifier = Modifier.clickable {
                    view.onClickFile(it)
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
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface), //, blendMode = BlendMode.Multiply
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(text = it.media.title ?: "No title", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
