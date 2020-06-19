package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Application
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

class FileWrapper constructor(
    private val app: Application
) {

    fun writeDataToUri(uri: String, data: String) {
        app.contentResolver.openOutputStream(Uri.parse(uri))?.use { outputStream ->
            outputStream.write(data.toByteArray())
        }
    }

    fun readDataFromUri(uri: String): String? {
        app.contentResolver.openInputStream(Uri.parse(uri))?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                return@readDataFromUri reader.readText()
            }
        }
        return null
    }
}