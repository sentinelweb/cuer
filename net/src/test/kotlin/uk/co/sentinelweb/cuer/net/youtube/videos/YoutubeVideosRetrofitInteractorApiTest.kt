package uk.co.sentinelweb.cuer.net.youtube.videos

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.koin.core.KoinComponent
import uk.co.sentinelweb.cuer.core.mappers.DateTimeMapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper
import uk.co.sentinelweb.cuer.net.retrofit.RetrofitBuilder
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*


/**
 * This runs the real API for manual testing (keep test ignored)
 *
 * the system property only gets picked up when running from command line
 *
 * TO RUN FROM AS : set -DCUER_YOUTUBE_API_KEY=api in configuration
 */
class YoutubeVideosRetrofitInteractorApiTest : KoinComponent {

    private lateinit var service: YoutubeService

    private var keyProvider = object : YoutubeApiKeyProvider {
        override val key: String = System.getProperty("CUER_YOUTUBE_API_KEY")
    }

    private lateinit var sut: YoutubeInteractor

    @Before
    fun setUp() {
        service = RetrofitBuilder().let { it.buildYoutubeService(it.buildYoutubeClient(true)) }

        sut = YoutubeRetrofitInteractor(
            service = service,
            keyProvider = keyProvider,
            videoMapper = YoutubeVideoMediaDomainMapper(DateTimeMapper()),
            channelMapper = YoutubeChannelDomainMapper(DateTimeMapper()),
            coContext = CoroutineContextProvider(),
            errorMapper = ErrorMapper(SystemLogWrapper())
        )
    }

    @Ignore("Real api test .. run manually only")
    @Test
    fun videos() {
        runBlocking {
            val actual = sut.videos(
                listOf("8nhPVOM97Jg", "fY7M3pzXdUo"),
                listOf(ID, SNIPPET, CONTENT_DETAILS, PLAYER)
            )
            assertNotNull(actual.data)
            assertEquals("8nhPVOM97Jg", actual.data!![0].platformId)
            assertEquals("fY7M3pzXdUo", actual.data!![1].platformId)
        }
    }

    @Ignore("Real api test .. run manually only")
    @Test
    fun channels() {
        runBlocking {
            val actual = sut.channels(
                listOf("UCzuqE7-t13O4NIDYJfakrhw", "UC2UIXt4VQnhQ-VZM4P1bUMQ"),
                listOf(ID, SNIPPET)
            )
            assertNotNull(actual.data)
            // note the items come out of order
            assertEquals("UCzuqE7-t13O4NIDYJfakrhw", actual.data!![1].id)
            assertEquals("UC2UIXt4VQnhQ-VZM4P1bUMQ", actual.data!![0].id)
        }
    }
}
