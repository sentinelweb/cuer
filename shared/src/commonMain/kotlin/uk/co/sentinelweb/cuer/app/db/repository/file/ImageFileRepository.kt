package uk.co.sentinelweb.cuer.app.db.repository.file

import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.std.localCurrentDirVfs
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class ImageFileRepository(
    private val pathParent: String,
    private val client: HttpClient,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
    private val platformOperation: PlatformOperation
) {
    var _dir: VfsFile
        private set

    init {
        val parent = VfsFile(localCurrentDirVfs.vfs, pathParent)
        _dir = parent.get(DIRECTORY_NAME)
        log.d("USE DIR: ${_dir.absolutePath}")
        coroutines.ioScope.launch {
            if (!_dir.exists()) _dir.mkdir()
        }
    }

    suspend fun saveImage(uri: String): String = withContext(coroutines.IO) {
        if (uri.startsWith("http")) {
            val url = Url(uri)
            log.d("get url:$url")
            val httpResponse: HttpResponse = client.get(url)
            val byteArrayBody: ByteArray = httpResponse.receive()
            val fullPath = url.fullPath
            val fileName = getFileName(fullPath)
            // todo might be a problem if theres duplicate files
            val targetFile = getUniqueFile(fileName)
            targetFile.writeBytes(byteArrayBody)
            log.d("got url:${targetFile.absolutePath}")
            SCHEME_PREFIX + getFileName(targetFile.absolutePath)
        } else if (uri.startsWith("file")) {
            val file = VfsFile(localCurrentDirVfs.vfs, uri.substring("file://".length))
            log.d("copy:${file.absolutePath}")
            val fileName = getFileName(uri)
            val targetFile = getUniqueFile(fileName)
            file.copyTo(targetFile)
            log.d("got file:${targetFile.absolutePath}")
            SCHEME_PREFIX + getFileName(targetFile.absolutePath)
        } else throw IllegalArgumentException("uri not supported: $uri")
    }

    suspend fun loadImage(uri: String): ByteArray? = withContext(coroutines.IO) {
        if (uri.startsWith(SCHEME_PREFIX)) {
            val file = _dir.get(uri.substring(SCHEME_PREFIX.length))
            if (file.exists()) {
                file.readBytes()
            } else null
        } else null
    }

    suspend fun toLocalUri(uri: String): String? = withContext(coroutines.IO) {
        if (uri.startsWith(SCHEME_PREFIX)) {
            val file = _dir.get(uri.substring(SCHEME_PREFIX.length))
            if (file.exists()) {
                "file://${file.absolutePath}"
            } else null
        } else null
    }

    suspend fun remove() = withContext(coroutines.IO) {
        _dir.list().onEach {
            platformOperation.delete(it)
        }
        platformOperation.delete(_dir)
    }

    private fun getFileName(fullPath: String) =
        fullPath.substring(fullPath.lastIndexOf("/") + 1)

    private suspend fun getUniqueFile(fileName: String): VfsFile {
        var targetFile = _dir.get(fileName)
        var ctr = 0
        while (targetFile.exists()) {
            ctr = ctr.inc()
            val nextFileName = if (fileName.indexOf(".") > 0) {
                val name = fileName.substring(0, fileName.indexOf("."))
                val ext = fileName.substring(fileName.indexOf(".") + 1)
                "$name$ctr.$ext"
            } else {
                "$fileName$ctr"
            }
            targetFile = _dir.get(nextFileName)
        }
        return targetFile
    }

    companion object {
        val SCHEME = "cuerfile"
        val SCHEME_PREFIX = SCHEME + "://"
        val DIRECTORY_NAME = SCHEME
    }

}