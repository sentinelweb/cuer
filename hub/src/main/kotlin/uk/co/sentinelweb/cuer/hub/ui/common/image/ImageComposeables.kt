package uk.co.sentinelweb.cuer.hub.ui.common.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.launch
import loadImageBitmapFromUrl

@Composable
fun ImageFromUrl(url: String) {
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        coroutineScope.launch {
            imageBitmap = loadImageBitmapFromUrl(url)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        imageBitmap?.let {
            Image(bitmap = it, contentDescription = "Loaded Image")
        }
    }
}