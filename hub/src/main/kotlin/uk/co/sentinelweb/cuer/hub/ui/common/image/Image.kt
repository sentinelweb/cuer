import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.bridge.BridgeContext
import org.apache.batik.bridge.GVTBuilder
import org.apache.batik.bridge.UserAgentAdapter
import org.apache.batik.util.XMLResourceDescriptor
import org.jetbrains.skia.Image
import org.w3c.dom.Element
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO
import javax.swing.ImageIcon

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

// Extension function to convert BufferedImage to Compose ImageBitmap
fun BufferedImage.toImageBitmap(): ImageBitmap {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(this, "PNG", outputStream)
    val byteArray = outputStream.toByteArray()
    val skijaImage = Image.makeFromEncoded(byteArray)
    return skijaImage.toComposeImageBitmap()
}

// Extension function to convert BufferedImage to ImageIcon
fun BufferedImage.toImageIcon(): ImageIcon {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(this, "PNG", outputStream)
    val byteArray = outputStream.toByteArray()
    val inputStream = ByteArrayInputStream(byteArray)
    val image = ImageIO.read(inputStream)
    return ImageIcon(image)
}

@OptIn(ExperimentalStdlibApi::class)
fun loadSVG(resourcePath: String, color: Color, size: Int): BufferedImage {
    val inputStream: InputStream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
        ?: throw IllegalArgumentException("Resource not found: $resourcePath")
    val svgURI = Thread.currentThread().contextClassLoader.getResource(resourcePath)?.toURI()

    val parser = XMLResourceDescriptor.getXMLParserClassName()
    val factory = SAXSVGDocumentFactory(parser)
    val document = factory.createDocument(svgURI.toString(), inputStream)

    // Convert Compose Color to RGB Hex String
    val hexColor = "#" + color.value.toString(16).substring(2, 8)

    // Apply the color to SVG elements.
    val paths = document.getElementsByTagName("path")
    for (i in 0 until paths.length) {
        val element = paths.item(i) as Element
        element.setAttribute("fill", hexColor)
    }

    val userAgent = UserAgentAdapter()
    val ctx = BridgeContext(userAgent)
    val builder = GVTBuilder()
    val rootNode = builder.build(ctx, document)

    val svgRoot = document.documentElement
    val originalWidth = svgRoot.getAttribute("width").removeSuffix("px").toFloatOrNull() ?: size.toFloat()
    val originalHeight = svgRoot.getAttribute("height").removeSuffix("px").toFloatOrNull() ?: size.toFloat()

    // Calculate scale to fit the SVG into the specified size while maintaining aspect ratio
    val scale = size / maxOf(originalWidth, originalHeight)

    val bufferedImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val graphics2D = bufferedImage.createGraphics()

    // Apply scaling transformation
    val at = AffineTransform.getScaleInstance(scale.toDouble(), scale.toDouble())
    graphics2D.transform(at)

    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    rootNode.paint(graphics2D)
    graphics2D.dispose()

    return bufferedImage
}
