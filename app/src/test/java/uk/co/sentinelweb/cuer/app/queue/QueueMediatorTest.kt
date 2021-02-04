package uk.co.sentinelweb.cuer.app.queue

import com.flextrade.jfixture.JFixture
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.DELETE
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.toPair
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYLIST
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextTestProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.matchesHeader
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator
import uk.co.sentinelweb.cuer.tools.ext.build
import uk.co.sentinelweb.cuer.tools.ext.buildCollection

@ExperimentalCoroutinesApi
class QueueMediatorTest {

    @MockK
    lateinit var mockMediaOrchestrator: MediaOrchestrator

    @MockK
    lateinit var mockPlaylistOrchestrator: PlaylistOrchestrator

    @MockK
    lateinit var mockPlaylistItemOrchestrator: PlaylistItemOrchestrator

    @MockK
    lateinit var mockMediaSessionManager: MediaSessionManager

    @MockK
    lateinit var mockPrefsWrapper: SharedPrefsWrapper<GeneralPreferences>

    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(TestCoroutineDispatcher())

    private val coroutines: CoroutineContextProvider = CoroutineContextTestProvider(testCoroutineDispatcher)
    private val playlistMutator: PlaylistMutator = PlaylistMutator()
    private val log: LogWrapper = SystemLogWrapper()

    private val fixture = JFixture()
    private lateinit var fixtCurrentPlaylist: PlaylistDomain
    private lateinit var fixtPlaylistDefault: PlaylistDomain
    private lateinit var fixtPlaylistOrchestratorFlow: MutableSharedFlow<Triple<Operation, Source, PlaylistDomain>>
    private lateinit var fixtPlaylistItemOrchestratorFlow: MutableSharedFlow<Triple<Operation, Source, PlaylistItemDomain>>
    private lateinit var fixtCurrentIdentifier: Identifier<Long>
    private val fixtSource: Source = fixture.build()
    private val fixtDefaultSource: Source = fixture.build()
    private val fixtDefaultCurentIndex: Int = 5
    private val fixtCurrentCurentIndex: Int = 1

    private var _idCounter: Long = 0
    private val idCounter: Long
        get() {
            _idCounter++
            return _idCounter
        }
    private lateinit var capturePlaylistFlow: MutableList<PlaylistDomain>
    private lateinit var captureItemFlow: MutableList<PlaylistItemDomain?>

    private lateinit var sut: QueueMediator

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        fixtCurrentPlaylist = fixture.build<PlaylistDomain>()
            .copy(
                currentIndex = fixtCurrentCurentIndex,
                items = fixture.buildCollection<PlaylistItemDomain, List<PlaylistItemDomain>>(10).map { it.copy(id = idCounter) })
        fixtPlaylistDefault = fixture.build<PlaylistDomain>()
            .copy(currentIndex = fixtDefaultCurentIndex, items = fixture.buildCollection(10))
        fixtCurrentIdentifier = Identifier(fixtCurrentPlaylist.id!!, fixtSource)
        fixtPlaylistOrchestratorFlow = MutableSharedFlow()
        fixtPlaylistItemOrchestratorFlow = MutableSharedFlow()
        capturePlaylistFlow = mutableListOf()
        captureItemFlow = mutableListOf()

