package uk.co.sentinelweb.cuer.net.youtube.videos

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.koin.core.KoinComponent
import uk.co.sentinelweb.cuer.core.mappers.DateTimeMapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.net.retrofit.RetrofitBuilder
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService
import uk.co.sentinelweb.cuer.net.youtube.YoutubeVideosInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

// this runs the real API for manual testing (keep test ignored)
class YoutubeVideosRetrofitInteractorApiTest : KoinComponent {

    private lateinit var mockService: YoutubeService

    private var keyProvider = object : YoutubeApiKeyProvider {
        // todo paste key or get system property to work
        override val key: String = "xxx" //System.getProperty("CUER_YOUTUBE_API_KEY")
    }

    private lateinit var sut: YoutubeVideosInteractor

    @Before
    fun setUp() {
        mockService = RetrofitBuilder().let { it.buildYoutubeService(it.buildYoutubeClient()) }

        sut = YoutubeVideosRetrofitInteractor(
            service = mockService,
            keyProvider = keyProvider,
            mapper = YoutubeVideoMediaDomainMapper(DateTimeMapper()),
            coContext = CoroutineContextProvider()
        )
    }

    @Ignore("Real api test .. run manually only")
    @Test
    fun videos() {
        runBlocking {
            sut.videos(
                listOf("8nhPVOM97Jg", "fY7M3pzXdUo"),
                listOf(ID, SNIPPET, CONTENT_DETAILS, PLAYER)
            )
        }
    }
}
