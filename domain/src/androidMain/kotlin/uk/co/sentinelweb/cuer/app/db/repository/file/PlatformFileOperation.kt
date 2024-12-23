package uk.co.sentinelweb.cuer.app.db.repository.file

import kotlinx.datetime.Instant
import java.io.File

actual class PlatformFileOperation {

    actual fun delete(file: AFile) {
        val platformFile = File(file.path)
        if (platformFile.exists()) {
            if (platformFile.isDirectory()) {
                platformFile.deleteRecursively()
            } else platformFile.delete()
        } else throw IllegalArgumentException("File doesn't exist: ${file.path}")
    }

    actual fun currentDir(): AFile = AFile(System.getProperty("user.dir")!!)

    actual fun exists(file: AFile): Boolean = File(file.path).exists()

    actual fun mkdirs(dir: AFile): Boolean = File(dir.path).mkdirs()

    actual fun writeBytes(file: AFile, bytes: ByteArray) {
        File(file.path).writeBytes(bytes)
    }

    actual fun readBytes(file: AFile): ByteArray =
        File(file.path).readBytes()

    actual fun copyTo(from: AFile, to: AFile) {
        File(from.path).copyTo(File(to.path), overwrite = true)
    }

    actual fun list(dir: AFile): List<AFile>? =
        File(dir.path).listFiles()
            ?.map { AFile(it.absolutePath) }

    actual fun properties(file: AFile): AFileProperties? =
        File(file.path)
            .takeIf { it.exists() }
            ?.let {
                AFileProperties(
                    file = file,
                    name = it.name,
                    size = it.length(),
                    isDirectory = it.isDirectory,
                    modified = Instant.fromEpochMilliseconds(it.lastModified())
                )
            }

    actual fun parent(file: AFile): AFile? =
        File(file.path).parent?.let { AFile(it) }

}
