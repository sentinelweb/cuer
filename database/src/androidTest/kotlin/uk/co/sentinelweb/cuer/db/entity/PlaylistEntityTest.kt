package uk.co.sentinelweb.cuer.db.entity

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.database.entity.Channel
import uk.co.sentinelweb.cuer.database.entity.Media
import uk.co.sentinelweb.cuer.database.entity.Playlist
import uk.co.sentinelweb.cuer.database.entity.Playlist_item
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.domain.ext.hasFlag
import kotlin.test.assertEquals

class PlaylistEntityTest : KoinTest {
    private val fixture = kotlinFixture { nullabilityStrategy(NeverNullStrategy) }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var dbIntegrationRule = DatabaseTestRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(
            listOf<Module>()
                .plus(dbIntegrationRule.modules)
                .plus(mainCoroutineRule.modules)
        )
    }

    val database: Database by inject()

    @Before
    fun before() {
        Database.Schema.create(get())
    }

    @Test
    fun createLoadEntity() {
        val expected = createPlaylist()
        val actual = database.playlistEntityQueries.load(expected.id).executeAsOne()
        assertEquals(expected, actual)
    }

    @Test
    fun updateEntity() {
        val initial = createPlaylist()
        val updated = createPlaylist().copy(id = initial.id)
        database.playlistEntityQueries.update(updated)
        val actual = database.playlistEntityQueries.load(initial.id).executeAsOne()
        assertEquals(updated, actual)
    }

    @Test
    fun loadAll() {
        createPlaylist()
        createPlaylist()
        val actual = database.playlistEntityQueries
            .loadAll()
            .executeAsList()
        assertEquals(2, actual.size)
    }

    @Test
    fun loadAllByIds() {
        val playlist1 = createPlaylist()
        createPlaylist()
        val playlist3 = createPlaylist()
        val actual = database.playlistEntityQueries
            .loadAllByIds(listOf(playlist1.id, playlist3.id))
            .executeAsList()
        assertEquals(2, actual.size)
        assertEquals(playlist1, actual[0])
        assertEquals(playlist3, actual[1])
    }

    @Test
    fun loadAllByPlatformIds() {
        val playlist1 = createPlaylist()
        createPlaylist()
        val playlist3 = createPlaylist()
        val actual = database.playlistEntityQueries
            .loadAllByPlatformIds(listOf(playlist1.platform_id, playlist3.platform_id))
            .executeAsList()
        assertEquals(2, actual.size)
        assertEquals(playlist1, actual[0])
        assertEquals(playlist3, actual[1])
    }

    @Test
    fun loadAllByFlags() {
        val playlist1 = createPlaylist()
        val playlist2 = createPlaylist()
        val playlist3 = createPlaylist()
        val actual = database.playlistEntityQueries
            .loadAllByFlags(FLAG_STARRED)
            .executeAsList()
        val expectedList = listOf(playlist1, playlist2, playlist3).filter { it.flags.hasFlag(FLAG_STARRED) }
        assertEquals(expectedList.size, actual.size)
        assertEquals(expectedList, actual)
    }

    @Test
    fun delete() {
        val playlist1 = createPlaylist()
        createPlaylist()
        database.playlistEntityQueries.delete(playlist1.id)
        val actual = database.playlistEntityQueries
            .loadAll()
            .executeAsList()
        assertEquals(1, actual.size)
    }

    @Test
    fun deleteAll() {
        createPlaylist()
        createPlaylist()
        database.playlistEntityQueries.deleteAll()
        val actual = database.playlistEntityQueries
            .loadAll()
            .executeAsList()
        assertEquals(0, actual.size)
    }

    @Test
    fun updateIndex() {
        val playlist1 = createPlaylist()
        database.playlistEntityQueries.updateIndex(3, playlist1.id)
        val actual = database.playlistEntityQueries
            .load(playlist1.id)
            .executeAsOne()
        assertEquals(3, actual.currentIndex)
    }

    @Test
    fun findPlaylistsForChannelPlatformId() {
        val playlistInitial =
            fixture<Playlist>().copy(id = 0, parent_id = null, channel_id = null, image_id = null, thumb_id = null)
        database.playlistEntityQueries.create(playlistInitial)
        val playlistId = database.playlistEntityQueries.getInsertId().executeAsOne()
        val playlist = playlistInitial.copy(id = playlistId)

        val channel = fixture<Channel>().copy(id = 0, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(channel)
        val channelId = database.channelEntityQueries.getInsertId().executeAsOne()

        val media = fixture<Media>().copy(id = 0, channel_id = channelId, image_id = null, thumb_id = null)
        database.mediaEntityQueries.create(media)
        val mediaId = database.mediaEntityQueries.getInsertId().executeAsOne()

        val item = fixture<Playlist_item>().copy(id = 0, media_id = mediaId, playlist_id = playlistId)
        database.playlistItemEntityQueries.create(item)

        val actual = database.playlistEntityQueries
            .findPlaylistsForChannelPlatformId(channel.platform_id)
            .executeAsList()
        assertEquals(1, actual.size)
        assertEquals(playlist, actual[0])
    }

    @Test
    fun findPlaylistsWithTitle() {
        val playlist1 = createPlaylist()
        val actual = database.playlistEntityQueries
            .findPlaylistsWithTitle(playlist1.title)
            .executeAsList()
        assertEquals(1, actual.size)
        assertEquals(playlist1, actual[0])
    }

    private fun createPlaylist(channelId:Long? = null): Playlist {
        val initial =
            fixture<Playlist>().copy(id = 0, parent_id = null, channel_id = channelId, image_id = null, thumb_id = null)
        database.playlistEntityQueries.create(initial)
        val insertId = database.playlistEntityQueries.getInsertId().executeAsOne()
        val expected = initial.copy(id = insertId)
        return expected
    }
}