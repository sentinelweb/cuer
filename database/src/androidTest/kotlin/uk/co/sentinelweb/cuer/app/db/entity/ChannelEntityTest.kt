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
import uk.co.sentinelweb.cuer.app.db.Image
import uk.co.sentinelweb.cuer.app.db.di.DatabaseModule
import uk.co.sentinelweb.cuer.app.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.app.db.util.MainCoroutineRule
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChannelEntityTest : KoinTest {
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
                .plus(DatabaseModule.modules)
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
        val initial = fixture<Channel>().copy(id = 0, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
        val insertId = database.channelEntityQueries.getInsertId().executeAsOne()
        val actual = database.channelEntityQueries.load(insertId).executeAsOne()
        assertEquals(initial.copy(id = insertId), actual)
    }

    @Test
    fun loadAllChannels() {
        val initial = fixture<List<Channel>>().map { it.copy(id = 0, image_id = null, thumb_id = null) }
        database.channelEntityQueries.transaction {
            initial.forEach {
                database.channelEntityQueries.create(it)
            }
        }
        val actual = database.channelEntityQueries.loadAll().executeAsList()
        assertEquals(initial.size, actual.size)
        actual.forEach { assertTrue(it.id > 0) }
    }

    @Test
    fun count() {
        val initial = fixture<List<Channel>>().map { it.copy(id = 0, image_id = null, thumb_id = null) }
        database.channelEntityQueries.transaction {
            initial.forEach {
                database.channelEntityQueries.create(it)
            }
        }
        val actual = database.channelEntityQueries.count().executeAsOne()
        assertEquals(initial.size, actual.toInt())
    }

    @Test
    fun loadAllByIds() {
        val initial = fixture<List<Channel>>().map { it.copy(id = 0, image_id = null, thumb_id = null) }
        database.channelEntityQueries.transaction {
            initial.forEach {
                database.channelEntityQueries.create(it)
            }
        }
        val idList = listOf(1L, 3L, 5L)
        val actual = database.channelEntityQueries
            .loadAllByIds(idList)
            .executeAsList()
        assertEquals(actual.size, idList.size)
        idList.forEach { id -> assertNotNull(actual.filter { it.id == id }) }
    }

    @Test
    fun createDeleteEntity() {
        val initial = fixture<Channel>().copy(id = 0, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
        val insertId = database.channelEntityQueries
            .getInsertId()
            .executeAsOne()
        database.channelEntityQueries.delete(insertId)
        val actual = database.channelEntityQueries
            .count()
            .executeAsOne()
        assertEquals(0, actual.toInt())
    }

    @Test
    fun findByChannelId() {
        val initial = fixture<Channel>().copy(id = 0, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
        val insertId = database.channelEntityQueries
            .getInsertId()
            .executeAsOne()
        database.channelEntityQueries.findByPlatformId(initial.platform_id, initial.platform)
        val actual = database.channelEntityQueries
            .findByPlatformId(initial.platform_id, initial.platform)
            .executeAsOne()
        assertEquals(insertId, actual.id)
    }

    @Test
    fun deleteByIds() {
        val initial = fixture<List<Channel>>().map { it.copy(id = 0, image_id = null, thumb_id = null) }
        database.channelEntityQueries.transaction {
            initial.forEach {
                database.channelEntityQueries.create(it)
            }
        }

        val deleteIdList = listOf(1L, 3L, 5L)
        database.channelEntityQueries.deleteByIds(deleteIdList)
        val actual = database.channelEntityQueries
            .count()
            .executeAsOne()
        assertEquals(2, actual.toInt())
        val remainIdList = listOf(2L, 4L)
        val actualRemain = database.channelEntityQueries
            .loadAllByIds(remainIdList)
            .executeAsList()
        assertEquals(actualRemain.size, remainIdList.size)
        remainIdList.forEach { id -> assertNotNull(actualRemain.filter { it.id == id }) }
    }

    @Test
    fun deleteAll() {
        val initial = fixture<List<Channel>>().map { it.copy(id = 0, image_id = null, thumb_id = null) }
        database.channelEntityQueries.transaction {
            initial.forEach {
                database.channelEntityQueries.create(it)
            }
        }

        database.channelEntityQueries.deleteAll()
        val actual = database.channelEntityQueries.count().executeAsOne()
        assertEquals(0, actual.toInt())
    }

    @Test
    fun deleteAllWithImages() {
        var idCtr = 1L
        val initial = fixture<List<Channel>>().map {
            it.copy(
                id = 0,
                thumb_id = idCtr++,
                image_id = idCtr++
            )
        }
        var mapped = listOf<Channel>()
        database.imageEntityQueries.transaction {
            mapped = initial.map { channel ->
                database.imageEntityQueries.create(fixture<Image>().copy(id = channel.thumb_id!!))
                database.imageEntityQueries.create(fixture<Image>().copy(id = channel.image_id!!))
                database.channelEntityQueries.create(channel)
                channel.copy(
                    id = database.channelEntityQueries
                        .getInsertId()
                        .executeAsOne()
                )
            }
        }

        database.channelEntityQueries.deleteAll()
        mapped.forEach {
            database.imageEntityQueries
                .deleteByIds(
                    mapped.flatMap { listOf(it.image_id!!, it.thumb_id!!) }
                )
        }

        val actual = database.channelEntityQueries.count().executeAsOne()
        assertEquals(0, actual.toInt())
        val actualImageCount = database.imageEntityQueries.count().executeAsOne()
        assertEquals(0, actualImageCount.toInt())
    }

    @Test
    fun update() {
        val initial = fixture<Channel>().copy(id = 0, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
        val insertId = database.channelEntityQueries.getInsertId().executeAsOne()
        val update = fixture<Channel>().copy(id = insertId, image_id = null, thumb_id = null)
        database.channelEntityQueries.update(update)
        val actual = database.channelEntityQueries.load(insertId).executeAsOne()
        assertEquals(update, actual)
    }

    @Test
    fun createLoadEntityWithImages() {
        val initial = fixture<Channel>().copy(id = 0, image_id = 1, thumb_id = 2)
        val initialImage = fixture<Image>().copy(id = 1)
        val initialThumb = fixture<Image>().copy(id = 2)
        database.imageEntityQueries.create(initialImage)
        database.imageEntityQueries.create(initialThumb)
        database.channelEntityQueries.create(initial)
        val insertId = database.channelEntityQueries.getInsertId().executeAsOne()
        val actual = database.channelEntityQueries.load(insertId).executeAsOne()
        assertEquals(initial.copy(id = insertId), actual)
    }

    @Test
    fun findByPlatformId() {
        val initial = fixture<Channel>().copy(id = 0, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
        val insertId = database.channelEntityQueries.getInsertId().executeAsOne()
        val actual = database.channelEntityQueries
            .findByPlatformId(initial.platform_id, initial.platform)
            .executeAsOne()
        assertEquals(initial.copy(id = insertId), actual)
    }
}