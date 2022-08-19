package uk.co.sentinelweb.cuer.app.db.entity

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
import uk.co.sentinelweb.cuer.app.db.Channel
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.Media
import uk.co.sentinelweb.cuer.app.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.app.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
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

        val ids = listOf(1L, 3L, 5L)
        val actual = database.mediaEntityQueries
            .loadAllByIds(ids)
            .executeAsList()

        val expected = initial
            .map { it.first }
            .filter { ids.contains(it.id) }

        assertEquals(expected, actual)
    }

    @Test
    fun loadByMediaId() {
        val initial = (1..4).map { media() }

        val index = 3
        val selectedPlatformId = initial[index].first.platform_id
        val actual = database.mediaEntityQueries
            .loadByPlatformId(selectedPlatformId)
            .executeAsOne()

        val expected = initial[index].first

        assertEquals(expected, actual)
    }

    @Test
    fun loadFlags() {
        val initial = (1..4).map { media() }

        val id = 3L
        val actual = database.mediaEntityQueries
            .loadFlags(id)
            .executeAsOne()

        val expected = initial.find { it.first.id==id }!!.first.flags

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
        val id = 3L
        database.mediaEntityQueries
            .delete(id)

        val actual =  database.mediaEntityQueries.loadById(id).executeAsOneOrNull()
        assertNull( actual)
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

    private fun media(): Pair<Media, Channel> {
        val channel = fixture<Channel>().copy(id = 0, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(channel)
        val channelId = database.channelEntityQueries.getInsertId().executeAsOne()
        val channelWithId = channel.copy(id = channelId)
        val initial = fixture<Media>().copy(id = 0, channel_id = channelId, image_id = null, thumb_id = null)
        database.mediaEntityQueries.create(initial)
        val insertId = database.mediaEntityQueries.getInsertId().executeAsOne()
        val initialWithId = initial.copy(id = insertId)
        return initialWithId to channelWithId
    }


}