package uk.co.sentinelweb.cuer.net.youtube.videos

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.core.mappers.DateTimeMapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto
import java.time.LocalDateTime

class YoutubeVideoMediaDomainMapperTest {
    @MockK
    private lateinit var mockDateMapper: DateTimeMapper

    @Fixture
    private lateinit var dto: YoutubeVideosDto

    @Fixture
    private lateinit var fixtDate: LocalDateTime
    private var fixtDuration: Long = 234324L

    private lateinit var sut: YoutubeVideoMediaDomainMapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        FixtureAnnotations.initFixtures(this)
        every { mockDateMapper.mapTimestamp(any()) } returns fixtDate
        every { mockDateMapper.mapDuration(any()) } returns fixtDuration
        sut = YoutubeVideoMediaDomainMapper(mockDateMapper)
    }

    @Test
    fun map() {
        val actual = sut.map(dto)

        actual.forEachIndexed { index, domain ->
            assertEquals(domain.title, dto.items[index].snippet?.title)
            assertEquals(domain.description, dto.items[index].snippet?.description)
            assertEquals(
                domain.channelData, ChannelDomain(
                    remoteId = dto.items[index].snippet?.channelId ?: "",
                    title = dto.items[index].snippet?.channelTitle ?: ""
                )
            )
            assertEquals(domain.published, fixtDate)
            assertEquals(domain.duration, fixtDuration)
            assertEquals(domain.mediaId, dto.items[index].id)
            assertEquals(domain.mediaType, MediaDomain.MediaTypeDomain.VIDEO)
            assertEquals(domain.platform, PlatformDomain.YOUTUBE)
            dto.items[index].snippet!!.thumbnails.medium!!.apply {
                assertEquals(domain.thumbNail, ImageDomain(url, width, height))
            }// ?: throw Exception("thumb test data broken")
            dto.items[index].snippet!!.thumbnails.maxres!!.apply {
                assertEquals(domain.image, ImageDomain(url, width, height))
            }// ?: throw Exception("Image test data broken")
            assertNull(domain.dateLastPlayed)
            assertNull(domain.positon)
            assertNull(domain.id)
        }
    }
}