package uk.co.sentinelweb.cuer.app.db.repository.file

data class AFile(
    val path: String
) {
    fun child(name: String): AFile = AFile(this.path + "/" + name)
}

expect class PlatformFileOperation() {
    fun delete(file: AFile)

    fun currentDir(): AFile

    fun exists(file: AFile): Boolean

    fun mkdirs(dir: AFile): Boolean

    fun writeBytes(file: AFile, bytes: ByteArray)

    fun readBytes(file: AFile): ByteArray

    fun copyTo(from: AFile, to: AFile)

    fun list(dir: AFile): List<AFile>?
}