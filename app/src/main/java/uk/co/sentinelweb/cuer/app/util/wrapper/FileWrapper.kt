package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Application
import android.net.Uri
import java.io.*
import java.lang.Integer.max

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

    fun copyFileToUri(dataFile: File, uri: String) {
        app.contentResolver.openOutputStream(Uri.parse(uri))?.use { outputStream ->
            outputStream.write(dataFile.readBytes())
        }
    }

    fun overwriteFileToUri(dataFile: File, uri: String) {

        app.contentResolver.openOutputStream(Uri.parse(uri))?.use { outputStream ->
            outputStream as FileOutputStream
            outputStream.channel.truncate(0)
            outputStream.write(dataFile.readBytes())
        }
    }

    fun getFileUriDescriptorSummary(uri: String): Pair<Boolean, String> {
        return try {
            app.contentResolver.openFileDescriptor(Uri.parse(uri), "w")
                ?.use {
                    it.fileDescriptor.valid() to
                            "size: ${it.statSize / 1024}k valid: ${it.fileDescriptor.valid()}"
                } ?: let { false to "Couldn't open file descriptor" }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false to "error: ${e.message}"
        } catch (e: IOException) {
            e.printStackTrace()
            false to "error: ${e.message}"
        } catch (e: SecurityException) {
            e.printStackTrace()
            false to "error: Permission denial: ${e.message}"
        }
    }

    fun copyFileFromUri(uri: String): File {
        val pathSegments = Uri.parse(uri).pathSegments
        val fileName = pathSegments.last().run {
            substring(max(length - 30, 0))
        }
        val file = File(app.cacheDir, fileName)
        file.outputStream().use { outputStream ->
            app.contentResolver.openInputStream(Uri.parse(uri))?.use { inputStream ->
                outputStream.write(inputStream.readBytes())
            }
        }
        return file
    }
}