package uk.co.sentinelweb.cuer.app.db.backup

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.backup.version.BackupFileModel
import uk.co.sentinelweb.cuer.app.db.backup.version.ParserFactory
import uk.co.sentinelweb.cuer.app.db.backup.version.jsonBackupSerialzer
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator

class BackupFileManager constructor(
    private val mediaRepository: MediaDatabaseRepository,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val contextProvider: CoroutineContextProvider,
    private val parserFactory: ParserFactory,
    private val initialiser: DatabaseInitializer,
    private val playlistItemCreator: PlaylistItemCreator,
    private val timeProvider: TimeProvider
) {

    suspend fun backupData() = withContext(contextProvider.IO) {
        BackupFileModel(
            version = 3,
            medias = listOf(),
            playlists = playlistRepository.loadList(OrchestratorContract.AllFilter()).data!!
        ).let {
            jsonBackupSerialzer.encodeToString(BackupFileModel.serializer(), it)
        }
    }

    suspend fun restoreData(data: String): Boolean = withContext(contextProvider.IO) {
        val backupFileModel = parserFactory.create(data).parse(data)

        if (backupFileModel.version == 3) {
            return@withContext mediaRepository.deleteAll()
                .takeIf { it.isSuccessful }
                ?.let { mediaRepository.deleteAllChannels() }
                ?.takeIf { it.isSuccessful }
                ?.let { playlistRepository.deleteAll() }
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
                    playlistRepository.save(
                        backupFileModel.playlists.map {
                            it.copy(
                                items = it.items.map {
                                    it.copy(
                                        media = idLookup.get(it.media.platformId)
                                            ?: throw IllegalArgumentException("media ID lookup failed: ${it.media.platformId}")
                                    )
                                }
                            )
                        }
                    )
                }
                ?.takeIf { it.isSuccessful }
                ?.isSuccessful
                ?: false
        } else {
            return@withContext mediaRepository.deleteAll()
                .takeIf { it.isSuccessful }
                ?.let { mediaRepository.deleteAllChannels() }
                ?.takeIf { it.isSuccessful }
                ?.let { mediaRepository.save(backupFileModel.medias) }
                ?.takeIf { it.isSuccessful }
                ?.let { playlistRepository.deleteAll() }
                ?.takeIf { it.isSuccessful }
                ?.let {
                    playlistRepository.save(backupFileModel.playlists).let {
                        if (it.isSuccessful && it.data?.filter { it.default }?.size ?: 0 == 0) {
                            playlistRepository.save(DatabaseInitializer.DEFAULT_PLAYLIST)
                        } else it
                    }
                }
                ?.takeIf { it.isSuccessful }
                ?.let {
                    playlistRepository
                        .loadList(OrchestratorContract.DefaultFilter())
                        .takeIf { it.isSuccessful && it.data?.size ?: 0 > 0 }
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
                                playlistRepository.savePlaylistItems(it)
                            }.isSuccessful
                        } ?: true
                } ?: false
        }
    }

    companion object {
        const val VERSION = 3
        const val CHUNK_SIZE = 400
    }
}
