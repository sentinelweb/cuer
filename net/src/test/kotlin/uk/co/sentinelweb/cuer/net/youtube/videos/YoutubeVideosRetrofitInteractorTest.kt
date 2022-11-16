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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextTestProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeChannelsDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeChannelDomainMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubePlaylistDomainMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeSearchMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeVideoMediaDomainMapper

// todo test update and channels
@Ignore("enable with ktor") // fixme
class YoutubeVideosRetrofitInteractorTest {

    @MockK
    private lateinit var mockService: YoutubeService

    @MockK
    private lateinit var mockKeyProvider: ApiKeyProvider

    @MockK
    private lateinit var mockVideoMapper: YoutubeVideoMediaDomainMapper

    @MockK
    private lateinit var mockChannelMapper: YoutubeChannelDomainMapper

    @MockK
    private lateinit var mockPlaylistDomainMapper: YoutubePlaylistDomainMapper

    @MockK
    private lateinit var mockSearchMapper: YoutubeSearchMapper

    @MockK
    private lateinit var mockErrorMapper: ErrorMapper

    @MockK
    private lateinit var mockLog: LogWrapper

    @Fixture
    private lateinit var videosDto: YoutubeVideosDto

    @Fixture
    private lateinit var videosDomain: List<MediaDomain>

    @Fixture
    private lateinit var channelsDto: YoutubeChannelsDto

    @Fixture
    private lateinit var channelsDomain: List<ChannelDomain>


    private val fixtIds = listOf("8nhPVOM97Jg", "fY7M3pzXdUo")
    private val fixtParts = listOf(ID, SNIPPET, CONTENT_DETAILS, PLAYER)

    private val connectivityWrapper = object : ConnectivityWrapper {
        override fun isConnected() = true
        override fun isMetered() = true
    }

    private lateinit var sut: YoutubeInteractor

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        FixtureAnnotations.initFixtures(this)

        every { mockVideoMapper.map(videosDto) } returns videosDomain
        every { mockChannelMapper.map(channelsDto) } returns channelsDomain
        every { mockErrorMapper.log } returns mockLog
        every { mockKeyProvider.key } returns "key"
        coEvery {
            mockService.getVideoInfos(
                fixtIds.joinToString(","),
                fixtParts.map { it.part }.joinToString(","),
                "key"
            )
        } returns videosDto
        coEvery {
            mockService.getChannelInfos(
                fixtIds.joinToString(","),
                fixtParts.map { it.part }.joinToString(","),
                "key"
            )
        } returns channelsDto

        sut = YoutubeRetrofitInteractor(
            service = mockService,
            keyProvider = mockKeyProvider,
            videoMapper = mockVideoMapper,
            channelMapper = mockChannelMapper,
            coContext = CoroutineContextTestProvider(),
            errorMapper = mockErrorMapper,
            connectivity = connectivityWrapper,
            playlistMapper = mockPlaylistDomainMapper,
            searchMapper = mockSearchMapper
        )
    }

    @Test
    fun videos() {
        every {
            mockErrorMapper.map<List<MediaDomain>>(any(), any())
        } returns NetResult.Error(Exception("error"))
        runBlocking {
            val actual = sut.videos(fixtIds, fixtParts)

            assertTrue(actual.isSuccessful)
            assertEquals(actual.data, videosDomain)
            verify { mockVideoMapper.map(videosDto) }
        }
    }

    @Test
    fun channels() {
        every {
            mockErrorMapper.map<List<ChannelDomain>>(any(), any())
        } returns NetResult.Error(Exception("error"))
        runBlocking {
            val actual = sut.channels(fixtIds, fixtParts)

            assertTrue(actual.isSuccessful)
            assertEquals(actual.data, channelsDomain)
            verify { mockChannelMapper.map(channelsDto) }
        }
    }
}
