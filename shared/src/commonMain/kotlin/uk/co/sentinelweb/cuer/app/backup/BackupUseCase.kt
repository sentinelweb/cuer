package uk.co.sentinelweb.cuer.app.backup

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.backup.version.ParserFactory
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.db.repository.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator

class BackupUseCase(
    private val channelRepository: ChannelDatabaseRepository,
    private val mediaRepository: MediaDatabaseRepository,
    private val playlistItemRepository: PlaylistItemDatabaseRepository,
    private val imageDatabaseRepository: ImageDatabaseRepository,
    private val contextProvider: CoroutineContextProvider,
    private val parserFactory: ParserFactory,
    private val playlistItemCreator: PlaylistItemCreator,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
) : IBackupJsonManager {

    init {
        log.tag(this)
    }

    override suspend fun restoreData(data: String): Boolean = withContext(contextProvider.IO) {
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
                        .map { mediaRepository.save(it, flat = false, emit = false) }
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
                            ),
                            flat = false, emit = false
                        ).isSuccessful && acc
                    }
                }
                ?.also {
                    log.d("--- file -----")
                    log.d("medias: " + backupFileModel.medias.size)
                    log.d("items: " + backupFileModel.playlists.fold(0) { acc, p -> acc + p.items.size })
                    log.d("playlists: " + backupFileModel.playlists.size)
                    log.d("--- db -----")
                    log.d("images: " + imageDatabaseRepository.count(OrchestratorContract.Filter.AllFilter).data)
                    log.d("channels: " + channelRepository.count(OrchestratorContract.Filter.AllFilter).data)
                    log.d("medias: " + mediaRepository.count(OrchestratorContract.Filter.AllFilter).data)
                    log.d("items: " + playlistItemRepository.count(OrchestratorContract.Filter.AllFilter).data)
                    log.d("playlists: " + playlistRepository.count(OrchestratorContract.Filter.AllFilter).data)
                } ?: false
        } else {
            return@withContext mediaRepository.deleteAll()
                .takeIf { it.isSuccessful }
                ?.let { channelRepository.deleteAll() }
                ?.takeIf { it.isSuccessful }
                ?.let { mediaRepository.save(backupFileModel.medias, flat = false, emit = false) }
                ?.takeIf { it.isSuccessful }
                ?.let { playlistRepository.deleteAll() }
                ?.takeIf { it.isSuccessful }
                ?.let {
                    playlistRepository.save(backupFileModel.playlists, flat = false, emit = false).let {
                        if (it.isSuccessful && (it.data?.filter { it.default }?.size ?: 0) == 0) {
                            playlistRepository.save(
                                DatabaseInitializer.DEFAULT_PLAYLIST_TEMPLATE,
                                flat = false,
                                emit = false
                            )
                        } else it
                    }
                }
                ?.takeIf { it.isSuccessful }
                ?.let {
                    playlistRepository
                        .loadList(OrchestratorContract.Filter.DefaultFilter, flat = false)
                        .takeIf { it.isSuccessful && (it.data?.size ?: 0) > 0 }
                        ?.let { defPlaylistResult ->
                            val orderBase = timeProvider.currentTimeMillis()
                            backupFileModel.medias.mapIndexedNotNull { idx, item ->
                                defPlaylistResult.data?.get(0)?.let { defPlist ->
                                    playlistItemCreator.buildPlayListItem(
                                        item,
                                        defPlist,
                                        orderBase + (idx * 1000),
                                        timeProvider.instant()
                                    )
                                }
                            }.let {
                                playlistItemRepository.save(it, flat = false, emit = false)
                            }.isSuccessful
                        } ?: true
                } ?: false
        }
    }

    companion object {
        const val CHUNK_SIZE = 400
    }
}