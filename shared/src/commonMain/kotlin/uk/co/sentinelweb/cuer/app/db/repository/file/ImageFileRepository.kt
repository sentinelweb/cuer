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
    private val platformOperation: PlatformOperation,
    private val directoryName:String = DIRECTORY_NAME_DEFAULT
) {
    var _dir: AFile
        private set

    init {
        val parent = AFile(pathParent)
        _dir = parent.child(directoryName)
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
            val targetFile = makeUniqueFileName(fileName, byteArrayBody)
            platformOperation.writeBytes(targetFile, byteArrayBody)
            log.d("got url:${targetFile.path}")
            SCHEME_PREFIX + targetFile.path
        } else if (uri.startsWith("file")) {
            val strippedFilePath = stripFileScheme(uri)
            val file = AFile(strippedFilePath)
            val targetFile = makeUniqueFileName(file.path, platformOperation.readBytes(file))
            platformOperation.copyTo(file, targetFile)
            log.d("create file: ${targetFile.path}")
            SCHEME_PREFIX + targetFile.path
        } else throw IllegalArgumentException("uri not supported: $uri")
    }

    suspend fun loadImage(uri: String): ByteArray? = withContext(coroutines.IO) {
        if (uri.startsWith(SCHEME)) {
            val strippedFilePath = stripFileScheme(uri)
            val file = AFile(strippedFilePath)
            if (platformOperation.exists(file)) {
                platformOperation.readBytes(file)
            } else null
        } else null
    }

//    suspend fun toLocalUri(uri: String): String? = withContext(coroutines.IO) {
//        if (uri.startsWith(SCHEME_PREFIX)) {
//            val file = _dir.child(uri.substring(SCHEME_PREFIX.length))
//            if (platformOperation.exists(file)) {
//                "$SCHEME_PREFIX${file.path}"
//            } else null
//        } else null
//    }

    suspend fun remove() = withContext(coroutines.IO) {
        platformOperation.list(_dir)?.onEach {
            platformOperation.delete(it)
        }
        platformOperation.delete(_dir)
    }

    private fun stripFileScheme(fullPath: String) = if (fullPath.startsWith(SCHEME_PREFIX)) {
        fullPath.substring(SCHEME_PREFIX.length)
    } else if (fullPath.startsWith(SCHEME_PREFIX_JAVA)){
        fullPath.substring(SCHEME_PREFIX_JAVA.length)
    } else throw IllegalArgumentException(fullPath)

    private fun getFileName(fullPath: String) = fullPath.substring(fullPath.lastIndexOf("/") + 1)

    private suspend fun makeUniqueFileName(fileName: String, byteArray: ByteArray): AFile {
        val fileNameStripped = getFileName(fileName)
        var targetFile = _dir.child(fileNameStripped)
        var ctr = 0
        while (platformOperation.exists(targetFile) &&
            !byteArray.contentEquals(platformOperation.readBytes(targetFile))
        ) {
            ctr = ctr.inc()
            val nextFileName = if (fileNameStripped.lastIndexOf(".") > 0) {
                val name = fileNameStripped.substring(0, fileNameStripped.lastIndexOf("."))
                val ext = fileNameStripped.substring(fileNameStripped.lastIndexOf(".") + 1)
                "${name}_$ctr.$ext"
            } else {
                "${fileNameStripped}_$ctr"
            }
            targetFile = _dir.child(nextFileName)
        }
        return targetFile
    }

    companion object {
        val SCHEME = "file"
        val SCHEME_PREFIX = SCHEME + "://"
        val SCHEME_PREFIX_JAVA = SCHEME + ":"
        val DIRECTORY_NAME_DEFAULT = "ImageRepository"
    }

}