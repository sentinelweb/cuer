package uk.co.sentinelweb.cuer.app.ui.upcoming

import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.LiveUpcomingMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingPresenter.Companion.MAX_RECORDS
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider.Companion.toLocalDateTime
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.tools.ext.kotlinFixtureDefaultConfig
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule
import kotlin.time.Duration.Companion.minutes

class UpcomingPresenterTest : KoinTest {
    private val fixture = kotlinFixtureDefaultConfig

    // todo fix flakiness (from fixture)
//    @get:Rule
//    var flakyTestRule = FlakyTestRule(5)

    @get:Rule
    var rule = CoroutineTestRule()

    private val view: UpcomingContract.View = mockk(relaxed = true)
    private val playlistItemOrchestrator: OrchestratorContract<PlaylistItemDomain> =
        mockk(relaxed = true) //OrchestratorContract<PlaylistItemDomain>
    private val mediaOrchestrator: OrchestratorContract<MediaDomain> =
        mockk(relaxed = true) //OrchestratorContract<PlaylistItemDomain>
    private val timeProvider: TimeProvider = mockk(relaxed = true)
    private val log: LogWrapper = SystemLogWrapper()
    private val testCoroutines = TestCoroutineContextProvider(rule.dispatcher)

    lateinit var sut: UpcomingPresenter

    init {
        log.tag(this)
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        sut = UpcomingPresenter(
            view,
            playlistItemOrchestrator,
            mediaOrchestrator,
            testCoroutines,
            timeProvider,
            log
        )
    }

    @Test
    fun `GIVEN live_upcoming list WHEN checking upcoming THEN notification is displayed for items within time limit`() =
        runTest {
            val now = Clock.System.now()
            every { timeProvider.instant() } returns now
            val allUpcomingList: List<PlaylistItemDomain> = fixture<List<PlaylistItemDomain>>()
                .mapIndexed { i, it ->
                    it.copy(
                        media = it.media.copy(broadcastDate = now.plus((i * 10 - 1).minutes).toLocalDateTime())
                    )
                }
            coEvery {
                playlistItemOrchestrator.loadList(LiveUpcomingMediaFilter(MAX_RECORDS), LOCAL.deepOptions())
            } returns allUpcomingList
            allUpcomingList.forEachIndexed { i, item -> log.d("$i - ${item.media.broadcastDate}") }
            sut.checkForUpcomingEpisodes(30)
            verify(exactly = 0) { view.showNotification(allUpcomingList[0]) }
            verify(exactly = 1) { view.showNotification(allUpcomingList[1]) }
            verify(exactly = 1) { view.showNotification(allUpcomingList[2]) }
            verify(exactly = 1) { view.showNotification(allUpcomingList[3]) }
            verify(exactly = 0) { view.showNotification(allUpcomingList[4]) }
            verify(exactly = 0) { view.showNotification(allUpcomingList[5]) }
        }

    @Test
    fun `GIVEN live_upcoming list WHEN checking upcoming THEN old items are marked not live or upcoming`() = runTest {
        val now = Clock.System.now()
        every { timeProvider.instant() } returns now
        val allUpcomingList: List<PlaylistItemDomain> = fixture<List<PlaylistItemDomain>>()
            .mapIndexed { i, it ->
                it.copy(
                    media = it.media.copy(broadcastDate = now.plus((i * 10 - 21).minutes).toLocalDateTime())
                )
            }
        coEvery {
            playlistItemOrchestrator.loadList(LiveUpcomingMediaFilter(MAX_RECORDS), LOCAL.deepOptions())
        } returns allUpcomingList
        allUpcomingList.forEachIndexed { i, item -> log.d("$i - ${item.media.broadcastDate}") }
        sut.checkForUpcomingEpisodes(30)

        coVerify(exactly = 1) {
            mediaOrchestrator
                .save(listOf(0, 1, 2).map {
                    allUpcomingList[it].media.copy(
                        isLiveBroadcast = false,
                        isLiveBroadcastUpcoming = false
                    )
                }, LOCAL.flatOptions())
        }
    }
}