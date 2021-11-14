package uk.co.sentinelweb.cuer.app.db.repository.file

import com.google.common.truth.Truth.assertThat
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.BinaryBody
import org.mockserver.model.Header.header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import java.io.File
import java.net.URL

// todo use mock web server
class ImageFileRepositoryIntegrationTest {

    private val log = SystemLogWrapper()

    private lateinit var sut: ImageFileRepository
    val platformOperation = PlatformOperation()
    private lateinit var mockServer: ClientAndServer

    @Before
    fun setUp() {
        sut = ImageFileRepository(
            platformOperation.currentDir().path,
            HttpClient(CIO),
            CoroutineContextProvider(),
            log,
            platformOperation
        )

    }

    private fun setupMockServer(path: String, bytes:ByteArray) {
        mockServer = startClientAndServer(1080);
        mockServer
            .`when`(
                request()
                    .withMethod("GET")
                    .withPath(path)
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader( header("content-type", "image/jpeg"))
                    .withBody(BinaryBody(bytes))
            )
    }

    @After
    fun tearDown() {
        runBlocking { sut.remove() }
    }

    @Test
    fun saveImage_http_duplicate() = runBlocking {
        val url: URL =
            this::class.java.classLoader.getResource("art-school-of-athens-1143741_640.jpg")
        val path = "/assets/art-school-of-athens-1143741_640.jpg"
        val inputPath = url.toString().substring("file:".length)
        val bytes = File(inputPath).readBytes()
        setupMockServer(path, bytes)
        val httpImage = "http://localhost:1080" + path

        val savedUri = sut.saveImage(httpImage)
        assertThat(savedUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val savedUri2 = sut.saveImage(httpImage)
        assertThat(savedUri2).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val filePath = savedUri2.substring("file://".length)
        assertTrue(File(filePath).exists())
        mockServer.stop()
        Unit

    }

    @Test
    fun saveImage_local() = runBlocking {
        val url: URL =
            this::class.java.classLoader.getResource("art-school-of-athens-1143741_640.jpg")
        val savedUri = sut.saveImage(url.toString())
        assertThat(savedUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val filePath = savedUri.substring("file://".length)
        assertTrue(File(filePath).exists())
        Unit
    }

    @Test
    fun saveImage_local_duplicate_same_name_data() = runBlocking {
        val url: URL =
            this::class.java.classLoader.getResource("art-school-of-athens-1143741_640.jpg")
        val savedUri = sut.saveImage(url.toString())
        assertThat(savedUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val filePath = savedUri.substring("file://".length)
        assertTrue(File(filePath).exists())
        Unit
    }

    @Test
    fun saveImage_local_duplicate_same_name_different_data() = runBlocking {
        val url: URL =
            this::class.java.classLoader.getResource("art-school-of-athens-1143741_640.jpg")
        val firstUri = sut.saveImage(url.toString())
        val differentUrl: URL =
            this::class.java.classLoader.getResource("diff_file_same_name/art-school-of-athens-1143741_640.jpg")
        val savedUri = sut.saveImage(differentUrl.toString())
        assertThat(savedUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640_1.jpg")
        val filePath = savedUri.substring("file://".length)
        assertTrue(File(filePath).exists())
        Unit
    }

    @Test
    fun loadImage_bytes() = runBlocking {
        val url: URL =
            this::class.java.classLoader.getResource("art-school-of-athens-1143741_640.jpg")
        val savedUri = sut.saveImage(url.toString())
        assertThat(savedUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val bytes = sut.loadImage(savedUri)
        val filePath = savedUri.substring("file://".length)
        assertTrue(File(filePath).exists())
        val readBytes = File(filePath).readBytes()
        log.d(readBytes.toString())
        assertThat(bytes).isEqualTo(readBytes)
        Unit
    }

    private fun makeRepoRoot() = "file://" + sut._dir.path + "/"
}