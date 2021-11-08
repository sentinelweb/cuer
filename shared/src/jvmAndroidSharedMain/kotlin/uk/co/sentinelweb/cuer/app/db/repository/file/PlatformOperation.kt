package uk.co.sentinelweb.cuer.app.db.repository.file

import com.soywiz.korio.file.VfsFile
import java.io.File

actual class PlatformOperation {
    actual fun delete(file: VfsFile) {
        val platformFile = File(file.absolutePath)
        if (platformFile.exists()) {
            if (platformFile.isDirectory()) {
                platformFile.deleteRecursively()
            } else platformFile.delete()
        } else throw IllegalArgumentException("File doesn't exist: ${file.absolutePath}")
    }
}
