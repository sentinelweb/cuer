package uk.co.sentinelweb.cuer.net.youtube.videos

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.KoinComponent
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProviderTest
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService
import uk.co.sentinelweb.cuer.net.youtube.YoutubeVideosInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

class YoutubeVideosRetrofitInteractorTest : KoinComponent {

    @MockK
    private lateinit var mockService: YoutubeService

    @MockK
    private lateinit var mockKeyProvider: YoutubeApiKeyProvider

    @MockK
    private lateinit var mockMapper: YoutubeVideoMediaDomainMapper

    @Fixture
    private lateinit var dto: YoutubeVideosDto

    @Fixture
    private lateinit var domain: List<MediaDomain>
    private val fixtIds = listOf("8nhPVOM97Jg", "fY7M3pzXdUo")
    private val fixtParts = listOf(ID, SNIPPET, CONTENT_DETAILS, PLAYER)

    private lateinit var sut: YoutubeVideosInteractor

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        FixtureAnnotations.initFixtures(this)

        every { mockMapper.map(dto) } returns domain
        every { mockKeyProvider.key } returns "key"
        coEvery {
            mockService.getVideoInfos(
                fixtIds.joinToString(","),
                fixtParts.map { it.part }.joinToString(","),
                "key"
            )
        } returns dto

        sut = YoutubeVideosRetrofitInteractor(
            service = mockService,
            keyProvider = mockKeyProvider,
            mapper = mockMapper,
            coContext = CoroutineContextProviderTest()
        )
    }

    @Test
    fun videos() {
        runBlocking {
            val actual = sut.videos(fixtIds, fixtParts)

            assertEquals(actual, domain)
            verify { mockMapper.map(dto) }
        }
    }
}
