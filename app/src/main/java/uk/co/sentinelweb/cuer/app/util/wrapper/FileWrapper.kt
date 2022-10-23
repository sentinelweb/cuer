package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Application
import android.net.Uri
import java.io.*

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

    fun getFileUriDescriptorSummary(uri: String): String {
        return try {
            app.contentResolver.openFileDescriptor(Uri.parse(uri), "w")
                ?.use {
                    "size: ${it.statSize / 1024}k valid: ${it.fileDescriptor.valid()} canDetectErrors: ${it.canDetectErrors()} "
                } ?: "null descriptor on opening"
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            "error: ${e.message}"
        } catch (e: IOException) {
            e.printStackTrace()
            "error: ${e.message}"
        } catch (e: SecurityException) {
            e.printStackTrace()
            "error: Permission denial: ${e.message}"
        }
    }

    fun copyFileFromUri(uri: String): File {
        val pathSegments = Uri.parse(uri).pathSegments
        val file = File(app.cacheDir, pathSegments.last())
        file.outputStream().use { outputStream ->
            app.contentResolver.openInputStream(Uri.parse(uri))?.use { inputStream ->
                outputStream.write(inputStream.readBytes())
            }
        }
        return file
    }
}