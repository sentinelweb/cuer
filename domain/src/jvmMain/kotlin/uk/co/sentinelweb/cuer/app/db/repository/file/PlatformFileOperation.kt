package uk.co.sentinelweb.cuer.app.db.repository.file

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.koin.core.component.KoinComponent
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

actual class PlatformFileOperation: KoinComponent {

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
            ?.let { jfile ->
                AFileProperties(
                    file = file,
                    name = jfile.name,
                    size = jfile.length(),
                    isDirectory = jfile.isDirectory,
                    modified = if (jfile.isDirectory) {
                        val filePath = Paths.get(jfile.absolutePath)
                        val attributes = Files.readAttributes(filePath, BasicFileAttributes::class.java)
                        attributes.lastModifiedTime().toInstant().toKotlinInstant()

                    } else {
                        Instant.fromEpochMilliseconds(jfile.lastModified())
                    }
                )
            }

    actual fun parent(file: AFile): AFile? =
        File(file.path).parent?.let { AFile(it) }
}
