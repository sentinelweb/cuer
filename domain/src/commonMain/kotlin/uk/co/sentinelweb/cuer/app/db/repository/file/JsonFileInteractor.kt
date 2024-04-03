package uk.co.sentinelweb.cuer.app.db.repository.file

class JsonFileInteractor(
    private val file: AFile,
    private val operation: PlatformFileOperation
) {
    fun loadJson(): String? = try {
        operation.readBytes(file).decodeToString()
    } catch (e: Exception) {
        null
    }

    fun saveJson(data: String) {
        operation.writeBytes(file, data.encodeToByteArray())
    }

    fun exists() =
        operation.exists(file)
}
