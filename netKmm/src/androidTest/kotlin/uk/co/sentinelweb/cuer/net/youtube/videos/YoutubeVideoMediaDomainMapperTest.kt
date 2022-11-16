package uk.co.sentinelweb.cuer.net.youtube.videos

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.JFixture
import com.flextrade.jfixture.annotations.Fixture
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.net.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.ThumbnailDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeImageMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeVideoMediaDomainMapper

// @Ignore("enable with ktor") // fixme
class YoutubeVideoMediaDomainMapperTest {
    @MockK
    private lateinit var mockStampMapper: TimeStampMapper

    @MockK
    private lateinit var mockImageMapper: YoutubeImageMapper

    @Fixture
    private lateinit var dto: YoutubeVideosDto

    @Fixture
    private lateinit var fixtMedium: ThumbnailDto

    @Fixture
    private lateinit var fixMaxRes: ThumbnailDto

    @Fixture
    private lateinit var fixtMediumDomain: ImageDomain

    @Fixture
    private lateinit var fixMaxResDomain: ImageDomain

    @Fixture
    private lateinit var fixtDate: LocalDateTime

    private var fixtDuration: Long = 234324L

    private lateinit var sut: YoutubeVideoMediaDomainMapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        val fixture = JFixture()
        FixtureAnnotations.initFixtures(this, fixture)

        dto = dto.copy(items = dto.items.map {
            it.copy(
                snippet = it.snippet.copy(
                    thumbnails = it.snippet.thumbnails.copy(
                        medium = fixtMedium,
                        maxres = fixMaxRes
                    )
                )
            )
        })
        every { mockStampMapper.mapTimestamp(any<String>()) } returns fixtDate
        every { mockStampMapper.mapDuration(any()) } returns fixtDuration
        dto.items.forEach {
            every { mockImageMapper.mapThumb(it.snippet.thumbnails) } returns fixtMediumDomain
            every { mockImageMapper.mapImage(it.snippet.thumbnails) } returns fixMaxResDomain
        }
        sut = YoutubeVideoMediaDomainMapper(mockStampMapper, mockImageMapper)
    }

    @Test
    fun map() {
        val actual = sut.map(dto)

        actual.forEachIndexed { index, domain ->
            assertEquals(domain.title, dto.items[index].snippet.title)
            assertEquals(domain.description, dto.items[index].snippet.description)
            assertEquals(
                domain.channelData, ChannelDomain(
                    platformId = dto.items[index].snippet.channelId,
                    title = dto.items[index].snippet.channelTitle,
                    platform = PlatformDomain.YOUTUBE
                )
            )
            assertEquals(domain.published, fixtDate)
            assertEquals(domain.duration, fixtDuration)
            assertEquals(domain.platformId, dto.items[index].id)
            assertEquals(domain.mediaType, MediaDomain.MediaTypeDomain.VIDEO)
            assertEquals(domain.platform, PlatformDomain.YOUTUBE)
            dto.items[index].snippet.thumbnails.medium!!.apply {
                assertEquals(
                    domain.thumbNail,
                    fixtMediumDomain
                )
            }
            dto.items[index].snippet.thumbnails.maxres!!.apply {
                assertEquals(
                    domain.image,
                    fixMaxResDomain
                )
            }
            assertNull(domain.dateLastPlayed)
            assertNull(domain.positon)
            assertNull(domain.id)
        }
    }
}