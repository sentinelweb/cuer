package uk.co.sentinelweb.cuer.app.db.repository.file

import java.io.File

actual class PlatformOperation {

    actual fun delete(file: AFile) {
        val platformFile = File(file.path)
        if (platformFile.exists()) {
            if (platformFile.isDirectory()) {
                platformFile.deleteRecursively()
            } else platformFile.delete()
        } else throw IllegalArgumentException("File doesn't exist: ${file.path}")
    }

    actual fun currentDir(): AFile  = AFile(System.getProperty("user.dir")!!)

    actual fun exists(file: AFile): Boolean = File(file.path).exists()

    actual fun mkdirs(dir:AFile): Boolean = File(dir.path).mkdirs()

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

}