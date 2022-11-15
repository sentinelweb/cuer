package uk.co.sentinelweb.cuer.app.db.repository.file

import android.content.Context
import java.io.InputStream

actual class AssetOperation(
    private val context: Context
) {
    actual fun getAsString(path: String): String? =
        try {
            val inputStream: InputStream = context.assets.open(path)
            inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
}