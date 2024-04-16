package uk.co.sentinelweb.cuer.app.db.repository.file

import android.content.Context
import java.io.IOException
import java.io.InputStream

actual class AssetOperations(
    private val context: Context
) {
    actual fun getAsString(path: String): String? =
        try {
            val inputStream: InputStream = context.assets.open(path)
            inputStream.bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
}