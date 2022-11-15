package uk.co.sentinelweb.cuer.net.youtube.videos

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.NetModuleConfig
import uk.co.sentinelweb.cuer.net.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper
import uk.co.sentinelweb.cuer.net.retrofit.RetrofitBuilder
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.*


/**
 * This runs the real API for manual testing (keep test ignored)
 *
 * the system property only gets picked up when running from command line
 *
 * TO RUN FROM AS : set -DCUER_YOUTUBE_API_KEY=api in configuration
 */
class YoutubeVideosRetrofitInteractorApiTest {

    private lateinit var service: YoutubeService

    private var keyProvider = object : ApiKeyProvider {
        override val key: String = System.getProperty("CUER_YOUTUBE_API_KEY")
    }

    private val log = SystemLogWrapper()
    private val config = NetModuleConfig(debug = true)
    private val connectivityWrapper = object : ConnectivityWrapper {
        override fun isConnected() = true
        override fun isMetered() = true
    }
    private val imageMapper = YoutubeImageMapper()
    private lateinit var sut: YoutubeInteractor

    @Before
    fun setUp() {
        service = RetrofitBuilder(config, SystemLogWrapper()).let { it.buildYoutubeService(it.buildYoutubeClient()) }

        val channelMapper = YoutubeChannelDomainMapper(TimeStampMapper(log), imageMapper)
        sut = YoutubeRetrofitInteractor(
            service = service,
            keyProvider = keyProvider,
            videoMapper = YoutubeVideoMediaDomainMapper(TimeStampMapper(log), imageMapper),
            channelMapper = channelMapper,
            coContext = CoroutineContextProvider(),
            errorMapper = ErrorMapper(SystemLogWrapper()),
            connectivity = connectivityWrapper,
            playlistMapper = YoutubePlaylistDomainMapper(
                TimeStampMapper(log),
                PlaylistItemCreator(TimeProvider()),
                imageMapper,
                channelMapper,
                SystemLogWrapper()
            ),
            searchMapper = YoutubeSearchMapper(
                timeStampMapper = TimeStampMapper(log),
                timeProvider = TimeProvider(),
                itemCreator = PlaylistItemCreator(TimeProvider()),
                imageMapper = imageMapper,
                channelMapper = channelMapper
            )
        )
    }

    @Ignore("Real api test .. run manually only")
    @Test
    fun videos() = runBlocking {
        val actual = sut.videos(
            listOf(
                "8nhPVOM97Jg",
                "fY7M3pzXdUo",//live broadcast
                "9umtumSsm7U",
                "Mz5fgoUrJf4",
            ),
            listOf(ID, SNIPPET, CONTENT_DETAILS, PLAYER)
        )

        assertTrue(actual.isSuccessful)
        assertNotNull(actual.data)
        assertEquals("8nhPVOM97Jg", actual.data!![0].platformId)
        assertEquals("fY7M3pzXdUo", actual.data!![1].platformId)
        assertFalse(actual.data!![1].isLiveBroadcast)
        println(actual.data!![3])
    }

    @Ignore("Real api test .. run manually only")
    @Test
    fun channels() = runBlocking {
        val actual = sut.channels(
            listOf("UCzuqE7-t13O4NIDYJfakrhw", "UC2UIXt4VQnhQ-VZM4P1bUMQ"),
            listOf(ID, SNIPPET)
        )

        assertTrue(actual.isSuccessful)
        assertNotNull(actual.data)
        // note the items come out of order
        assertEquals("UCzuqE7-t13O4NIDYJfakrhw", actual.data!![0].platformId)
        assertEquals("UC2UIXt4VQnhQ-VZM4P1bUMQ", actual.data!![1].platformId)
    }


    @Ignore("Real api test .. run manually only")
    @Test
    fun playlist() = runBlocking {
        val actual = sut.playlist(// long 170 items
            "PLf-zrdqNE8p-7tt7JHtlpLH3w9QA1EU0L"
        )

        assertTrue(actual.isSuccessful)
        assertNotNull(actual.data)
        // note the items come out of order
        assertEquals(168, actual.data!!.items.size)

        val actualLive = sut.playlist(// live - news
            "PLf-zrdqNE8p9qjU-kzB8ROMpgbXYahCQR"
        )

        assertTrue(actualLive.isSuccessful)
        assertNotNull(actualLive.data)
        // note the items come out of order
        assertEquals(10, actualLive.data!!.items.size)
        assertTrue(actualLive.data!!.items[0].media.isLiveBroadcast)
    }


    //@Ignore("Real api test .. run manually only")
    @Test
    fun search() = runBlocking {
        val actual = sut.search(// long 170 items
            SearchRemoteDomain("heidegger")
        )

        assertTrue(actual.isSuccessful)
        assertNotNull(actual.data)
        // note the items come out of order
        assertEquals(50, actual.data!!.items.size)

    }

}
