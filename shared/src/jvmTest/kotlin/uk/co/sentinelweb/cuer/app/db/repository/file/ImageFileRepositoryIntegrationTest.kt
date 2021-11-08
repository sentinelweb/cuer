package uk.co.sentinelweb.cuer.app.db.repository.file

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.soywiz.korio.file.std.localCurrentDirVfs
import com.soywiz.korio.lang.substr
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.db.repository.file.ImageFileRepository.Companion.SCHEME_PREFIX
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import java.io.File
import kotlin.test.assertEquals

class ImageFileRepositoryIntegrationTest {

    private val log = SystemLogWrapper()

    private lateinit var sut: ImageFileRepository

    @Before
    fun setUp() {
        sut = ImageFileRepository(
            localCurrentDirVfs.absolutePath,
            HttpClient(CIO),
            CoroutineContextProvider(),
            log,
            PlatformOperation()
        )
    }

    @After
    fun tearDown() {
       runBlocking { sut.remove() }
    }

    @Test
    fun saveImage_http() = runBlocking {
        val httpImage = "https://cuer.app/assets/img/screenshots/playlist.png"
        val uri = sut.saveImage(httpImage)
        assertThat(uri).isEqualTo(SCHEME_PREFIX + "playlist.png")
        val fileUri = sut.toLocalUri(uri)!!
        val filePath = fileUri.substring("file://".length)
        assertTrue(File(filePath).exists())
        Unit
    }

    @Test
    fun saveImage_http_duplicate() = runBlocking {
        val httpImage = "https://cuer.app/assets/img/screenshots/playlist.png"
        sut.saveImage(httpImage)
        val uri = sut.saveImage(httpImage)
        assertThat(uri).isEqualTo(SCHEME_PREFIX + "playlist1.png")
        val fileUri = sut.toLocalUri(uri)!!
        val filePath = fileUri.substring("file://".length)
        assertTrue(File(filePath).exists())
        Unit
    }

    @Test
    fun saveImage_local() = runBlocking {
        val file =
            "file://${localCurrentDirVfs.absolutePath}/../media/stock_photo/playlist/original/art-school-of-athens-1143741_1920.jpg"
        val uri = sut.saveImage(file)
        assertThat(uri).isEqualTo(SCHEME_PREFIX + "art-school-of-athens-1143741_1920.jpg")
        val fileUri = sut.toLocalUri(uri)!!
        val filePath = fileUri.substring("file://".length)
        assertTrue(File(filePath).exists())
        Unit
    }

    @Test
    fun loadImage_bytes() = runBlocking {
        val file =
            "file://${localCurrentDirVfs.absolutePath}/../media/stock_photo/playlist/original/art-school-of-athens-1143741_1920.jpg"
        val uri = sut.saveImage(file)
        assertThat(uri).isEqualTo(SCHEME_PREFIX + "art-school-of-athens-1143741_1920.jpg")
        val bytes = sut.loadImage(uri)
        val filePath = file.substring("file://".length)
        assertThat(bytes).isEqualTo(File(filePath).readBytes())
        Unit
    }

    @Test
    fun toLocalUri() = runBlocking {
        val httpImage = "https://cuer.app/assets/img/screenshots/playlist.png"
        val uri = sut.saveImage(httpImage)
        assertThat(uri).isEqualTo(SCHEME_PREFIX + "playlist.png")
        val localFileUri = sut.toLocalUri(uri)
        assertThat(localFileUri).isEqualTo("file://${sut._dir.absolutePath}/playlist.png")
        Unit
    }
}