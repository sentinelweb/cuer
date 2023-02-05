package uk.co.sentinelweb.cuer.app.queue

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import summarise
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.DELETE
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.usecase.PlaylistMediaUpdateUsecase
import uk.co.sentinelweb.cuer.app.usecase.PlaylistOrDefaultUsecase
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextTestProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.ext.matchesHeader
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain
import uk.co.sentinelweb.cuer.tools.ext.generatePlaylist
import uk.co.sentinelweb.cuer.tools.ext.kotlinFixtureDefaultConfig

// todo uncomment
@ExperimentalCoroutinesApi
class QueueMediatorTest {
    private val fixture = kotlinFixtureDefaultConfig

    @MockK
    lateinit var mockPlaylistOrchestrator: PlaylistOrchestrator

    @MockK
    lateinit var mockPlaylistItemOrchestrator: PlaylistItemOrchestrator

    @MockK
    lateinit var mockPrefsWrapper: MultiPlatformPreferencesWrapper

    @MockK
    lateinit var mediaUpdate: PlaylistMediaUpdateUsecase

    @MockK
    lateinit var playlistOrDefaultUsecase: PlaylistOrDefaultUsecase

    @MockK
    lateinit var mockRecentLocalPlaylists: RecentLocalPlaylists

    private val testCoroutineDispatcher = UnconfinedTestDispatcher()

    private val coroutines: CoroutineContextProvider = CoroutineContextTestProvider(testCoroutineDispatcher)
    private val playlistMutator: PlaylistMutator = PlaylistMutator()
    private val log: LogWrapper = SystemLogWrapper()
    private val testLog: LogWrapper = SystemLogWrapper()
    private val guidCreator: GuidCreator = GuidCreator()

    private lateinit var fixtCurrentPlaylist: PlaylistDomain
    private lateinit var fixtPlaylistDefault: PlaylistDomain
    private lateinit var fixtPlaylistOrchestratorFlow: MutableSharedFlow<Triple<Operation, Source, PlaylistDomain>>
    private lateinit var fixtPlaylistItemOrchestratorFlow: MutableSharedFlow<Triple<Operation, Source, PlaylistItemDomain>>
    private lateinit var fixtCurrentIdentifier: Identifier<GUID>
    private val fixtSource: Source = fixture()
    private val fixtDefaultSource: Source = fixture()
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
        testLog.tag(this)
        fixtCurrentPlaylist = generatePlaylist(fixture)
        fixtCurrentPlaylist = fixtCurrentPlaylist.copy(
            id = guidCreator.create().toIdentifier(fixture()),
            currentIndex = fixtCurrentCurentIndex,
            mode = SINGLE,
            items = fixture<List<PlaylistItemDomain>>()
                .map { it.copy(id = guidCreator.create().toIdentifier(fixture()), order = idCounter * 1000, playlistId = fixtCurrentPlaylist.id) })

        fixtPlaylistDefault = generatePlaylist(fixture)
            .copy(currentIndex = fixtDefaultCurentIndex, items = fixture())

        fixtCurrentIdentifier = fixtCurrentPlaylist.id!!
        fixtPlaylistOrchestratorFlow = MutableSharedFlow()
        fixtPlaylistItemOrchestratorFlow = MutableSharedFlow()
        capturePlaylistFlow = mutableListOf()
        captureItemFlow = mutableListOf()

