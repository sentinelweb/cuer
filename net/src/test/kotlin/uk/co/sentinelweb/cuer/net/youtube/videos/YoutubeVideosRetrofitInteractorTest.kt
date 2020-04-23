package uk.co.sentinelweb.cuer.net.youtube.videos

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.koin.core.KoinComponent
import uk.co.sentinelweb.cuer.net.youtube.YoutubeVideosInteractor
import uk.co.sentinelweb.cuer.net.retrofit.RetrofitBuilder
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService

class YoutubeVideosRetrofitInteractorTest : KoinComponent {
    private val useReal = false

    private lateinit var mockService: YoutubeService

    private var keyProvider = object : YoutubeApiKeyProvider {
        override val key: String = "xxx" //System.getProperty("CUER_YOUTUBE_API_KEY")
    }

    private lateinit var sut: YoutubeVideosInteractor

    @Before
    fun setUp() {
        if (useReal) { // todo remove this
            mockService = RetrofitBuilder().let { it.buildYoutubeService(it.buildYoutubeClient()) }
        } else {
            mockService = mockk()
        }
        sut = YoutubeVideosRetrofitInteractor(
            service = mockService,
            keyProvider = keyProvider,
            mapper = YoutubeVideoMediaDomainMapper()
        )
    }

    @Test
    fun videos() {
        runBlocking {
            val actual = sut.videos(
                listOf("8nhPVOM97Jg", "fY7M3pzXdUo"),
                listOf(ID, SNIPPET, CONTENT_DETAILS, PLAYER)
            )
        }
    }
}
