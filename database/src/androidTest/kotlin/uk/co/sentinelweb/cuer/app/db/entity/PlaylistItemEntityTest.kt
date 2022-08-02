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
import uk.co.sentinelweb.cuer.app.db.*
import uk.co.sentinelweb.cuer.app.db.di.DatabaseModule
import uk.co.sentinelweb.cuer.app.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.app.db.util.MainCoroutineRule
import kotlin.test.assertEquals

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
        val channel = fixture<Channel>().copy(id = 0, image_id = null, thumb_id = null)
        database.channelEntityQueries.createChannel(channel)
        val channelId = database.channelEntityQueries.getInsertIdChannel().executeAsOne()
        val media = fixture<Media>().copy(id = 0, channel_id = channelId, image_id = null, thumb_id = null)
        database.mediaEntityQueries.createMedia(media)
        val mediaId = database.mediaEntityQueries.getInsertIdMedia().executeAsOne()
        val playlist = fixture<Playlist>().copy(id = 0, parent_id = null, channel_id = null, image_id = null, thumb_id = null)
        database.playlistEntityQueries.createPlaylist(playlist)
        val playlistId = database.playlistEntityQueries.getInsertIdPlaylist().executeAsOne()

        val initial = fixture<Playlist_item>().copy(id = 0, media_id = mediaId, playlist_id = playlistId)
        database.playlistItemEntityQueries.createPlaylistItem(initial)
        val insertId = database.playlistItemEntityQueries.getInsertIdPlaylistItem().executeAsOne()
        val actual = database.playlistItemEntityQueries.loadPlaylistItem(1).executeAsOne()
        assertEquals(initial.copy(id = insertId), actual)
    }
}