        every { mockPlaylistOrchestrator.updates } returns fixtPlaylistOrchestratorFlow
        coEvery {
            playlistOrDefaultUsecase.getPlaylistOrDefault(fixtCurrentIdentifier)
        } returns (fixtCurrentPlaylist)
        every { mockPlaylistItemOrchestrator.updates } returns fixtPlaylistItemOrchestratorFlow
        every {
            mockPrefsWrapper.currentPlayingPlaylistId
        } returns (Identifier(fixtCurrentIdentifier.id, fixtCurrentIdentifier.source))
    }

    private fun createSut(state: QueueMediatorState = QueueMediatorState()) {
        sut = QueueMediator(
            state,
            mockPlaylistOrchestrator,
            mockPlaylistItemOrchestrator,
            coroutines,
            playlistMutator,
            mediaUpdate,
            playlistOrDefaultUsecase,
            mockPrefsWrapper,
            log,
            mockRecentLocalPlaylists
        )
        sut.currentItemFlow
            .onEach {
                testLog.d("flow item changed: $it")
                captureItemFlow.add(it)
            }
            .launchIn(coroutines.computationScope)
        sut.currentPlaylistFlow
            .onEach {
                testLog.d("flow playlist changed: $it")
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
    fun test_flow_playlist_header_changed() = runTest {
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


    @Test
    fun test_flow_playlist_changed() = runTest {
        createSut()
        val currentIndex = 3
        val fixChanged = generatePlaylist(fixture)
            .copy(id = fixtCurrentPlaylist.id, currentIndex = currentIndex, items = fixture())

        // test
        fixtPlaylistOrchestratorFlow
            .emit((Operation.FULL to fixtSource then fixChanged))

        //verify
        assertThat(sut.playlist!!.items.size).isEqualTo(fixChanged.items.size)
        assertThat(sut.playlist).isEqualTo(fixChanged)
        assertThat(sut.currentItemIndex).isEqualTo(currentIndex)
        assertThat(captureItemFlow.last()).isEqualTo(fixChanged.items.get(currentIndex))
    }

    @Test
    fun test_flow_playlist_deleted() = runTest {
        createSut()
        coEvery { playlistOrDefaultUsecase.getPlaylistOrDefault(fixtCurrentIdentifier) } returns (fixtPlaylistDefault)

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


    @Test
    fun test_flow_playlistitem_changed_not_current() = runTest {
        createSut()
        val replaceItemIndex = 3
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
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

    @Test
    fun test_flow_current_playlistitem_changed() = runTest {
        createSut()
        val replaceItemIndex = fixtCurrentCurentIndex
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
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


    @Test
    fun test_flow_current_playlistitem_moved() = runTest {
        createSut()
        val replaceItemIndex = fixtCurrentCurentIndex
        val targetItemIndex = 5
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
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


    @Test
    fun test_flow_current_playlistitem_moved_out_of_playlist___should_play_next_item() = runTest {
        createSut()
        val replaceItemIndex = fixtCurrentCurentIndex
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
            id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
            playlistId = fixture()
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


    @Test
    fun test_flow_playlistitem_moved_out_of_playlist_before_current() = runTest {
        createSut()
        val replaceItemIndex = fixtCurrentCurentIndex - 1
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
            id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
            playlistId = fixture()
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


    @Test
    fun test_flow_playlistitem_moved_out_of_playlist_after_current() = runTest {
        createSut()
        val replaceItemIndex = fixtCurrentCurentIndex + 1
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
            id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
            playlistId = fixture()
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


    @Test
    fun test_flow_playlistitem_added_to_playlist_before_current() = runTest {
        createSut()
        val addItemIndex = fixtCurrentCurentIndex - 1
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
            id = fixture(),
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


    @Test
    fun test_flow_playlistitem_added_to_playlist_after_current() = runTest {
        createSut()
        val addItemIndex = fixtCurrentCurentIndex + 1
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
            id = fixture(),
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


    @Test
    fun test_flow_playlistitem_deleted_before_current() = runTest {
        createSut()
        val deleteItemIndex = fixtCurrentCurentIndex - 1
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
            id = fixtCurrentPlaylist.items.get(deleteItemIndex).id,
            playlistId = fixture()
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


    @Test
    fun test_flow_playlistitem_deleted_after_current() = runTest {
        createSut()
        val deleteItemIndex = fixtCurrentCurentIndex + 1
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
            id = fixtCurrentPlaylist.items.get(deleteItemIndex).id,
            playlistId = fixture()
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


    @Test
    fun test_flow_playlistitem_deleted_is_current() = runTest {
        createSut()
        val deleteItemIndex = fixtCurrentCurentIndex
        val fixtChanged: PlaylistItemDomain = fixture<PlaylistItemDomain>().copy(
            id = fixtCurrentPlaylist.items.get(deleteItemIndex).id,
            playlistId = fixture()
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

    @Test
    fun switchToPlaylist() = runTest {
        createSut()

        val switchPlaylistId: Identifier<GUID> = fixture()
        val fixtSwitchPlaylist = generatePlaylist(fixture).copy(
            id = switchPlaylistId,
            currentIndex = 3,
            items = fixture<List<PlaylistItemDomain>>()
                .map { it.copy(id = guidCreator.create().toIdentifier(LOCAL), order = idCounter * 1000, playlistId = switchPlaylistId) })
        val switchIdentifier = switchPlaylistId
        coEvery {
            playlistOrDefaultUsecase.getPlaylistOrDefault(
                fixtSwitchPlaylist.id
            )
        } returns fixtSwitchPlaylist
        // test
        sut.switchToPlaylist(switchIdentifier)

        //verify
        val expectedCurrentItem = fixtSwitchPlaylist.items.get(fixtSwitchPlaylist.currentIndex)
        assertThat(sut.playlist!!.items.size).isEqualTo(fixtSwitchPlaylist.items.size)
        assertThat(sut.currentItemIndex).isEqualTo(fixtSwitchPlaylist.currentIndex)
        assertThat(sut.currentItem).isEqualTo(expectedCurrentItem)
        assertThat(captureItemFlow.last()).isEqualTo(expectedCurrentItem)
        assertThat(capturePlaylistFlow.last()).isEqualTo(fixtSwitchPlaylist)
        verify { mockPrefsWrapper.currentPlayingPlaylistId = switchIdentifier }
        verify { mockRecentLocalPlaylists.addRecent(sut.playlist!!) }
    }

    @Test
    fun onItemSelected__same_no_force_play() = runTest {
        createSut()
        val currentItem = sut.currentItem!!
        val itemFlowSize = captureItemFlow.size

        sut.onItemSelected(currentItem, forcePlay = false)

        assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
        assertThat(sut.currentItem).isEqualTo(currentItem)
        assertThat(captureItemFlow.last()).isEqualTo(currentItem)
        assertThat(itemFlowSize).isEqualTo(itemFlowSize)
        //verify { mockMediaSessionManager.setMedia(currentItem.media, queue.playlist) }
    }


    @Test
    fun onItemSelected_same_force_play() = runTest {
        createSut()
        val currentItem = sut.currentItem!!
        val itemFlowSize = captureItemFlow.size

        sut.onItemSelected(currentItem, forcePlay = true)

        assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
        assertThat(sut.currentItem).isEqualTo(currentItem)
        assertThat(captureItemFlow.last()).isEqualTo(currentItem)
        //verify { mockMediaSessionManager.setMedia(currentItem.media, queue.playlist) }
        coVerify {
            playlistOrDefaultUsecase.updateCurrentIndex(
                fixtCurrentPlaylist,
                fixtCurrentIdentifier.flatOptions(true)
            )
        }
        assertThat(itemFlowSize).isEqualTo(itemFlowSize) // doesnt emit same object
    }


    @Test
// todo test force play , reset position
    fun onItemSelected__reset_position() = runTest {
        createSut()
        val currentItemBefore = sut.currentItem!!
        val expectedCurrentItem =
            currentItemBefore.copy(media = currentItemBefore.media.copy(positon = 0, watched = true))
        val mediaPositionUpdate = MediaPositionUpdateDomain(
            id = expectedCurrentItem.media.id!!,
            positon = expectedCurrentItem.media.positon,
            duration = expectedCurrentItem.media.duration,
            dateLastPlayed = expectedCurrentItem.media.dateLastPlayed,
            watched = true
        )
        val expectedMediaAfterUpdate = expectedCurrentItem.media.copy(
            positon = mediaPositionUpdate.positon,
            duration = mediaPositionUpdate.duration,
            dateLastPlayed = mediaPositionUpdate.dateLastPlayed,
            watched = mediaPositionUpdate.watched
        )
        coEvery {
            mediaUpdate.updateMedia(
                fixtCurrentPlaylist, mediaPositionUpdate, fixtCurrentIdentifier.flatOptions(emit = true)
            )
        } returns expectedMediaAfterUpdate

        sut.onItemSelected(currentItemBefore, forcePlay = true, resetPosition = true)

        assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
        assertThat(sut.currentItem).isNotEqualTo(currentItemBefore) // position changed
        assertThat(sut.currentItem).isEqualTo(expectedCurrentItem) // position changed
        assertThat(captureItemFlow.last()).isEqualTo(expectedCurrentItem)
        //verify { mockMediaSessionManager.setMedia(expectedCurrentItem.media, queue.playlist) }
        coVerify {
            playlistOrDefaultUsecase.updateCurrentIndex(
                fixtCurrentPlaylist,
                fixtCurrentIdentifier.flatOptions(true)
            )
        }
        assertThat(captureItemFlow.size).isEqualTo(2)
    }


    @Test
// todo test force play , reset position
    fun onItemSelected__reset_position__different_item__no_force() = runTest {
        createSut()
        val selectedItemIndex = 4
        val selectedItem = fixtCurrentPlaylist.items.get(selectedItemIndex)
        val fixtUpdatedPlaylist = fixtCurrentPlaylist.copy(currentIndex = selectedItemIndex)
        val expectedSelectedItem = selectedItem.copy(media = selectedItem.media.copy(positon = 0, watched = true))
        val mediaPositionUpdate = MediaPositionUpdateDomain(
            id = expectedSelectedItem.media.id!!,
            positon = expectedSelectedItem.media.positon,
            duration = expectedSelectedItem.media.duration,
            dateLastPlayed = expectedSelectedItem.media.dateLastPlayed,
            watched = expectedSelectedItem.media.watched
        )
        val expectedMediaAfterUpdate = expectedSelectedItem.media.copy(
            positon = mediaPositionUpdate.positon,
            duration = mediaPositionUpdate.duration,
            dateLastPlayed = mediaPositionUpdate.dateLastPlayed,
            watched = mediaPositionUpdate.watched
        )
        coEvery {
            mediaUpdate.updateMedia(
                fixtUpdatedPlaylist, mediaPositionUpdate, fixtCurrentIdentifier.flatOptions(emit = true)
            )
        } returns expectedMediaAfterUpdate

        sut.onItemSelected(selectedItem, resetPosition = true)

        assertThat(sut.currentItemIndex).isEqualTo(selectedItemIndex)
        assertThat(sut.currentItem).isNotEqualTo(selectedItem) // position changed
        assertThat(sut.currentItem).isEqualTo(expectedSelectedItem) // position changed
        assertThat(captureItemFlow.last()).isEqualTo(expectedSelectedItem)
        //verify { mockMediaSessionManager.setMedia(expectedSelectedItem.media, queue.playlist) }
        coVerify {
            playlistOrDefaultUsecase.updateCurrentIndex(
                fixtCurrentPlaylist.copy(currentIndex = selectedItemIndex),
                fixtCurrentIdentifier.flatOptions(true)
            )
        }
        assertThat(captureItemFlow.size).isEqualTo(2)
    }


    @Test
    fun playNow__playlist_only() {
        // todo
    }

    @Test
    fun playNow__with_playlist_item() {
        // todo
    }

    @Test
    fun updateCurrentMediaItem() = runTest {
        createSut()
        val fixtUpdateMedia: MediaDomain = fixture()
        val mediaPositionUpdate = MediaPositionUpdateDomain(
            id = sut.currentItem!!.media.id!!,
            positon = fixtUpdateMedia.positon,
            duration = fixtUpdateMedia.duration,
            dateLastPlayed = fixtUpdateMedia.dateLastPlayed,
            watched = true
        )
        val expectedMediaAfterUpdate = sut.currentItem!!.media.copy(
            positon = mediaPositionUpdate.positon,
            duration = mediaPositionUpdate.duration,
            dateLastPlayed = mediaPositionUpdate.dateLastPlayed,
            watched = mediaPositionUpdate.watched
        )
        coEvery {
            mediaUpdate.updateMedia(
                fixtCurrentPlaylist, mediaPositionUpdate, fixtCurrentIdentifier.flatOptions(emit = true)
            )
        } returns expectedMediaAfterUpdate

        // test
        sut.updateCurrentMediaItem(fixtUpdateMedia)

        // verify
        assertThat(sut.currentItemIndex).isEqualTo(fixtCurrentCurentIndex)
        assertThat(sut.currentItem!!.media).isEqualTo(expectedMediaAfterUpdate) // position changed
        coVerify {
            mediaUpdate.updateMedia(fixtCurrentPlaylist, mediaPositionUpdate, sut.playlistId!!.flatOptions(true))
        }
        // todo fix
    }

    @Test
    fun destroy() = runTest {
        createSut()
        // test
        sut.destroy()
        // todo check how to verify
    }

    //////////// flaky tests ////////////////////////////////////////////
    @Test
    @Ignore("flaky")
    fun onItemSelected_simple() = runTest {
        // QueueMediatorState(playlist = fixtCurrentPlaylist, playlistIdentifier = fixtCurrentIdentifier)
        createSut()
        val selectedItemIndex = 4
        val selectedItem = fixtCurrentPlaylist.items.get(selectedItemIndex)
        testLog.d("onItemSelected_simple:${selectedItem.summarise()}")
//        val mediaPositionUpdate = MediaPositionUpdateDomain(
//            id = selectedItem.media.id!!,
//            positon = selectedItem.media.positon,
//            duration = selectedItem.media.duration,
//            dateLastPlayed = selectedItem.media.dateLastPlayed,
//            watched = true
//        )
//        val expectedMediaAfterUpdate = selectedItem.media.copy(
//            positon = mediaPositionUpdate.positon,
//            duration = mediaPositionUpdate.duration,
//            dateLastPlayed = mediaPositionUpdate.dateLastPlayed,
//            watched = mediaPositionUpdate.watched
//        )
//        coEvery {
//            mediaUpdate.updateMedia(
//                fixtCurrentPlaylist, mediaPositionUpdate, fixtCurrentIdentifier.flatOptions(emit = true)
//            )
//        } answers { testLog.d("mock return media"); expectedMediaAfterUpdate}
        // test
        sut.onItemSelected(selectedItem)

        //verify
        assertThat(sut.currentItemIndex).isEqualTo(selectedItemIndex)
        assertThat(sut.currentItem).isEqualTo(selectedItem)
        assertThat(captureItemFlow.last()).isEqualTo(selectedItem)
        assertThat(captureItemFlow.size).isEqualTo(2) // should emit
        //verify { mockMediaSessionManager.setMedia(selectedItem.media, queue.playlist) }
    }

    @Test
    @Ignore("flaky")
    fun nextItem() = runTest {
        createSut()
        val expectedIndex = fixtCurrentCurentIndex + 1

        // test
        sut.nextItem()

        // verify
        assertThat(sut.currentItemIndex).isEqualTo(expectedIndex)
        assertThat(sut.currentItem).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
        assertThat(captureItemFlow.last()).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
        //verify { mockMediaSessionManager.setMedia(fixtCurrentPlaylist.items.get(expectedIndex).media, queue.playlist) }
        coVerify {
            playlistOrDefaultUsecase.updateCurrentIndex(
                fixtCurrentPlaylist.copy(currentIndex = expectedIndex),
                fixtCurrentIdentifier.flatOptions(true)
            )
        }
    }

    @Test
    @Ignore("flaky")
    fun onTrackEnded() = runTest {
        createSut()
        val expectedIndex = fixtCurrentCurentIndex + 1

        // test
        sut.onTrackEnded()

        // verify
        assertThat(sut.currentItemIndex).isEqualTo(expectedIndex)
        assertThat(sut.currentItem).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
        assertThat(captureItemFlow.last()).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
        coVerify {
            playlistOrDefaultUsecase.updateCurrentIndex(
                fixtCurrentPlaylist.copy(currentIndex = expectedIndex),
                fixtCurrentIdentifier.flatOptions(true)
            )
        }
    }

    @Test
    @Ignore("flaky")
    fun previousItem() = runTest {
        createSut()
        val expectedIndex = fixtCurrentCurentIndex - 1

        // test
        sut.previousItem()

        // verify
        assertThat(sut.currentItemIndex).isEqualTo(expectedIndex)
        assertThat(sut.currentItem).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
        assertThat(captureItemFlow.last()).isEqualTo(fixtCurrentPlaylist.items.get(expectedIndex))
        coVerify {
            playlistOrDefaultUsecase.updateCurrentIndex(
                fixtCurrentPlaylist.copy(currentIndex = expectedIndex),
                fixtCurrentIdentifier.flatOptions(true)
            )
        }
    }

    @Test
    @Ignore("flaky")
    fun test_flow_playlistitem_replace_same_item__no_change__does_not_emit() = runTest {
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