        every { mockPlaylistOrchestrator.updates } returns fixtPlaylistOrchestratorFlow
        coEvery {
            mockPlaylistOrchestrator.getPlaylistOrDefault(fixtCurrentIdentifier.id, Options(fixtCurrentIdentifier.source, flat = false))
        } returns (fixtCurrentPlaylist to fixtSource)
        every { mockPlaylistItemOrchestrator.updates } returns fixtPlaylistItemOrchestratorFlow
        every {
            mockPrefsWrapper.getPair(CURRENT_PLAYLIST, NO_PLAYLIST.toPair())
        } returns (fixtCurrentIdentifier.id to fixtCurrentIdentifier.source)
    }

    private fun createSut(state: QueueMediatorState = QueueMediatorState()) {
        sut = QueueMediator(
            state,
            mockMediaOrchestrator,
            mockPlaylistOrchestrator,
            mockPlaylistItemOrchestrator,
            coroutines,
            mockMediaSessionManager,
            playlistMutator,
            mockPrefsWrapper,
            log
        )
        sut.currentItemFlow
            .onEach {
                log.d("flow item changed: $it")
                captureItemFlow.add(it)
            }
            .launchIn(coroutines.computationScope)
        sut.currentPlaylistFlow
            .onEach {
                log.d("flow playlist changed: $it")
                capturePlaylistFlow.add(it)
            }
            .launchIn(coroutines.computationScope)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getCurrentItem() {
        createSut()
        assertThat(sut.currentItem).isEqualTo(fixtCurrentPlaylist.items[0])
    }

    @Test
    fun getCurrentItemIndex() {
        createSut()
        assertThat(sut.currentItemIndex).isEqualTo(0)
    }

    @Test
    fun getPlaylist() {
        createSut()
        assertThat(sut.playlist).isEqualTo(fixtCurrentPlaylist)
    }

    @Test
    fun getPlaylistId() {
        createSut()
        assertThat(sut.playlistId).isEqualTo(fixtCurrentIdentifier)
    }

    @Test
    fun `test flow playlist header changed`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val fixChangedHeader = fixtCurrentPlaylist.copy(currentIndex = 1)

            fixtPlaylistOrchestratorFlow
                .emit((FLAT to fixtSource then fixChangedHeader))

            assertThat(sut.currentItemIndex).isEqualTo(1)
            assertThat(captureItemFlow.last()).isEqualTo(fixtCurrentPlaylist.items.get(1))
            assertTrue(sut.playlist?.matchesHeader(fixChangedHeader) ?: false)
            //assertThat(captureItemFlow.size).isEqualTo(1) // todo check why 2 events
        }
    }

    @Test
    fun `test flow playlist changed`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val currentIndex = 3
            val fixChanged = fixture.build<PlaylistDomain>()
                .copy(id = fixtCurrentPlaylist.id, currentIndex = currentIndex, items = fixture.buildCollection(15))

            fixtPlaylistOrchestratorFlow
                .emit((Operation.FULL to fixtSource then fixChanged))

            assertThat(sut.currentItemIndex).isEqualTo(currentIndex)
            assertThat(captureItemFlow.last()).isEqualTo(fixChanged.items.get(currentIndex))
        }
    }

    @Test
    fun `test flow playlist deleted`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            coEvery {
                mockPlaylistOrchestrator.getPlaylistOrDefault(fixtCurrentIdentifier.id, Options(fixtCurrentIdentifier.source, flat = false))
            } returns (fixtPlaylistDefault to fixtDefaultSource)

            // test
            fixtPlaylistOrchestratorFlow
                .emit((DELETE to fixtSource then fixtCurrentPlaylist))

            assertThat(sut.currentItemIndex).isEqualTo(fixtDefaultCurentIndex)
            assertThat(captureItemFlow.last()).isEqualTo(fixtPlaylistDefault.items.get(fixtDefaultCurentIndex))
            assertThat(capturePlaylistFlow.last()).isEqualTo(fixtPlaylistDefault)
        }
    }

    @Test
    fun `test flow playlistitem changed not current`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val replaceItemIndex = 3
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id
            )
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtChanged))

            assertThat(sut.playlist!!.items.get(replaceItemIndex)).isEqualTo(fixtChanged)
        }
    }

    @Test
    fun `test flow current playlistitem changed`() {
        testCoroutineScope.runBlockingTest {
            createSut()
        }
    }

    @Test
    fun `test flow playlistitem deleted`() {
        testCoroutineScope.runBlockingTest {
            createSut()
        }
    }

    @Test
    fun `test flow current playlistitem deleted`() {
        testCoroutineScope.runBlockingTest {
            createSut()
        }
    }

    @Test
    fun get_currentItemFlow() {
    }

    @Test
    fun set_currentItemFlow() {
    }

    @Test
    fun getCurrentItemFlow() {
    }

    @Test
    fun get_currentPlaylistFlow() {
    }

    @Test
    fun set_currentPlaylistFlow() {
    }

    @Test
    fun getCurrentPlaylistFlow() {
    }

    @Test
    fun switchToPlaylist() {
    }

    @Test
    fun onItemSelected() {
    }

    @Test
    fun playNow() {
    }

    @Test
    fun testPlayNow() {
    }

    @Test
    fun updateMediaItem() {
    }

    @Test
    fun destroy() {
    }

    @Test
    fun nextItem() {
    }

    @Test
    fun previousItem() {
    }

    @Test
    fun onTrackEnded() {
    }

    @Test
    fun refreshQueueBackground() {
    }

    @Test
    fun refreshQueue() {
    }
}