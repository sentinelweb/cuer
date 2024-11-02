package uk.co.sentinelweb.cuer.hub.ui.filebrowser.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.domain.MediaDomain
import java.io.File
import javax.swing.JFrame


@Composable
private fun TextFileWindow(path: String) {

    // Remember coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Hold file content in mutable state
    var fileContent by remember { mutableStateOf("") }

    // Load file content asynchronously
    LaunchedEffect(path) {
        coroutineScope.launch {
            val loadedFileContent = withContext(Dispatchers.IO) {
                File(path).readText()
            }
            fileContent = loadedFileContent
        }
    }

    // Render the content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {
        BasicText(
            text = fileContent,
            style = TextStyle(fontFamily = FontFamily.Monospace, color = Color.Black),
        )
    }
}

fun showTextWindow(media: MediaDomain) {

    val frame = JFrame("New Window")
    val panel = ComposePanel().apply {
        setContent {
            TextFileWindow(media.platformId)
        }
    }
    frame.contentPane.add(panel)
    frame.setSize(400, 640)
    frame.isVisible = true
}
