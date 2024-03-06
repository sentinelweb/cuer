package uk.co.sentinelweb.cuer.app.usecase

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.app.service.update.UpdateServiceContract
import uk.co.sentinelweb.cuer.app.usecase.PlaylistUpdateUsecase.Companion.SECONDS
import uk.co.sentinelweb.cuer.app.usecase.PlaylistUpdateUsecase.UpdateResult.Result
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProviderImpl
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.tools.ext.generatePlaylist
import kotlin.time.Duration.Companion.seconds

class PlaylistUpdateUsecaseTest {
    private val fixture = kotlinFixture {
        nullabilityStrategy(NeverNullStrategy)
        repeatCount { 6 }
        factory { OrchestratorContract.Identifier(GuidCreator().create(), fixture()) }
        // repeatCount(PlaylistDomain::items) { 7 }
    }

    @MockK
    lateinit var playlistOrchestrator: OrchestratorContract<PlaylistDomain>

    @MockK
    lateinit var playlistItemOrchestrator: OrchestratorContract<PlaylistItemDomain>

    @MockK
    lateinit var mediaOrchestrator: OrchestratorContract<MediaDomain>

    @MockK
    lateinit var playlistMediaLookupUsecase: PlaylistMediaLookupUsecase

    @MockK
    lateinit var timeProvider: TimeProvider

    val updateChecker: PlaylistUpdateUsecase.UpdateCheck = PlaylistUpdateUsecase.PlatformUpdateCheck()

    @MockK
    lateinit var updateServiceManager: UpdateServiceContract.Manager

    private val log = SystemLogWrapper()
    private val timeProviderTest: TimeProvider = TimeProviderImpl()

