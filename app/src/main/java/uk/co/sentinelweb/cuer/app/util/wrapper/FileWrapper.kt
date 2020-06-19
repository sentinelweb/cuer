package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Application
import android.net.Uri

class FileWrapper constructor(
    private val app: Application
) {

    fun writeDataToUri(uri: String, data: String) {
        app.contentResolver.openOutputStream(Uri.parse(uri))?.use { outputStream ->
            outputStream.write(data.toByteArray())
        }
    }
}