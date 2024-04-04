package uk.co.sentinelweb.cuer.hub.ui.common.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.jetbrains.skia.Image

suspend fun loadImageBitmapFromUrl(url: String): ImageBitmap? {
    val client = HttpClient(CIO)
    return try {
        val httpResponse: HttpResponse = client.get(url)
        val bytes = httpResponse.readBytes()
        client.close()
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (e: Exception) {
        client.close()
        e.printStackTrace()
        null
    }
}
