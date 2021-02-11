package uk.co.sentinelweb.cuer.app.queue

import com.flextrade.jfixture.JFixture
import com.google.common.truth.Truth.assertThat
import io.mockk.*
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
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.DELETE
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYLIST
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextTestProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
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
        fixtCurrentPlaylist = fixtCurrentPlaylist.copy(
            currentIndex = fixtCurrentCurentIndex,
            mode = SINGLE,
            items = fixture.buildCollection<PlaylistItemDomain, List<PlaylistItemDomain>>(10)
                .map { it.copy(id = idCounter, order = idCounter * 1000, playlistId = fixtCurrentPlaylist.id) })

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
        assertThat(sut.currentItem).isEqualTo(fixtCurrentPlaylist.items[fixtCurrentCurentIndex])
    }

    @Test
    fun getCurrentItemIndex() {
        createSut()
        assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
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

            // test
            fixtPlaylistOrchestratorFlow
                .emit((FLAT to fixtSource then fixChangedHeader))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size)
            assertThat(sut.playlist!!.items).isEqualTo(fixtCurrentPlaylist.items)
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

            // test
            fixtPlaylistOrchestratorFlow
                .emit((Operation.FULL to fixtSource then fixChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixChanged.items.size)
            assertThat(sut.playlist).isEqualTo(fixChanged)
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

            //verify
            assertThat(sut.playlist).isEqualTo(fixtPlaylistDefault)
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtPlaylistDefault.items.size)
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
                playlistId = fixtCurrentPlaylist.id,
                order = fixtCurrentPlaylist.items.get(replaceItemIndex).order
            )
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size)
            assertThat(sut.playlist!!.items.get(replaceItemIndex)).isEqualTo(fixtChanged)
            assertThat(captureItemFlow.last()).isEqualTo(fixtCurrentPlaylist.items.get(fixtCurrentCurentIndex))
        }
    }

    @Test
    fun `test flow current playlistitem changed`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val replaceItemIndex = fixtCurrentCurentIndex
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id,
                order = fixtCurrentPlaylist.items.get(replaceItemIndex).order
            )
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size)
            assertThat(sut.playlist!!.items.get(replaceItemIndex)).isEqualTo(fixtChanged)
            assertThat(captureItemFlow.last()).isEqualTo(fixtChanged)
        }
    }

    @Test
    fun `test flow current playlistitem moved`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val replaceItemIndex = fixtCurrentCurentIndex
            val targetItemIndex = 5
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id,
                order = fixtCurrentPlaylist.items.get(targetItemIndex).order
            )
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size)
            assertThat(sut.playlist!!.items.get(targetItemIndex)).isEqualTo(fixtChanged)
            assertThat(captureItemFlow.last()).isEqualTo(fixtChanged)
            assertThat(sut.currentItemIndex).isEqualTo(targetItemIndex)
        }
    }

    @Test
    fun `test flow current playlistitem moved out of playlist - should play next item`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val replaceItemIndex = fixtCurrentCurentIndex
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )
            val nextItem = fixtCurrentPlaylist.items.get(replaceItemIndex + 1)

            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(sut.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            assertThat(captureItemFlow.last()).isEqualTo(nextItem)
            assertThat(sut.currentItemIndex).isEqualTo(replaceItemIndex)
        }
    }

    @Test
    fun `test flow playlistitem moved out of playlist before current`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val replaceItemIndex = fixtCurrentCurentIndex - 1
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )
            val currentItemBefore = sut.currentItem
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(sut.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            assertThat(captureItemFlow.last()).isEqualTo(currentItemBefore)
            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex - 1)
        }
    }

    @Test
    fun `test flow playlistitem moved out of playlist after current`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val replaceItemIndex = fixtCurrentCurentIndex + 1
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )
            val currentItemBefore = sut.currentItem
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(sut.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            assertThat(captureItemFlow.last()).isEqualTo(currentItemBefore)
            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
        }
    }

    @Test
    fun `test flow playlistitem added to playlist before current`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val addItemIndex = fixtCurrentCurentIndex - 1
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixture.build(),
                playlistId = fixtCurrentPlaylist.id,
                order = fixtCurrentPlaylist.items.get(addItemIndex).order - 100
            )
            val currentItemBefore = sut.currentItem
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size + 1)
            assertThat(sut.playlist!!.items.find { it.id == fixtChanged.id }).isEqualTo(fixtChanged)
            assertThat(sut.playlist!!.items.indexOfFirst { it.id == fixtChanged.id }).isEqualTo(addItemIndex)
            assertThat(captureItemFlow.last()).isEqualTo(currentItemBefore)
            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex + 1)
        }
    }

    @Test
    fun `test flow playlistitem added to playlist after current`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val addItemIndex = fixtCurrentCurentIndex + 1
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixture.build(),
                playlistId = fixtCurrentPlaylist.id,
                order = fixtCurrentPlaylist.items.get(addItemIndex).order - 100
            )
            val currentItemBefore = sut.currentItem
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size + 1)
            assertThat(sut.playlist!!.items.find { it.id == fixtChanged.id }).isEqualTo(fixtChanged)
            assertThat(sut.playlist!!.items.indexOfFirst { it.id == fixtChanged.id }).isEqualTo(addItemIndex)
            assertThat(captureItemFlow.last()).isEqualTo(currentItemBefore)
            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
        }
    }

    @Test
    fun `test flow playlistitem deleted before current`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val deleteItemIndex = fixtCurrentCurentIndex - 1
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(deleteItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )
            val currentItemBefore = sut.currentItem
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((DELETE to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(sut.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            assertThat(captureItemFlow.last()).isEqualTo(currentItemBefore)
            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex - 1)
        }
    }

    @Test
    fun `test flow playlistitem deleted after current`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val deleteItemIndex = fixtCurrentCurentIndex + 1
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(deleteItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )
            val currentItemBefore = sut.currentItem
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((DELETE to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(sut.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            assertThat(captureItemFlow.last()).isEqualTo(currentItemBefore)
            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
        }
    }

    @Test
    fun `test flow playlistitem deleted is current`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val deleteItemIndex = fixtCurrentCurentIndex
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(deleteItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )
            val expectedPlayingAfter = fixtCurrentPlaylist.items.get(deleteItemIndex + 1)
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((DELETE to fixtSource then fixtChanged))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(sut.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            assertThat(captureItemFlow.last()).isEqualTo(expectedPlayingAfter)
            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
        }
    }

    @Test
    fun `test flow playlistitem replace same item (no change) does not emit`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val sameItemIndex = fixtCurrentCurentIndex - 1
            val fixtSame = fixtCurrentPlaylist.items.get(sameItemIndex)
            val currentItem = sut.currentItem
            val emitSizeBefore = captureItemFlow.size
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((FLAT to fixtSource then fixtSame))

            //verify
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size)
            assertThat(sut.playlist!!.items.find { it.id == fixtSame.id }).isEqualTo(fixtSame)
            assertThat(captureItemFlow.last()).isEqualTo(currentItem)
            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
            assertThat(emitSizeBefore).isEqualTo(captureItemFlow.size)
        }
    }

    @Test
    fun switchToPlaylist() {
        testCoroutineScope.runBlockingTest {
            createSut()

            val switchPlaylistId: Long = fixture.build()
            val fixtSwitchPlaylist = fixture.build<PlaylistDomain>().copy(
                id = switchPlaylistId,
                currentIndex = 3,
                items = fixture.buildCollection<PlaylistItemDomain, List<PlaylistItemDomain>>(13)
                    .map { it.copy(id = idCounter, order = idCounter * 1000, playlistId = switchPlaylistId) })
            val fixtSwitchSource: Source = fixture.build()
            val switchIdentifier = switchPlaylistId.toIdentifier(fixtSwitchSource)
            coEvery {
                mockPlaylistOrchestrator.getPlaylistOrDefault(fixtSwitchPlaylist.id, Options(fixtSwitchSource, flat = false))
            } returns (fixtSwitchPlaylist to fixtSwitchSource)
            // test
            sut.switchToPlaylist(switchIdentifier)

            //verify
            val expectedCurrentItem = fixtSwitchPlaylist.items.get(fixtSwitchPlaylist.currentIndex)
            assertThat(sut.playlist!!.items.size).isEqualTo(fixtSwitchPlaylist.items.size)
            assertThat(sut.currentItemIndex).isEqualTo(fixtSwitchPlaylist.currentIndex)
            assertThat(sut.currentItem).isEqualTo(expectedCurrentItem)
            assertThat(captureItemFlow.last()).isEqualTo(expectedCurrentItem)
            assertThat(capturePlaylistFlow.last()).isEqualTo(fixtSwitchPlaylist)
            verify { mockPrefsWrapper.putPair(CURRENT_PLAYLIST, switchIdentifier.toPair()) }
        }
    }

    @Test
    fun `onItemSelected - simple`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val selectedItemIndex = 8
            val selectedItem = fixtCurrentPlaylist.items.get(selectedItemIndex)

            // test
            sut.onItemSelected(selectedItem)

            //verify
            assertThat(sut.currentItemIndex).isEqualTo(selectedItemIndex)
            assertThat(sut.currentItem).isEqualTo(selectedItem)
            assertThat(captureItemFlow.last()).isEqualTo(selectedItem)
            assertThat(captureItemFlow.size).isEqualTo(2) // should emit
            verify { mockMediaSessionManager.setMedia(selectedItem.media) }
        }
    }

    @Test
    fun `onItemSelected - same no force play`() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val currentItem = sut.currentItem!!
            val itemFlowSize = captureItemFlow.size

            sut.onItemSelected(currentItem, forcePlay = false)

            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
            assertThat(sut.currentItem).isEqualTo(currentItem)
            assertThat(captureItemFlow.last()).isEqualTo(currentItem)
            assertThat(itemFlowSize).isEqualTo(itemFlowSize)
            verify { mockMediaSessionManager.setMedia(currentItem.media) }
        }
    }

    @Test
    fun `onItemSelected - same force play`() {// todo test force play , reset position
        testCoroutineScope.runBlockingTest {
            createSut()
            val currentItem = sut.currentItem!!
            val itemFlowSize = captureItemFlow.size

            sut.onItemSelected(currentItem, forcePlay = true)

            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
            assertThat(sut.currentItem).isEqualTo(currentItem)
            assertThat(captureItemFlow.last()).isEqualTo(currentItem)
            verify { mockMediaSessionManager.setMedia(currentItem.media) }
            coVerify { mockPlaylistOrchestrator.updateCurrentIndex(fixtCurrentPlaylist, fixtCurrentIdentifier.toFlatOptions<Long>(true)) }
            assertThat(itemFlowSize).isEqualTo(itemFlowSize) // doesnt emit same object
        }
    }

    @Test
    fun `onItemSelected - reset position`() {// todo test force play , reset position
        testCoroutineScope.runBlockingTest {
            createSut()
            val currentItemBefore = sut.currentItem!!
            val expectedCurrentItem = currentItemBefore.copy(media = currentItemBefore.media.copy(positon = 0, watched = true))

            sut.onItemSelected(currentItemBefore, forcePlay = true, resetPosition = true)

            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
            assertThat(sut.currentItem).isNotEqualTo(currentItemBefore) // position changed
            assertThat(sut.currentItem).isEqualTo(expectedCurrentItem) // position changed
            assertThat(captureItemFlow.last()).isEqualTo(expectedCurrentItem)
            verify { mockMediaSessionManager.setMedia(expectedCurrentItem.media) }
            coVerify { mockPlaylistOrchestrator.updateCurrentIndex(fixtCurrentPlaylist, fixtCurrentIdentifier.toFlatOptions<Long>(true)) }
            assertThat(captureItemFlow.size).isEqualTo(2)
        }
    }

    @Test
    fun `onItemSelected - reset position - different item - no force`() {// todo test force play , reset position
        testCoroutineScope.runBlockingTest {
            createSut()
            val selectedItemIndex = 8
            val selectedItem = fixtCurrentPlaylist.items.get(selectedItemIndex)
            val expectedSelectedItem = selectedItem.copy(media = selectedItem.media.copy(positon = 0, watched = true))

            sut.onItemSelected(selectedItem, resetPosition = true)

            assertThat(sut.currentItemIndex).isEqualTo(selectedItemIndex)
            assertThat(sut.currentItem).isNotEqualTo(selectedItem) // position changed
            assertThat(sut.currentItem).isEqualTo(expectedSelectedItem) // position changed
            assertThat(captureItemFlow.last()).isEqualTo(expectedSelectedItem)
            verify { mockMediaSessionManager.setMedia(expectedSelectedItem.media) }
            coVerify {
                mockPlaylistOrchestrator.updateCurrentIndex(
                    fixtCurrentPlaylist.copy(currentIndex = selectedItemIndex),
                    fixtCurrentIdentifier.toFlatOptions<Long>(true)
                )
            }
            assertThat(captureItemFlow.size).isEqualTo(2)
        }
    }

    @Test
    fun `playNow - playlist only`() {
        // todo
    }

    @Test
    fun `playNow - with playlist item`() {
        // todo
    }

    @Test
    fun updateCurrentMediaItem() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val fixtUpdateMedia: MediaDomain = fixture.build()
            val expectedMediaAfterUpdate = sut.currentItem!!.media.copy(
                positon = fixtUpdateMedia.positon,
                duration = fixtUpdateMedia.duration,
                dateLastPlayed = fixtUpdateMedia.dateLastPlayed,
                watched = true
            )
            // test
            sut.updateCurrentMediaItem(fixtUpdateMedia)

            // verify
            assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
            assertThat(sut.currentItem!!.media).isEqualTo(expectedMediaAfterUpdate) // position changed
            coVerify {
                mockMediaOrchestrator.save(expectedMediaAfterUpdate, sut.playlistId!!.toFlatOptions<Long>(true))
            }
        }

    }

    @Test
    fun destroy() {
        testCoroutineScope.runBlockingTest {
            createSut()
            // test
            sut.destroy()
            // todo check how to verify
        }
    }

    @Test
    fun nextItem() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val expectedIndex = fixtCurrentCurentIndex + 1

            // test
            sut.nextItem()

            // verify
            assertThat(sut.currentItemIndex).isEqualTo(expectedIndex)
            assertThat(sut.currentItem).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
            assertThat(captureItemFlow.last()).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
            verify { mockMediaSessionManager.setMedia(fixtCurrentPlaylist.items.get(expectedIndex).media) }
            coVerify {
                mockPlaylistOrchestrator.updateCurrentIndex(
                    fixtCurrentPlaylist.copy(currentIndex = expectedIndex),
                    fixtCurrentIdentifier.toFlatOptions<Long>(true)
                )
            }
        }
    }

    @Test
    fun previousItem() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val expectedIndex = fixtCurrentCurentIndex - 1

            // test
            sut.previousItem()

            // verify
            assertThat(sut.currentItemIndex).isEqualTo(expectedIndex)
            assertThat(sut.currentItem).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
            assertThat(captureItemFlow.last()).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
            verify { mockMediaSessionManager.setMedia(fixtCurrentPlaylist.items.get(expectedIndex).media) }
            coVerify {
                mockPlaylistOrchestrator.updateCurrentIndex(
                    fixtCurrentPlaylist.copy(currentIndex = expectedIndex),
                    fixtCurrentIdentifier.toFlatOptions<Long>(true)
                )
            }
        }
    }

    @Test
    fun onTrackEnded() {
        testCoroutineScope.runBlockingTest {
            createSut()
            val expectedIndex = fixtCurrentCurentIndex + 1

            // test
            sut.onTrackEnded(sut.currentItem?.media)

            // verify
            assertThat(sut.currentItemIndex).isEqualTo(expectedIndex)
            assertThat(sut.currentItem).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
            assertThat(captureItemFlow.last()).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
            verify { mockMediaSessionManager.setMedia(fixtCurrentPlaylist.items.get(expectedIndex).media) }
            coVerify {
                mockPlaylistOrchestrator.updateCurrentIndex(
                    fixtCurrentPlaylist.copy(currentIndex = expectedIndex),
                    fixtCurrentIdentifier.toFlatOptions<Long>(true)
                )
            }
        }
    }
}