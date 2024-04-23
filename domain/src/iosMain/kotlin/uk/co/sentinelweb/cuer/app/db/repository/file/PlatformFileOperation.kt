package uk.co.sentinelweb.cuer.app.db.repository.file

actual class PlatformFileOperation {
    actual fun delete(file: AFile) {
        TODO("Not yet implemented")
    }

    actual fun currentDir(): AFile {
        TODO("Not yet implemented")
    }

    actual fun exists(file: AFile): Boolean {
        TODO("Not yet implemented")
    }

    actual fun mkdirs(dir: AFile): Boolean {
        TODO("Not yet implemented")
    }

    actual fun writeBytes(file: AFile, bytes: ByteArray) {
    }

    actual fun readBytes(file: AFile): ByteArray {
        TODO("Not yet implemented")
    }

    actual fun copyTo(from: AFile, to: AFile) {
        TODO("Not yet implemented")
    }

    actual fun list(dir: AFile): List<AFile>? {
        TODO("Not yet implemented")
    }

    actual fun properties(file: AFile): AFileProperties? {
        TODO("Not yet implemented")
    }

    actual fun parent(file: AFile): AFile? {
        TODO("Not yet implemented")
    }
}