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
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider.Companion.toLocalDateTime
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.tools.ext.kotlinFixtureDefaultConfig
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule
import kotlin.time.Duration.Companion.minutes

class UpcomingPresenterTest : KoinTest {
    private val fixture = kotlinFixtureDefaultConfig

    @get:Rule
    var rule = CoroutineTestRule()

    private val view: UpcomingContract.View = mockk(relaxed = true)
    private val playlistItemOrchestrator: OrchestratorContract<PlaylistItemDomain> = mockk(relaxed = true)
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
            testCoroutines,
            timeProvider,
            log
        )
    }

    @Test
    fun loads_data() = runTest {
        val now = Clock.System.now()
        every { timeProvider.instant() } returns now
        val allUpcomingList: List<PlaylistItemDomain> = fixture<List<PlaylistItemDomain>>()
            .mapIndexed { i, it ->
                it.copy(
                    media = it.media.copy(broadcastDate = now.plus((i * 10 - 1).minutes).toLocalDateTime())
                )
            }
        coEvery {
            playlistItemOrchestrator.loadList(LiveUpcomingMediaFilter(100), LOCAL.deepOptions())
        } returns allUpcomingList
        allUpcomingList.forEachIndexed { i, item -> log.d("$i - ${item.media.broadcastDate}") }
        sut.checkForUpcomingEpisodes(30)
        //assertEquals( sut.checkForUpcomingEpisodes(30))
        verify(exactly = 0) { view.showNotification(allUpcomingList[0]) }
        verify(exactly = 1) { view.showNotification(allUpcomingList[1]) }
        verify(exactly = 1) { view.showNotification(allUpcomingList[2]) }
        verify(exactly = 1) { view.showNotification(allUpcomingList[3]) }
        verify(exactly = 0) { view.showNotification(allUpcomingList[4]) }
        verify(exactly = 0) { view.showNotification(allUpcomingList[5]) }
    }

}