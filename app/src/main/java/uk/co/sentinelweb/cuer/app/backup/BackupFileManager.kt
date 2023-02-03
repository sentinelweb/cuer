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
import java.io.File
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
        try {
            val out = ZipOutputStream(FileOutputStream(f))
            val e = ZipEntry(DB_FILE_JSON)
            out.putNextEntry(e)
            val playlists = playlistRepository.loadList(AllFilter, flat = false).data!!
            val jsonDataBytes = backupDataJson(playlists).toByteArray()
            out.write(jsonDataBytes, 0, jsonDataBytes.size)
            out.closeEntry()
            playlists
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
                        out.closeEntry()
                    }
                }
            out.close()
        } catch (t: Throwable) {
            log.e("Error backing up", t)
        }
        AFile(f.absolutePath)
    }

    private suspend fun backupDataJson(playlists: List<PlaylistDomain>) =
        withContext(contextProvider.IO) {
            BackupFileModel(
                version = 3, // todo to v4
                medias = listOf(), // todo remove
                playlists = playlists
            ).serialise()
        }

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
                            File(imageFileRepository._dir.path, entry.name).outputStream()
                                .use { output ->
                                    input.copyTo(output)
                                }
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
        const val BACKUP_VERSION = 3
        const val DB_FILE_JSON = "database.json"
    }
}
