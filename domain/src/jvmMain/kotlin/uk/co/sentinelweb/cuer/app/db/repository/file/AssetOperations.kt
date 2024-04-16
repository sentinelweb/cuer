package uk.co.sentinelweb.cuer.app.db.repository.file

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

actual class AssetOperations() {
    actual fun getAsString(path: String): String? = try {
        val inputStream: InputStream = Thread.currentThread().contextClassLoader.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Resource not found: $path")
        BufferedReader(InputStreamReader(inputStream)).readText()
    } catch (ex: IOException) {
        ex.printStackTrace()
        null
    }
}
