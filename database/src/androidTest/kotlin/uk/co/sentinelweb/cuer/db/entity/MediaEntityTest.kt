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
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.database.entity.Channel
import uk.co.sentinelweb.cuer.database.entity.Media
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.ext.hasFlag
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaEntityTest : KoinTest {
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
    val timeProvider: TimeProvider by inject()
    private val guidCreator = GuidCreator()

    @Before
    fun before() {
        Database.Schema.create(get())
    }

    @Test
    fun createLoadEntity() {
        val (mediaInitial, _) = media()

        val actual = database.mediaEntityQueries
            .loadById(mediaInitial.id)
            .executeAsOne()
        assertEquals(mediaInitial, actual)
    }

    @Test
    fun update() {
        val (mediaInitial, channelInitial) = media()

        val update = fixture<Media>().copy(
            id = mediaInitial.id,
            channel_id = channelInitial.id,
            image_id = null,
            thumb_id = null
        )
        database.mediaEntityQueries.update(update)

        val actual = database.mediaEntityQueries
            .loadById(mediaInitial.id)
            .executeAsOne()
        assertEquals(update, actual)
    }

    @Test
    fun updatePosition() {
        val (mediaInitial, _) = media()
        val lastPlayed = timeProvider.instant()
        database.mediaEntityQueries.updatePosition(
            id = mediaInitial.id,
            dateLastPlayed = lastPlayed,
            position = 1000,
            duration = 10000,
            flags = FLAG_WATCHED
        )
        val actual = database.mediaEntityQueries
            .loadById(mediaInitial.id)
            .executeAsOne()
        assertEquals(lastPlayed, actual.date_last_played)
        assertEquals(1000L, actual.position)
        assertEquals(10000L, actual.duration)
        assertEquals(FLAG_WATCHED, actual.flags and FLAG_WATCHED)
    }

    @Test
    fun loadAll() {
        val initial = (1..4).map { media() }

        val actual = database.mediaEntityQueries.loadAll().executeAsList()

        assertEquals(initial.map { it.first }, actual)
    }

    @Test
    fun loadAllByFlags() {
        val initial = (1..4).map { media() }

        val actual = database.mediaEntityQueries
            .loadAllByFlags(FLAG_WATCHED)
            .executeAsList()

        val expected = initial
            .map { it.first }
            .filter { it.flags.hasFlag(FLAG_WATCHED) }
        assertEquals(expected, actual)
    }

    @Test
    fun loadAllByIds() {
        val initial = (1..4).map { media() }

        val ids = initial.map { it.first.id }
        val actual = database.mediaEntityQueries
            .loadAllByIds(ids)
            .executeAsList()
            .sortedBy { it.id }

        val expected = initial
            .map { it.first }
            .sortedBy { it.id }

        assertEquals(expected, actual)
    }

    @Test
    fun loadByMediaId() {
        val initial = (1..4).map { media() }

        val index = 3
        val (selectedPlatformId, selectedPlatform) = initial[index].first.let { it.platform_id to it.platform }
        val actual = database.mediaEntityQueries
            .loadByPlatformId(selectedPlatformId, selectedPlatform)
            .executeAsOne()

        val expected = initial[index].first

        assertEquals(expected, actual)
    }

    @Test
    fun loadFlags() {
        val initial = (1..4).map { media() }

        val id = initial[3].first.id
        val actual = database.mediaEntityQueries
            .loadFlags(id)
            .executeAsOne()

        val expected = initial.find { it.first.id == id }!!.first.flags

        assertEquals(expected, actual)
    }

    @Test
    fun count() {
        val initial = (1..4).map { media() }

        val actual = database.mediaEntityQueries
            .count()
            .executeAsOne()

        assertEquals(initial.size.toLong(), actual)
    }

    @Test
    fun delete() {
        val initial = (1..4).map { media() }
        database.mediaEntityQueries
            .delete(initial[3].first.id)

        val actual = database.mediaEntityQueries.loadById(initial[3].first.id).executeAsOneOrNull()
        assertNull(actual)
    }

    @Test
    fun deleteAll() {
        (1..4).map { media() }

        database.mediaEntityQueries.deleteAll()

        val actual = database.mediaEntityQueries
            .count()
            .executeAsOne()
        assertEquals(0L, actual)
    }

    @Test//(expected = SQLiteException::class)//expected<android.database.sqlite.SQLiteException> but was<org.sqlite.SQLiteException>
    fun onConflictInsert() {
        val initial = (1..4).map { media() }
        val conflicting = initial[2].first.copy(id = initial[0].first.id)

        // try to insert conflicting
        try {
            database.mediaEntityQueries.create(conflicting)
        } catch (e: Exception) {
            assertEquals("SQLiteException", e::class.simpleName)
        }
        // item is not inserted
        //assertEquals(4, database.mediaEntityQueries.getInsertId().executeAsOne())
        assertEquals(4, database.mediaEntityQueries.count().executeAsOne())
    }

    @Test
    fun onConflictUpdate() {
        val initial = (1..4).map { media() }
        println(database.mediaEntityQueries.count().executeAsOne())
        println(
            database.mediaEntityQueries.loadAll().executeAsList()
                .map { "\nmedia:${it.id} ${it.title} ${it.platform} ${it.platform_id} flags=${it.flags}" })
        val conflicting = initial[1].first.copy(id = initial[2].first.id, flags = 12345)

        // try to insert conflicting
        database.mediaEntityQueries.update(conflicting)
        println(
            database.mediaEntityQueries.loadAll().executeAsList()
                .map { "\nmedia:${it.id} ${it.title} ${it.platform} ${it.platform_id} flags=${it.flags}" })
        // item is replaced into 3
        //assertEquals(3, database.mediaEntityQueries.getInsertId().executeAsOne())
        assertEquals(3, database.mediaEntityQueries.count().executeAsOne())
    }

    private fun media(): Pair<Media, Channel> {
        val guidChannel = guidCreator.create()
        val channel = fixture<Channel>().copy(id = guidChannel.value, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(channel)
        //val channelId = database.channelEntityQueries.getInsertId().executeAsOne()
        //val channelWithId = channel.copy(id = channelId)
        val guidMedia = guidCreator.create()
        val initial = fixture<Media>().copy(
            id = guidMedia.value,
            channel_id = guidChannel.value,
            image_id = null,
            thumb_id = null
        )
        database.mediaEntityQueries.create(initial)
//        val insertId = database.mediaEntityQueries.getInsertId().executeAsOne()
//        val initialWithId = initial.copy(id = insertId)
        return initial to channel
    }


}