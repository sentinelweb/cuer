package uk.co.sentinelweb.cuer.db.entity

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
import uk.co.sentinelweb.cuer.database.entity.Image
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.db.util.kotlinFixtureDefaultConfig
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChannelEntityTest : KoinTest {
    private val fixture = kotlinFixtureDefaultConfig

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
    private val guidCreator = GuidCreator()

    @Before
    fun before() {
        Database.Schema.create(get())
    }

    @Test
    fun createLoadEntity() {
        val guid = guidCreator.create()
        val initial = fixture<Channel>().copy(id = guid.value, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
        //val insertId = database.channelEntityQueries.getInsertId().executeAsOne()
        val actual = database.channelEntityQueries.load(guid.value).executeAsOne()
        assertEquals(initial, actual)
    }

    @Test
    fun loadAllChannels() {
        val initial =
            fixture<List<Channel>>().map { it.copy(id = guidCreator.create().value, image_id = null, thumb_id = null) }
        database.channelEntityQueries.transaction {
            initial.forEach {
                database.channelEntityQueries.create(it)
            }
        }
        val actual = database.channelEntityQueries.loadAll().executeAsList()
        assertEquals(initial.size, actual.size)
        actual.forEach { assertNotNull(it.id) }
    }

    @Test
    fun count() {
        val initial =
            fixture<List<Channel>>().map { it.copy(id = guidCreator.create().value, image_id = null, thumb_id = null) }
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
        val initial =
            fixture<List<Channel>>().map { it.copy(id = guidCreator.create().value, image_id = null, thumb_id = null) }
        database.channelEntityQueries.transaction {
            initial.forEach {
                database.channelEntityQueries.create(it)
            }
        }
        val idList = initial.map { it.id }
        val actual = database.channelEntityQueries
            .loadAllByIds(idList)
            .executeAsList()
        assertEquals(actual.size, idList.size)
        idList.forEach { id -> assertNotNull(actual.filter { it.id == id }) }
    }

    @Test
    fun createDeleteEntity() {
        val guid = guidCreator.create()
        val initial = fixture<Channel>().copy(id = guid.value, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
//        val insertId = database.channelEntityQueries
//            .getInsertId()
//            .executeAsOne()
        database.channelEntityQueries.delete(guid.value)
        val actual = database.channelEntityQueries
            .count()
            .executeAsOne()
        assertEquals(0, actual.toInt())
    }

    @Test
    fun findByChannelId() {
        val guid = guidCreator.create()
        val initial = fixture<Channel>().copy(id = guid.value, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
//        val insertId = database.channelEntityQueries
//            .getInsertId()
//            .executeAsOne()
        database.channelEntityQueries.findByPlatformId(initial.platform_id, initial.platform)
        val actual = database.channelEntityQueries
            .findByPlatformId(initial.platform_id, initial.platform)
            .executeAsOne()
        assertEquals(guid.value, actual.id)
    }

    @Test
    fun deleteByIds() {
        val initial =
            fixture<List<Channel>>().map { it.copy(id = guidCreator.create().value, image_id = null, thumb_id = null) }
        database.channelEntityQueries.transaction {
            initial.forEach {
                database.channelEntityQueries.create(it)
            }
        }

        val ids = initial.map { it.id }
        val deleteIdList = ids.take(3)
        database.channelEntityQueries.deleteByIds(deleteIdList)
        val actual = database.channelEntityQueries
            .count()
            .executeAsOne()
        assertEquals(2, actual.toInt())
        val remainIdList = ids.filter { !deleteIdList.contains(it) }
        val actualRemain = database.channelEntityQueries
            .loadAllByIds(remainIdList)
            .executeAsList()
        assertEquals(remainIdList.size, actualRemain.size)
        remainIdList.forEach { id -> assertNotNull(actualRemain.filter { it.id == id }) }
    }

    @Test
    fun deleteAll() {
        val initial =
            fixture<List<Channel>>().map { it.copy(id = guidCreator.create().value, image_id = null, thumb_id = null) }
        database.channelEntityQueries.transaction {
            initial.forEach {
                database.channelEntityQueries.create(it)
            }
        }

        database.channelEntityQueries.deleteAll()
        val actual = database.channelEntityQueries.count().executeAsOne()
        assertEquals(0, actual.toInt())
    }

//    @Test
//    fun deleteAllWithImages() {
//        var idCtr = 1L
//        val initial = fixture<List<Channel>>().map {
//            it.copy(
//                id = 0,
//                thumb_id = idCtr++,
//                image_id = idCtr++
//            )
//        }
//        var mapped = listOf<Channel>()
//        database.imageEntityQueries.transaction {
//            mapped = initial.map { channel ->
//                database.imageEntityQueries.create(fixture<Image>().copy(id = channel.thumb_id!!))
//                database.imageEntityQueries.create(fixture<Image>().copy(id = channel.image_id!!))
//                database.channelEntityQueries.create(channel)
//                channel.copy(
//                    id = database.channelEntityQueries
//                        .getInsertId()
//                        .executeAsOne()
//                )
//            }
//        }
//
//        database.channelEntityQueries.deleteAll()
//        mapped.forEach {
//            database.imageEntityQueries
//                .deleteByIds(
//                    mapped.flatMap { listOf(it.image_id!!, it.thumb_id!!) }
//                )
//        }
//
//        val actual = database.channelEntityQueries.count().executeAsOne()
//        assertEquals(0, actual.toInt())
//        val actualImageCount = database.imageEntityQueries.count().executeAsOne()
//        assertEquals(0, actualImageCount.toInt())
//    }

    @Test
    fun update() {
        val initial = fixture<Channel>().copy(id = guidCreator.create().value, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
        //val insertId = database.channelEntityQueries.getInsertId().executeAsOne()
        val update = fixture<Channel>().copy(id = initial.id, image_id = null, thumb_id = null)
        database.channelEntityQueries.update(update)
        val actual = database.channelEntityQueries.load(initial.id).executeAsOne()
        assertEquals(update, actual)
    }

    @Test
    fun createLoadEntityWithImages() {
        val initial =
            fixture<Channel>().copy(id = guidCreator.create().value, image_id = guidCreator.create().value, thumb_id = guidCreator.create().value)
        val initialImage = fixture<Image>().copy(id = initial.image_id!!)
        val initialThumb = fixture<Image>().copy(id = initial.thumb_id!!)
        database.imageEntityQueries.create(initialImage)
        database.imageEntityQueries.create(initialThumb)
        database.channelEntityQueries.create(initial)
        //val insertId = database.channelEntityQueries.getInsertId().executeAsOne()
        val actual = database.channelEntityQueries.load(initial.id).executeAsOne()
        assertEquals(initial.copy(id = initial.id), actual)
    }

    @Test
    fun findByPlatformId() {
        val initial = fixture<Channel>().copy(id = guidCreator.create().value, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(initial)
        //val insertId = database.channelEntityQueries.getInsertId().executeAsOne()
        val actual = database.channelEntityQueries
            .findByPlatformId(initial.platform_id, initial.platform)
            .executeAsOne()
        assertEquals(initial, actual)
    }
}