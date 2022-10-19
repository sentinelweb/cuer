package uk.co.sentinelweb.cuer.app.backup

import android.content.Context
import android.os.Build
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaLocalDateTime
import uk.co.sentinelweb.cuer.app.backup.version.ParserFactory
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer.Companion.DEFAULT_PLAYLIST_TEMPLATE
import uk.co.sentinelweb.cuer.app.db.repository.*
import uk.co.sentinelweb.cuer.app.db.repository.file.ImageFileRepository
import uk.co.sentinelweb.cuer.app.db.repository.file.ImageFileRepository.Companion.REPO_SCHEME
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.ext.getFileName
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.net.mappers.TimeStampMapper
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class BackupFileManager constructor(
    private val channelRepository: ChannelDatabaseRepository,
    private val mediaRepository: MediaDatabaseRepository,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val playlistItemRepository: PlaylistItemDatabaseRepository,
    private val imageDatabaseRepository: ImageDatabaseRepository,
    private val contextProvider: CoroutineContextProvider,
    private val parserFactory: ParserFactory,
    private val playlistItemCreator: PlaylistItemCreator,
    private val timeProvider: TimeProvider,
    private val timeStampMapper: TimeStampMapper,
    private val imageFileRepository: ImageFileRepository,
    private val context: Context,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    private fun makeFileName(): String {
        val device = Build.MODEL.replace(" ", "_")
        val timeStamp = timeStampMapper.mapDateTimeSimple(
            timeProvider.localDateTime().toJavaLocalDateTime()
        )
        return "v$VERSION-$timeStamp-cuer_backup-$device.zip"
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun makeBackupZipFile(): File = withContext(contextProvider.IO) {
        val f = File(context.cacheDir, makeFileName())
        try {
            val out = ZipOutputStream(FileOutputStream(f))
            val e = ZipEntry(DB_FILE_JSON)
            out.putNextEntry(e)
            val playlists = playlistRepository.loadList(OrchestratorContract.AllFilter()).data!!
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
        f
    }

    private suspend fun backupDataJson(playlists: List<PlaylistDomain>) =
        withContext(contextProvider.IO) {
            BackupFileModel(
                version = 3,
                medias = listOf(),
                playlists = playlists
            ).serialise()
        }

    suspend fun restoreData(data: String): Boolean = withContext(contextProvider.IO) {
        val backupFileModel = parserFactory.create(data).parse(data)

        if (backupFileModel.version == 3) {
            return@withContext mediaRepository.deleteAll()
                .takeIf { it.isSuccessful }
                ?.let { channelRepository.deleteAll() }
                ?.takeIf { it.isSuccessful }
                ?.let { playlistRepository.deleteAll() }
                ?.takeIf { it.isSuccessful }
                ?.let { playlistItemRepository.deleteAll() }
                ?.takeIf { it.isSuccessful }
                ?.let { imageDatabaseRepository.deleteAll() }
                ?.takeIf { it.isSuccessful }
                ?.let {
                    backupFileModel.medias.chunked(CHUNK_SIZE)
                        .map { mediaRepository.save(it) }
                        .reduce { acc: RepoResult<List<MediaDomain>>, result: RepoResult<List<MediaDomain>> ->
                            RepoResult.Composite<List<MediaDomain>>(
                                acc.isSuccessful && result.isSuccessful,
                                result.data?.let { acc.data?.plus(it) }
                            )
                        }
                }
                ?.takeIf { it.isSuccessful }
                ?.data
                ?.let { savedMedias ->
                    val idLookup = savedMedias.map { it.platformId to it }.toMap()
                    backupFileModel.playlists.fold(
                        true
                    ) { acc, playlist ->
                        playlistRepository.save(
                            playlist.copy(
                                items = playlist.items.map {
                                    it.copy(
                                        media = idLookup.get(it.media.platformId)
                                            ?: throw IllegalArgumentException("Media ID lookup failed: ${it.media.platformId}")
                                    )
                                }
                            )
                        ).isSuccessful && acc
                    }
                }
                ?.also {
                    log.d("--- file -----")
                    log.d("medias: " + backupFileModel.medias.size)
                    log.d("items: " + backupFileModel.playlists.fold(0) { acc, p -> acc + p.items.size })
                    log.d("playlists: " + backupFileModel.playlists.size)
                    log.d("--- db -----")
                    log.d("images: " + imageDatabaseRepository.count().data)
                    log.d("channels: " + channelRepository.count().data)
                    log.d("medias: " + mediaRepository.count().data)
                    log.d("items: " + playlistItemRepository.count().data)
                    log.d("playlists: " + playlistRepository.count().data)
                } ?: false
        } else {
            return@withContext mediaRepository.deleteAll()
                .takeIf { it.isSuccessful }
                ?.let { channelRepository.deleteAll() }
                ?.takeIf { it.isSuccessful }
                ?.let { mediaRepository.save(backupFileModel.medias) }
                ?.takeIf { it.isSuccessful }
                ?.let { playlistRepository.deleteAll() }
                ?.takeIf { it.isSuccessful }
                ?.let {
                    playlistRepository.save(backupFileModel.playlists).let {
                        if (it.isSuccessful && (it.data?.filter { it.default }?.size ?: 0) == 0) {
                            playlistRepository.save(DEFAULT_PLAYLIST_TEMPLATE)
                        } else it
                    }
                }
                ?.takeIf { it.isSuccessful }
                ?.let {
                    playlistRepository
                        .loadList(OrchestratorContract.DefaultFilter())
                        .takeIf { it.isSuccessful && (it.data?.size ?: 0) > 0 }
                        ?.let { defPlaylistResult ->
                            val orderBase = timeProvider.currentTimeMillis()
                            backupFileModel.medias.mapIndexedNotNull { idx, item ->
                                defPlaylistResult.data?.get(0)?.let { defPlist ->
                                    playlistItemCreator.buildPlayListItem(
                                        item,
                                        defPlist,
                                        orderBase + (idx * 1000)
                                    )
                                }
                            }.let {
                                playlistItemRepository.save(it)
                            }.isSuccessful
                        } ?: true
                } ?: false
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun restoreDataZip(f: File): Boolean = withContext(contextProvider.IO) {
        imageFileRepository.removeAll(false)
        ZipFile(f).use { zip ->
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

    companion object {
        const val VERSION = 3
        const val CHUNK_SIZE = 400
        const val DB_FILE_JSON = "database.json"
    }
}
