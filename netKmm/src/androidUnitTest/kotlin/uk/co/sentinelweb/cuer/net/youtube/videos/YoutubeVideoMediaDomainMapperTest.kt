package uk.co.sentinelweb.cuer.net.youtube.videos

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.decorator.optional.NeverOptionalStrategy
import com.appmattus.kotlinfixture.decorator.optional.optionalStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.ThumbnailDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeImageMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeVideoMediaDomainMapper

class YoutubeVideoMediaDomainMapperTest {
    private val fixture = kotlinFixture {
        nullabilityStrategy(NeverNullStrategy)
        optionalStrategy(NeverOptionalStrategy) {
            propertyOverride(YoutubeVideosDto.VideoDto::contentDetails, NeverOptionalStrategy)
        }
        factory { Identifier(GuidCreator().create(), fixture()) }
    }

    @MockK
    private lateinit var mockStampMapper: TimeStampMapper

    @MockK
    private lateinit var mockImageMapper: YoutubeImageMapper

    //@Fixture
    private lateinit var dto: YoutubeVideosDto

    //@Fixture
    private lateinit var fixtMedium: ThumbnailDto

    //@Fixture
    private lateinit var fixMaxRes: ThumbnailDto

    //@Fixture
    private lateinit var fixtMediumDomain: ImageDomain

    //@Fixture
    private lateinit var fixMaxResDomain: ImageDomain

    //@Fixture
    private lateinit var fixtDate: LocalDateTime

    private var fixtDuration: Long = 234324L

    private lateinit var sut: YoutubeVideoMediaDomainMapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
//        val fixture = JFixture()
//        fixture.customise().lazyInstance(Identifier::class.java) { Identifier(GuidCreator().create(), LOCAL) }
//
//        FixtureAnnotations.initFixtures(this, fixture)
        dto = fixture()
        fixtMedium = fixture()
        fixMaxRes = fixture()
        fixtMediumDomain = fixture()
        fixMaxResDomain = fixture()
        fixtDate = fixture()
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
        every { mockStampMapper.parseTimestamp(any<String>()) } returns fixtDate
        every { mockStampMapper.parseDuration(any()) } returns fixtDuration
        dto.items.forEach {
            every { mockImageMapper.mapThumb(it.snippet.thumbnails) } returns fixtMediumDomain
            every { mockImageMapper.mapImage(it.snippet.thumbnails) } returns fixMaxResDomain
        }
        sut = YoutubeVideoMediaDomainMapper(mockStampMapper, mockImageMapper, SystemLogWrapper())
    }

    @Test
    fun map() {
        val actual = sut.map(dto)

        actual.forEachIndexed { index, domain ->
            assertEquals(dto.items[index].snippet.title, domain.title)
            assertEquals(dto.items[index].snippet.description, domain.description)
            assertEquals(
                domain.channelData, ChannelDomain(
                    id = null,
                    platformId = dto.items[index].snippet.channelId,
                    title = dto.items[index].snippet.channelTitle,
                    platform = PlatformDomain.YOUTUBE
                )
            )
            assertEquals(fixtDate, domain.published)
            if (dto.items[index].contentDetails != null) {
                assertEquals(fixtDuration, domain.duration)
            } else {
                assertEquals(-1L, domain.duration)
            }
            assertEquals(dto.items[index].id, domain.platformId)
            assertEquals(MediaDomain.MediaTypeDomain.VIDEO, domain.mediaType)
            assertEquals(PlatformDomain.YOUTUBE, domain.platform)
            dto.items[index].snippet.thumbnails.medium!!
                .apply { assertEquals(fixtMediumDomain, domain.thumbNail) }
            dto.items[index].snippet.thumbnails.maxres!!
                .apply { assertEquals(fixMaxResDomain, domain.image) }
            assertNull(domain.dateLastPlayed)
            assertNull(domain.positon)
            assertNull(domain.id)
        }
    }
}