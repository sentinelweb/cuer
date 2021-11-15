package uk.co.sentinelweb.cuer.app.db.repository.file

import com.google.common.truth.Truth.assertThat
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.assertTrue
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.BinaryBody
import org.mockserver.model.Header.header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import java.io.File
import java.net.URL

class ImageFileRepositoryIntegrationTest {

    private val log = SystemLogWrapper()


    private lateinit var sut: ImageFileRepository
    val platformOperation = PlatformFileOperation()

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

    @After
    fun tearDown() {
        runBlocking { sut.removeAll() }
    }

    @Test
    fun saveImage_http_duplicate() = runBlocking {
        val savedImage = sut.saveImage(testImageFromWebServer)
        val savedLocalUri = sut.toLocalUri(savedImage.url)
        assertThat(savedLocalUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val savedImage2 = sut.saveImage(testImageFromWebServer)
        val savedLocalUri2 = sut.toLocalUri(savedImage2.url)
        assertThat(savedLocalUri2).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val filePath = savedLocalUri2?.substring("file://".length)
        assertTrue(File(filePath!!).exists())
    }

    @Test
    fun saveImage_http_named() = runBlocking {
        val name = "test name"
        val savedImage = sut.saveImage(testImageFromWebServer, name)
        val savedLocalUri = sut.toLocalUri(savedImage.url)
        assertThat(savedLocalUri).isEqualTo(makeRepoRoot() + "test_name.jpg")
        val filePath = savedLocalUri?.substring("file://".length)
        assertTrue(File(filePath!!).exists())
    }

    @Test
    fun saveImage_local() = runBlocking {
        val url: URL? =
            this::class.java.classLoader?.getResource("art-school-of-athens-1143741_640.jpg")
        val savedImage = sut.saveImage(ImageDomain(url.toString()))
        val savedLocalUri = sut.toLocalUri(savedImage.url)
        assertThat(savedLocalUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val filePath = sut.toLocalUri(savedImage.url)?.substring("file://".length)
        assertTrue(File(filePath!!).exists())
    }

    @Test
    fun saveImage_local_duplicate_same_name_and_data() = runBlocking {
        val url: URL? =
            this::class.java.classLoader?.getResource("art-school-of-athens-1143741_640.jpg")
        val firstImage = sut.saveImage(ImageDomain(url.toString()))
        val savedImage = sut.saveImage(ImageDomain(url.toString()))
        val savedLocalUri = sut.toLocalUri(savedImage.url)
        assertThat(savedLocalUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val filePath = sut.toLocalUri(savedImage.url)?.substring("file://".length)
        assertTrue(File(filePath!!).exists())
    }

    @Test
    fun saveImage_local_named_duplicate_diff_name_and_same_data() = runBlocking {
        val name = "test name"
        val firstFileUrl: URL? =
            this::class.java.classLoader?.getResource("art-school-of-athens-1143741_640.jpg")
        val firstImage = sut.saveImage(ImageDomain(firstFileUrl.toString()), name)
        val firstLocalUri = sut.toLocalUri(firstImage.url)
        assertThat(firstLocalUri).isEqualTo(makeRepoRoot() + "test_name.jpg")
        val firsrFilePath = sut.toLocalUri(firstImage.url)?.substring("file://".length)
        assertTrue(File(firsrFilePath!!).exists())

        val diffFileUrl: URL? =
            this::class.java.classLoader?.getResource("diff-name-art-school-of-athens-1143741_640.jpg")
        val sameImage = sut.saveImage(ImageDomain(diffFileUrl.toString()), name)
        assertThat(sameImage).isEqualTo(firstImage)
        val sameLocalUri = sut.toLocalUri(sameImage.url)
        assertThat(sameLocalUri).isEqualTo(makeRepoRoot() + "test_name.jpg")
        val filePath = sut.toLocalUri(sameImage.url)?.substring("file://".length)
        assertTrue(File(filePath!!).exists())
    }

    @Test
    fun saveImage_local_named_duplicate_same_name_and_data() = runBlocking {
        val url: URL? =
            this::class.java.classLoader?.getResource("art-school-of-athens-1143741_640.jpg")
        val firstImage = sut.saveImage(ImageDomain(url.toString()))
        val savedImage = sut.saveImage(ImageDomain(url.toString()))
        val savedLocalUri = sut.toLocalUri(savedImage.url)
        assertThat(savedLocalUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val filePath = sut.toLocalUri(savedImage.url)?.substring("file://".length)
        assertTrue(File(filePath!!).exists())
    }

    @Test
    fun saveImage_local_named_duplicate_same_name_different_data() = runBlocking {
        val name = "test name"
        val url: URL? =
            this::class.java.classLoader?.getResource("art-school-of-athens-1143741_640.jpg")
        val firstImage = sut.saveImage(ImageDomain(url.toString()), name)
        val firstLocalUri = sut.toLocalUri(firstImage.url)
        assertThat(firstLocalUri).isEqualTo(makeRepoRoot() + "test_name.jpg")
        val differentUrl: URL? =
            this::class.java.classLoader?.getResource("diff_file_same_name/art-school-of-athens-1143741_640.jpg")
        val savedImage = sut.saveImage(ImageDomain(differentUrl.toString()), name)
        val savedLocalUri = sut.toLocalUri(savedImage.url)
        assertThat(savedLocalUri).isEqualTo(makeRepoRoot() + "test_name_1.jpg")
        val filePath = sut.toLocalUri(savedImage.url)?.substring("file://".length)
        assertTrue(File(filePath!!).exists())
    }

    @Test
    fun loadImage_bytes() = runBlocking {
        val url: URL? =
            this::class.java.classLoader?.getResource("art-school-of-athens-1143741_640.jpg")
        val savedImage = sut.saveImage(ImageDomain(url.toString()))
        val savedLocalUri = sut.toLocalUri(savedImage.url)
        assertThat(savedLocalUri).isEqualTo(makeRepoRoot() + "art-school-of-athens-1143741_640.jpg")
        val bytes = sut.loadImage(savedImage)
        val filePath = savedLocalUri?.substring("file://".length)
        assertTrue(File(filePath!!).exists())
        val readBytes = File(filePath).readBytes()
        log.d(readBytes.toString())
        assertThat(bytes).isEqualTo(readBytes)
    }

    private fun makeRepoRoot() = "file://" + sut._dir.path + "/"

    companion object {
        @JvmStatic
        private lateinit var mockServer: ClientAndServer
        private lateinit var testImageFromWebServer: ImageDomain

        @BeforeClass
        @JvmStatic
        fun startupServer() {
            val url: URL? =
                this::class.java.classLoader?.getResource("art-school-of-athens-1143741_640.jpg")
            val path = "/assets/art-school-of-athens-1143741_640.jpg"
            val inputPath = url.toString().substring("file:".length)
            val bytes = File(inputPath).readBytes()
            setupMockServer(path, bytes)
            testImageFromWebServer = ImageDomain("http://localhost:1080" + path)
        }

        @AfterClass()
        fun shutdownServer() {
            mockServer.stop()
        }

        private fun setupMockServer(path: String, bytes: ByteArray) {
            mockServer = startClientAndServer(1080)
            mockServer
                .`when`(
                    request()
                        .withMethod("GET")
                        .withPath(path)
                )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withHeader(header("content-type", "image/jpeg"))
                        .withBody(BinaryBody(bytes))
                )
        }
    }
}