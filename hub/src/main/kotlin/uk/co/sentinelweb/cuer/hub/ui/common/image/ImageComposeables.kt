package uk.co.sentinelweb.cuer.hub.ui.common.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import loadImageBitmapFromUrl
import loadSVG
import toImageBitmap
import java.io.File


@Composable
fun ImageGrid(imagesDir: File) {
    val imageFiles = imagesDir
        .listFiles { _, name -> name.endsWith(".svg") }
        ?.sortedBy { it.name }
        ?: listOf<File>()

    DynamicGrid(imageFiles)
}

@Composable
fun DynamicGrid(images: List<File>) {
    val cellSize = 180.dp
    val padding = 16.dp

    BoxWithConstraints {
        val gridSize = maxWidth - (padding * 2)
        val columns = (gridSize / cellSize).toInt()
        val chunkedImages = chunkedImages(images, columns)

        LazyColumn(
            contentPadding = PaddingValues(padding)
        ) {
            items(chunkedImages) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(padding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    it.forEach {
                        Column(
                            modifier = Modifier
                                .width(cellSize)
                                .wrapContentHeight()
                                .padding(vertical = 8.dp)
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ImageSvg("drawable/${it.name}", modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                text = it.name.replace(".svg", ""),
                                style = TextStyle(fontSize = 14.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun chunkedImages(imageFiles: List<File>, chunkSize: Int): List<List<File>> {
    return imageFiles.chunked(chunkSize)
}

@Composable
fun ImageSvg(s: String, imageSize: Int = 32, tint: Color = Color.Black, modifier: Modifier) {
    val bufferedImage = loadSVG(s, tint, imageSize)
    val imageBitmap = bufferedImage.toImageBitmap()
    Image(
        bitmap = imageBitmap,
        contentDescription = "SVG Icon",
        modifier = modifier
            .width(imageSize.dp)
            .height(imageSize.dp)
    )
}

@Composable
fun ImageFromUrl(url: String, modifier: Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        coroutineScope.launch {
            imageBitmap = loadImageBitmapFromUrl(url)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        imageBitmap?.let {
            Image(bitmap = it, contentDescription = "Loaded Image", modifier = modifier)
        }
    }
}
