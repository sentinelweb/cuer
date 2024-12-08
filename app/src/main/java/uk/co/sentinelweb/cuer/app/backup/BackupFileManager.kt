package uk.co.sentinelweb.cuer.app.backup

import android.content.Context
import android.os.Build
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.file.AFile
import uk.co.sentinelweb.cuer.app.db.repository.file.ImageFileRepository
import uk.co.sentinelweb.cuer.app.db.repository.file.ImageFileRepository.Companion.REPO_SCHEME
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.core.ext.getFileName
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.ext.serialise
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class BackupFileManager constructor(
    private val playlistRepository: PlaylistDatabaseRepository,
    private val backupJsonManager: IBackupJsonManager,
    private val timeProvider: TimeProvider,
    private val timeStampMapper: TimeStampMapper,
    private val imageFileRepository: ImageFileRepository,
    private val contextProvider: CoroutineContextProvider,
    private val context: Context,
    private val log: LogWrapper,
) : IBackupManager {
    init {
        log.tag(this)
    }

    override suspend fun restoreData(data: String): Boolean = backupJsonManager.restoreData(data)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun makeBackupZipFile(): AFile = withContext(contextProvider.IO) {
        val f = File(context.cacheDir, makeFileName())
        //try {
        val out = ZipOutputStream(FileOutputStream(f))
        val playlistsFlat = playlistRepository.loadList(AllFilter, flat = true).data!!

        val dbJsonFile = File(context.cacheDir, DB_FILE_JSON)
        createBackupJsonFile(playlistsFlat, dbJsonFile)
        if (!dbJsonFile.exists()) error("Failed to save backup file")
        val e = ZipEntry(DB_FILE_JSON)
        out.putNextEntry(e)
//            val jsonDataBytes = backupDataJson(playlists).toByteArray()
//            out.write(jsonDataBytes, 0, jsonDataBytes.size)
        copyDbJsonFileToOutput(dbJsonFile, out)
        out.flush()
        out.closeEntry()
        dbJsonFile.delete()

        playlistsFlat
            .map { listOf(it.image, it.thumb) }
            .flatten()
            .mapNotNull { it }
            .filter { it.url.startsWith(REPO_SCHEME) }
            .distinct()
            .forEach {
                imageFileRepository.loadImage(it)?.apply {
                    val imgEntry = ZipEntry(it.url.getFileName())
                    out.putNextEntry(imgEntry)
                    out.write(this, 0, this.size)
                    out.flush()
                    out.closeEntry()
                }
            }
        out.close()
        //} catch (t: Throwable) {
        //    log.e("Error backing up", t)
        //}
        AFile(f.absolutePath)
    }

    private fun copyDbJsonFileToOutput(dbJsonFile: File, out: ZipOutputStream) {
        val bufferSize = 1024
        FileInputStream(dbJsonFile).use { fis ->
            BufferedInputStream(fis, bufferSize).use { bis ->
                val buffer = ByteArray(bufferSize)
                var length: Int
                while (bis.read(buffer).also { length = it } > 0) {
                    out.write(buffer, 0, length)
                }
            }
        }
    }

    suspend fun createBackupJsonFile(playlistsFlat: List<PlaylistDomain>, file: File) {
        FileOutputStream(file).use { fos ->
            // serialises empty backup json and splits it at the playlist point
            val template = backupDataJsonTemplate()
            val locatorString = "\"playlists\": []"
            val splitPoint = template.lastIndexOf(locatorString) + locatorString.length - 1
            val templateHeader = template.substring(0, splitPoint)
            val templateFooter = template.substring(splitPoint)
            fos.write(templateHeader.toByteArray())

            var firstChunk = true

            playlistsFlat
                .map { playlistRepository.load(it.id!!.id, flat = false).data!! }
                .forEach { playlist ->
                    val jsonDataChunk = playlist.serialise()

                    if (!firstChunk) {
                        fos.write(",".toByteArray())
                    }
                    fos.write(jsonDataChunk.toByteArray())

                    //log.d("data chunk:playlistid:${playlist.id?.id} ${jsonDataChunk.length} bytes")

                    firstChunk = false
                    fos.flush()
                }

            fos.write(templateFooter.toByteArray())
        }
    }

    private fun backupDataJsonTemplate() =
        BackupFileModel(
            version = BACKUP_VERSION,
            medias = listOf(), // todo remove?
            playlists = listOf()
        ).serialise()


    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun restoreDataZip(f: AFile): Boolean = withContext(contextProvider.IO) {
        imageFileRepository.removeAll(false)
        ZipFile(File(f.path)).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    when (entry.name) {
                        DB_FILE_JSON -> {
                            restoreData(input.readBytes().decodeToString())
                        }

                        else -> { // assume image
                            File(imageFileRepository._dir.path, entry.name)
                                .outputStream()
                                .use { output -> input.copyTo(output) }
                        }
                    }
                }
            }
        }
        true
    }

    private fun makeFileName(): String {
        val device = Build.MODEL.replace(" ", "_")
        val timeStamp = timeStampMapper.toTimestampSimple(
            timeProvider.localDateTime()
        )
        return "v$BACKUP_VERSION-$timeStamp-cuer_backup-$device.zip"
    }

    companion object {
        const val BACKUP_VERSION = 4
        const val DB_FILE_JSON = "database.json"
    }
}
