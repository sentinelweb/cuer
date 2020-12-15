package uk.co.sentinelweb.cuer.net.youtube.videos

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.JFixture
import com.flextrade.jfixture.annotations.Fixture
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto
import java.time.LocalDateTime

class YoutubeVideoMediaDomainMapperTest {
    @MockK
    private lateinit var mockStampMapper: TimeStampMapper

    @Fixture
    private lateinit var dto: YoutubeVideosDto

    @Fixture
    private lateinit var fixtMedium: YoutubeVideosDto.VideoDto.SnippetDto.ThumbnailsDto.ThumbnailDto

    @Fixture
    private lateinit var fixMaxRes: YoutubeVideosDto.VideoDto.SnippetDto.ThumbnailsDto.ThumbnailDto

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
                snippet = it.snippet!!.copy(
                    thumbnails = it.snippet!!.thumbnails.copy(
                        medium = fixtMedium,
                        maxres = fixMaxRes
                    )
                )
            )
        })
        every { mockStampMapper.mapTimestamp(any<String>()) } returns fixtDate
        every { mockStampMapper.mapDuration(any()) } returns fixtDuration
        sut = YoutubeVideoMediaDomainMapper(mockStampMapper)
    }

    @Test
    // fixme test fails!
    fun map() {
        val actual = sut.map(dto)

        actual.forEachIndexed { index, domain ->
            assertEquals(domain.title, dto.items[index].snippet?.title)
            assertEquals(domain.description, dto.items[index].snippet?.description)
            assertEquals(
                domain.channelData, ChannelDomain(
                    platformId = dto.items[index].snippet?.channelId ?: "",
                    title = dto.items[index].snippet?.channelTitle ?: "",
                    platform = PlatformDomain.YOUTUBE
                )
            )
            assertEquals(domain.published, fixtDate)
            assertEquals(domain.duration, fixtDuration)
            assertEquals(domain.platformId, dto.items[index].id)
            assertEquals(domain.mediaType, MediaDomain.MediaTypeDomain.VIDEO)
            assertEquals(domain.platform, PlatformDomain.YOUTUBE)
            dto.items[index].snippet!!.thumbnails.medium!!.apply {
                assertEquals(
                    domain.thumbNail,
                    ImageDomain(fixtMedium.url, fixtMedium.width, fixtMedium.height)
                )
            }
            dto.items[index].snippet!!.thumbnails.maxres!!.apply {
                assertEquals(
                    domain.image,
                    ImageDomain(fixMaxRes.url, fixMaxRes.width, fixMaxRes.height)
                )
            }
            assertNull(domain.dateLastPlayed)
            assertNull(domain.positon)
            assertNull(domain.id)
        }
    }
}