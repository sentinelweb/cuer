package uk.co.sentinelweb.cuer.app.db.repository.file

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.core.ext.fileExt
import uk.co.sentinelweb.cuer.core.ext.getFileName
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain

class ImageFileRepository(
    pathParent: String,
    private val client: HttpClient,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
    private val platformOperation: PlatformFileOperation,
    directoryName: String = DIRECTORY_NAME_DEFAULT
) {
    var _dir: AFile
        private set

    init {
        val parent = AFile(pathParent)
        _dir = parent.child(directoryName)
        // log.d("USE DIR: ${_dir.path}")
        coroutines.ioScope.launch {
            if (!platformOperation.exists(_dir)) platformOperation.mkdirs(_dir)
        }
    }

    suspend fun saveImage(input: ImageDomain, nameBase: String? = null): ImageDomain =
        withContext(coroutines.IO) {
            if (input.url.startsWith("http")) {
                val url = Url(input.url)
                val httpResponse: HttpResponse = client.get(url)
                val byteArrayBody: ByteArray = httpResponse.body()
                val fullPath = url.fullPath
                val fileName = buildFileName(fullPath, nameBase)
                val targetFile = makeUniqueFileName(fileName, byteArrayBody)
                platformOperation.writeBytes(targetFile, byteArrayBody)
                input.copy(url = REPO_SCHEME_PREFIX + targetFile.path.getFileName())
            } else if (input.url.startsWith("file")) {
                val strippedFilePath = stripFileScheme(input.url)
                val inputFile = AFile(strippedFilePath)
                val fileName = buildFileName(strippedFilePath, nameBase)
                val targetFile = makeUniqueFileName(fileName, platformOperation.readBytes(inputFile))
                platformOperation.copyTo(inputFile, targetFile)
                input.copy(url = REPO_SCHEME_PREFIX + targetFile.path.getFileName())
            } else throw IllegalArgumentException("uri not supported: ${input.url}")
        }

    suspend fun loadImage(input: ImageDomain): ByteArray? = withContext(coroutines.IO) {
        if (input.url.startsWith(REPO_SCHEME)) {
            val strippedFilePath = stripRepoScheme(input.url)
            val file = AFile(filePathToRepoFile(strippedFilePath))
            getBytes(file)
        } else null
    }

    fun toLocalUri(uri: String): String? =
        toLocalPath(uri)?.let { FILE_SCHEME_PREFIX + it }

    fun toLocalPath(uri: String): String? =
        if (uri.startsWith(REPO_SCHEME_PREFIX)) {
            val file = _dir.child(stripRepoScheme(uri))
            if (platformOperation.exists(file)) {
                file.path
            } else null
        } else null

    suspend fun removeAll(removeRoot: Boolean = true) = withContext(coroutines.IO) {
        platformOperation.list(_dir)?.onEach {
            platformOperation.delete(it)
        }
        if (removeRoot) {
            platformOperation.delete(_dir)
        }
    }

    private fun buildFileName(fullPath: String, nameBase: String?) = (nameBase
        ?.replace("\\s".toRegex(), "_")
        ?.let { it + (fullPath.fileExt()?.let { "." + it } ?: ".jpg") }
        ?: fullPath.getFileName())

    private fun getBytes(file: AFile) =
        if (platformOperation.exists(file)) {
            platformOperation.readBytes(file)
        } else null

    private fun stripFileScheme(fullPath: String) = if (fullPath.startsWith(FILE_SCHEME_PREFIX)) {
        fullPath.substring(FILE_SCHEME_PREFIX.length)
    } else if (fullPath.startsWith(FILE_SCHEME_PREFIX_JAVA)) {
        fullPath.substring(FILE_SCHEME_PREFIX_JAVA.length)
    } else throw IllegalArgumentException(fullPath)

    private fun stripRepoScheme(fullPath: String) = fullPath.substring(REPO_SCHEME_PREFIX.length)

    private fun filePathToRepoFile(fileName: String) = _dir.path + "/" + fileName

    private fun makeUniqueFileName(fileName: String, byteArray: ByteArray): AFile {
        val fileNameStripped = fileName.getFileName()
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
        val REPO_SCHEME = "cuerimage"
        val REPO_SCHEME_PREFIX = REPO_SCHEME + "://"
        val FILE_SCHEME = "file"
        val FILE_SCHEME_PREFIX = FILE_SCHEME + "://"
        val FILE_SCHEME_PREFIX_JAVA = FILE_SCHEME + ":"
        val DIRECTORY_NAME_DEFAULT = "ImageRepository"
    }

}