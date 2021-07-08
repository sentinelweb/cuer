package uk.co.sentinelweb.cuer.remote.server.database

import kotlinx.datetime.Clock
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.ext.deserialiseBackupFileModel
import java.io.File
import kotlin.random.Random

/*internal*/ data class TestDatabase constructor(
    val data: BackupFileModel,
    val items: Map<Long, PlaylistItemDomain> = data.playlists.map { it.items }.flatten().associateBy { it.id!! },
    val media: Map<Long, MediaDomain> = data.playlists.map { it.items.map { it.media } }.flatten().associateBy { it.id!! },
) : RemoteDatabaseAdapter {

    override suspend fun getPlaylists(): List<PlaylistDomain> = data.playlists.map { it.copy(items = listOf()) }

    override suspend fun getPlaylist(id: Long): PlaylistDomain? = data.playlists.find { it.id == id }

    override suspend fun getPlaylistItem(id: Long): PlaylistItemDomain? = items[id]

    override suspend fun scanUrl(url: String): Domain? =
        data.playlists
            .find { it.title == "music" }
            ?.let {
                it.items
                    .get(Random.nextInt(0, it.items.size))
                    .media
            }

    override suspend fun commitPlaylistItem(item: PlaylistItemDomain): PlaylistItemDomain = item

    companion object {
        //"media/data/v3-2021-05-26_13 28 23-cuer_backup-Pixel_3a.json"
        internal fun fromFile(path: String): TestDatabase? =
            File(File(System.getProperty("user.dir")).parent, path)
                .takeIf { it.exists() }
                ?.let { TestDatabase(deserialiseBackupFileModel(it.readText())) }

        /*internal*/ fun hardcoded(): TestDatabase = TestDatabase(
            BackupFileModel(
                playlists = listOf(
                    PlaylistDomain(
                        id = 1, title = "Test", items = listOf(
                            PlaylistItemDomain(
                                id = 1,
                                dateAdded = Clock.System.now(),
                                order = 1,
                                media = MediaDomain(
                                    id = 1,
                                    title = "marc rebillet & harry mack",
                                    url = "https://www.youtube.com/watch?v=ggLpFa6CQyU",
                                    platformId = "ggLpFa6CQyU",
                                    platform = PlatformDomain.YOUTUBE,
                                    mediaType = MediaDomain.MediaTypeDomain.VIDEO,
                                    channelData = ChannelDomain(title = "author", platformId = "xxx", platform = PlatformDomain.YOUTUBE)
                                )
                            )
                        )
                    )
                ), medias = listOf()
            )
        )
    }
}