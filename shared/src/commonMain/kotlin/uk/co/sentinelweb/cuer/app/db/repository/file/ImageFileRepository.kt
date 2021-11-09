package uk.co.sentinelweb.cuer.app.db.repository.file

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
    var _dir: AFile
        private set

    init {
        val parent = AFile(pathParent)
        _dir = parent.child(DIRECTORY_NAME)
        log.d("USE DIR: ${_dir.path}")
        coroutines.ioScope.launch {
            if (!platformOperation.exists(_dir)) platformOperation.mkdirs(_dir)
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
            platformOperation.writeBytes(targetFile, byteArrayBody)
            log.d("got url:${targetFile.path}")
            SCHEME_PREFIX + getFileName(targetFile.path)
        } else if (uri.startsWith("file")) {
            val file = AFile(uri.substring("file://".length))
            log.d("copy:${file.path}")
            val fileName = getFileName(uri)
            val targetFile = getUniqueFile(fileName)
            platformOperation.copyTo(file, targetFile)
            log.d("got file:${targetFile.path}")
            SCHEME_PREFIX + getFileName(targetFile.path)
        } else throw IllegalArgumentException("uri not supported: $uri")
    }

    suspend fun loadImage(uri: String): ByteArray? = withContext(coroutines.IO) {
        if (uri.startsWith(SCHEME_PREFIX)) {
            val file = _dir.child(uri.substring(SCHEME_PREFIX.length))
            if (platformOperation.exists(file)) {
                platformOperation.readBytes(file)
            } else null
        } else null
    }

    suspend fun toLocalUri(uri: String): String? = withContext(coroutines.IO) {
        if (uri.startsWith(SCHEME_PREFIX)) {
            val file = _dir.child(uri.substring(SCHEME_PREFIX.length))
            if (platformOperation.exists(file)) {
                "file://${file.path}"
            } else null
        } else null
    }

    suspend fun remove() = withContext(coroutines.IO) {
        platformOperation.list(_dir)?.onEach {
            platformOperation.delete(it)
        }
        platformOperation.delete(_dir)
    }

    private fun getFileName(fullPath: String) =
        fullPath.substring(fullPath.lastIndexOf("/") + 1)

    private suspend fun getUniqueFile(fileName: String): AFile {
        var targetFile = _dir.child(fileName)
        var ctr = 0
        while (platformOperation.exists(targetFile)) {
            ctr = ctr.inc()
            val nextFileName = if (fileName.indexOf(".") > 0) {
                val name = fileName.substring(0, fileName.indexOf("."))
                val ext = fileName.substring(fileName.indexOf(".") + 1)
                "$name$ctr.$ext"
            } else {
                "$fileName$ctr"
            }
            targetFile = _dir.child(nextFileName)
        }
        return targetFile
    }

    companion object {
        val SCHEME = "cuerfile"
        val SCHEME_PREFIX = SCHEME + "://"
        val DIRECTORY_NAME = SCHEME
    }

}