    private lateinit var sut: PlaylistUpdateUsecase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sut = PlaylistUpdateUsecase(
            playlistOrchestrator,
            playlistItemOrchestrator,
            mediaOrchestrator,
            playlistMediaLookupUsecase,
            timeProvider,
            log,
            updateChecker,
            updateServiceManager
        )
    }

    @Test
    fun update_must_be_platform() = runTest {
        val playlist = generatePlaylist(fixture).copy(type = USER)

        val actual = sut.update(playlist)

        assertEquals(actual.status, Result.Failure)
    }


    @Test
    fun update_descending_playlist_must_add_at_top_in_reverse_order() = runTest {
        val platformId = "update_platformId"
        val tz = TimeZone.UTC
        val localtime = timeProviderTest.localDateTime()
        val existingPlaylist = generatePlaylist(fixture).let { pl ->
            pl.copy(
                //id = 1,
                type = PLATFORM,
                platform = PlatformDomain.YOUTUBE,
                platformId = platformId,
                items = pl.items.mapIndexed { i, item ->
                    item.copy(
                        order = (pl.items.size - i + 7) * SECONDS,
                        media = item.media.copy(
                            published = localtime.toInstant(tz).plus(-i.seconds).toLocalDateTime(tz)
                        )
                    )
                }
            )
        }
        //log.d("existingPlaylist: ${existingPlaylist.items.size}")
        val updatedPlatformPlaylist = generatePlaylist(fixture).let { pl ->
            pl.copy(
                id = null,
                type = PLATFORM,
                platform = PlatformDomain.YOUTUBE,
                platformId = platformId,
                items = existingPlaylist.items.plus(pl.items)
            )
        }
        log.d("updatedPlatformPlaylist: ${updatedPlatformPlaylist.items.size}")
        log.d(updatedPlatformPlaylist.items.map { it.media.platformId }.joinToString(","))

        coEvery {
            playlistOrchestrator.loadByPlatformId(platformId, Source.PLATFORM.deepOptions())
        } returns updatedPlatformPlaylist

        coEvery {
            mediaOrchestrator.loadList(
                PlatformIdListFilter(updatedPlatformPlaylist.items.map { it.media.platformId }),
                LOCAL.flatOptions()
            )
        } returns existingPlaylist.items.map { it.media }

        coEvery { playlistMediaLookupUsecase.lookupMediaAndReplace(any()) } answers { firstArg() }
        coEvery {
            playlistItemOrchestrator.save(any<List<PlaylistItemDomain>>(), LOCAL.deepOptions())
        } answers { firstArg() }

        val actual = sut.update(existingPlaylist)

        log.d("actual; ${actual.status} ${actual.newItems?.size}")
        assertEquals(actual.status, Result.Success)
        assertEquals(6, actual.newItems?.size)
        // existing order is:: minOrder: 8000 maxOrder: 13000 orderIsAscending: false
        // so least recent is 7000 and most recent is 2000
        actual.newItems?.forEachIndexed { i, item ->
            assertEquals(8 * SECONDS - ((6 - i) * SECONDS), item.order)
        }
    }

    @Test
    fun update_ascending_playlist_must_add_at_bottom_in_order() = runTest {
        val platformId = "update_platformId"
        val tz = TimeZone.UTC
        val localtime = timeProviderTest.localDateTime()
        val existingPlaylist = generatePlaylist(fixture).let { pl ->
            pl.copy(
                type = PLATFORM,
                platform = PlatformDomain.YOUTUBE,
                platformId = platformId,
                items = pl.items.mapIndexed { i, item ->
                    item.copy(
                        order = i * SECONDS,
                        media = item.media.copy(published = localtime.toInstant(tz).plus(i.seconds).toLocalDateTime(tz))
                    )
                }
            )
        }
        log.d("canupdate: ${sut.canUpdate(existingPlaylist)}")
        val updatedPlatformPlaylist = generatePlaylist(fixture).let { pl ->
            pl.copy(
                id = null,
                type = PLATFORM,
                platform = PlatformDomain.YOUTUBE,
                platformId = platformId,
                items = existingPlaylist.items.plus(pl.items)
            )
        }
        //log.d("updatedPlatformPlaylist: ${updatedPlatformPlaylist.items.size}")
        //log.d(updatedPlatformPlaylist.items.map { it.media.platformId }.joinToString(","))

        coEvery {
            playlistOrchestrator.loadByPlatformId(platformId, Source.PLATFORM.deepOptions())
        } returns updatedPlatformPlaylist

        coEvery {
            mediaOrchestrator.loadList(
                PlatformIdListFilter(updatedPlatformPlaylist.items.map { it.media.platformId }),
                LOCAL.flatOptions()
            )
        } returns existingPlaylist.items.map { it.media }

        coEvery { playlistMediaLookupUsecase.lookupMediaAndReplace(any()) } answers { firstArg() }
        coEvery {
            playlistItemOrchestrator.save(any<List<PlaylistItemDomain>>(), LOCAL.deepOptions())
        } answers { firstArg() }

        val actual = sut.update(existingPlaylist)

        log.d("actual; $actual")
        assertEquals(actual.status, Result.Success)
        assertEquals(6, actual.newItems?.size)
        // existing order is:: minOrder: 8000 maxOrder: 13000 orderIsAscending: false
        // so least recent is 7000 and most recent is 2000
        actual.newItems?.forEachIndexed { i, item ->
            assertEquals(5 * SECONDS + ((i + 1) * SECONDS), item.order)
        }
    }

    @Test
    fun update_liveupcoming_starts_service() = runTest {
        val existingPlaylist = generatePlaylist(fixture).let { pl ->
            pl.copy(
                id = PlaylistMemoryRepository.MemoryPlaylist.LiveUpcoming.identifier(),
                type = APP,
                platform = null,
                platformId = null,
            )
        }

        val actual = sut.update(existingPlaylist)

        log.d("actual; ${actual.status} ${actual.newItems?.size}")
        assertEquals(actual.status, Result.Pending)
        assertEquals(-1, actual.numberItems)
        coVerify { updateServiceManager.start() }
    }

}
