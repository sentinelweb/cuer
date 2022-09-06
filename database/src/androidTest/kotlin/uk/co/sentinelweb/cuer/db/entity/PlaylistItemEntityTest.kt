package uk.co.sentinelweb.cuer.db.entity

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit.Companion.SECOND
import kotlinx.datetime.minus
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
import uk.co.sentinelweb.cuer.database.entity.Playlist_item
import uk.co.sentinelweb.cuer.db.util.DataCreation
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaylistItemEntityTest : KoinTest {
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
    lateinit var dataCreation: DataCreation

    @Before
    fun before() {
        Database.Schema.create(get())
        dataCreation = DataCreation(database, fixture)
    }

    @Test
    fun createLoadEntity() {
        val (_, expected) = dataCreation.createPlaylistAndItem()

        val actual = database.playlistItemEntityQueries.load(1).executeAsOne()
        assertEquals(expected, actual)
    }

    @Test
    fun updateEntity() {
        val (_, initial) = dataCreation.createPlaylistAndItem()
        val updated = fixture<Playlist_item>().copy(
            id = initial.id,
            media_id = initial.media_id,
            playlist_id = initial.playlist_id
        )
        database.playlistItemEntityQueries.update(updated)

        val actual = database.playlistItemEntityQueries.load(initial.id).executeAsOne()
        assertEquals(updated, actual)
    }

    @Test
    fun loadAllByIds() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        dataCreation.createPlaylistItem(item0.playlist_id)
        val item2 = dataCreation.createPlaylistItem(item0.playlist_id)

        val actual = database.playlistItemEntityQueries.loadAllByIds(listOf(1, 3)).executeAsList()
        assertEquals(listOf(item0, item2), actual)
    }

    @Test
    fun loadAllByPlaylistId() {
        val (playlist0, item0) = dataCreation.createPlaylistAndItem()
        dataCreation.createPlaylistAndItem()

        val actual = database.playlistItemEntityQueries.loadPlaylist(playlist0.id).executeAsList()
        assertEquals(listOf(item0), actual)
    }

    @Test
    fun loadItemsByMediaId() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        dataCreation.createPlaylistItem(item0.playlist_id)
        val item2 = dataCreation.createPlaylistItem(item0.playlist_id)

        val actual = database.playlistItemEntityQueries.loadItemsByMediaId(listOf(item0.media_id, item2.media_id))
            .executeAsList()
        assertEquals(listOf(item0, item2), actual)
    }

    @Test
    fun delete() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        database.playlistItemEntityQueries.delete(item0.id)
        val actual = database.playlistItemEntityQueries.load(item0.id).executeAsOneOrNull()
        assertNull(actual)
    }

    @Test
    fun deleteAll() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        dataCreation.createPlaylistItem(item0.playlist_id)
        dataCreation.createPlaylistItem(item0.playlist_id)
        database.playlistItemEntityQueries.deleteAll()
        val actual = database.playlistItemEntityQueries.loadAll().executeAsList()
        assertEquals(0, actual.size)
    }

    @Test
    fun deleteCascade() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        database.playlistEntityQueries.delete(item0.playlist_id)
        val actual = database.playlistItemEntityQueries.load(item0.id).executeAsOneOrNull()
        assertNull(actual)
    }

    @Test
    fun deletePlaylistItems() {
        val (playlist0, _) = dataCreation.createPlaylistAndItem()
        val (_, item1) = dataCreation.createPlaylistAndItem()

        database.playlistItemEntityQueries.deletePlaylistItems(playlist0.id)

        val actual = database.playlistItemEntityQueries.loadAll().executeAsList()
        assertEquals(1, actual.size)
        assertEquals(item1, actual[0])
    }

    @Test
    fun countMediaFlags() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        val item1 = dataCreation.createPlaylistItem(item0.playlist_id)
        val item2 = dataCreation.createPlaylistItem(item0.playlist_id)
        with(database.mediaEntityQueries) {
            updatePosition(Clock.System.now(), 22L, 1000, flags = FLAG_WATCHED, item0.media_id)
            updatePosition(Clock.System.now(), 22L, 1000, flags = FLAG_WATCHED, item1.media_id)
            updatePosition(Clock.System.now(), 22L, 1000, flags = 0L, item2.media_id)
        }

        val actual = database.playlistItemEntityQueries.countMediaFlags(item0.playlist_id, FLAG_WATCHED).executeAsOne()
        assertEquals(2, actual.toInt())
    }

    @Test
    fun countItemsInPlaylist() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        dataCreation.createPlaylistItem(item0.playlist_id)
        dataCreation.createPlaylistAndItem()

        val actual = database.playlistItemEntityQueries.countItemsInPlaylist(item0.playlist_id).executeAsOne()
        assertEquals(2, actual.toInt())
    }

    @Test
    fun loadAllPlaylistItemsWithNewMedia() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        val item1 = dataCreation.createPlaylistItem(item0.playlist_id)
        val item2 = dataCreation.createPlaylistItem(item0.playlist_id)
        val item3 = dataCreation.createPlaylistItem(item0.playlist_id)
        with(database.mediaEntityQueries) {
            updatePosition(Clock.System.now(), 22L, 1000, flags = 0L, item0.media_id)
            updatePosition(Clock.System.now(), 22L, 1000, flags = 0L, item1.media_id)
            updatePosition(Clock.System.now(), 22L, 1000, flags = 0L, item2.media_id)
            updatePosition(Clock.System.now(), 22L, 1000, flags = FLAG_WATCHED, item3.media_id)
        }
        val actual = database.playlistItemEntityQueries.loadAllPlaylistItemsWithNewMedia(2).executeAsList()
        assertEquals(2, actual.size)
        val actual1 = database.playlistItemEntityQueries.loadAllPlaylistItemsWithNewMedia(4).executeAsList()
        assertEquals(3, actual1.size)
    }

    @Test
    fun loadAllPlaylistItemsRecent() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        val item1 = dataCreation.createPlaylistItem(item0.playlist_id)
        val item2 = dataCreation.createPlaylistItem(item0.playlist_id)
        val item3 = dataCreation.createPlaylistItem(item0.playlist_id)
        val dateLastPlayed = Clock.System.now()
        with(database.mediaEntityQueries) {
            updatePosition(dateLastPlayed.minus(1, SECOND), 22L, 1000, flags = FLAG_WATCHED, item0.media_id)
            updatePosition(dateLastPlayed, 22L, 1000, flags = FLAG_WATCHED, item1.media_id)
            updatePosition(dateLastPlayed.minus(2, SECOND), 22L, 1000, flags = FLAG_WATCHED, item2.media_id)
            updatePosition(dateLastPlayed, 22L, 1000, flags = 0, item3.media_id)
        }
        val actual = database.playlistItemEntityQueries.loadAllPlaylistItemsRecent(4).executeAsList()
        assertEquals(3, actual.size)
        assertEquals(item1, actual[0])
        assertEquals(item0, actual[1])
        assertEquals(item2, actual[2])
    }

    @Test
    fun search() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        val item1 = dataCreation.createPlaylistItem(item0.playlist_id)
        dataCreation.createPlaylistItem(item0.playlist_id)
        dataCreation.createPlaylistItem(item0.playlist_id)
        val media1 = database.mediaEntityQueries.loadById(item1.media_id).executeAsOne()
        val actual = database.playlistItemEntityQueries
            .search(media1.title!!.substring(2, 10), 2)
            .executeAsList()
        assertEquals(1, actual.size)
        assertEquals(item1, actual[0])
    }

    @Test
    fun searchPlaylist() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        val (_, item1) = dataCreation.createPlaylistAndItem()
        val media0 = database.mediaEntityQueries.loadById(item0.media_id).executeAsOne()
        database.mediaEntityQueries.update(media0.copy(id = item1.media_id)) // media0 == media1

        val actual = database.playlistItemEntityQueries
            .searchPlaylists(media0.title!!.substring(2, 10), listOf(item0.playlist_id), 2)
            .executeAsList()
        assertEquals(1, actual.size)
        assertEquals(item0, actual[0])
    }

    @Test
    fun loadAllByPlatformIds() {
        val (_, item0) = dataCreation.createPlaylistAndItem()
        val (_, item1) = dataCreation.createPlaylistAndItem()
        val (_, item2) = dataCreation.createPlaylistAndItem()
        val medias = database.mediaEntityQueries.loadAll().executeAsList()

        val actual = database.playlistItemEntityQueries
            .loadAllByPlatformIds(medias.map { it.platform_id })
            .executeAsList()
        assertEquals(3, actual.size)
        assertEquals(item0, actual[0])
        assertEquals(item1, actual[1])
        assertEquals(item2, actual[2])
    }
}