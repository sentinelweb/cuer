package uk.co.sentinelweb.cuer.app.ui.playlist.old

import com.flextrade.jfixture.JFixture
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.old.PlaylistContract.State
import uk.co.sentinelweb.cuer.app.usecase.AddPlaylistUsecase
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.usecase.PlaylistOrDefaultUsecase
import uk.co.sentinelweb.cuer.app.usecase.PlaylistUpdateUsecase
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextTestProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.matchesHeader
import uk.co.sentinelweb.cuer.domain.ext.scanOrder
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator
import uk.co.sentinelweb.cuer.tools.ext.build
import uk.co.sentinelweb.cuer.tools.ext.buildCollection

@ExperimentalCoroutinesApi
@Ignore("superceeded by MVI implementation")
class PlaylistPresenterTest {
    @MockK
    lateinit var mockView: PlaylistContract.View

    @MockK
    lateinit var mockMediaOrchestrator: MediaOrchestrator

    @MockK
    lateinit var mockAddPlaylistUsecase: AddPlaylistUsecase

    @MockK
    lateinit var mockPlaylistOrchestrator: PlaylistOrchestrator

    @MockK
    lateinit var mockPlaylistItemOrchestrator: PlaylistItemOrchestrator

    @MockK
    lateinit var mockPlaylistUpdateUsecase: PlaylistUpdateUsecase

    @MockK
    lateinit var mockModelMapper: PlaylistModelMapper

    @MockK
    lateinit var mockQueue: QueueMediatorContract.Producer

    @MockK
    lateinit var mockToastWrapper: ToastWrapper

    @MockK
    lateinit var mockYtContextHolder: ChromecastYouTubePlayerContextHolder

    @MockK
    lateinit var mockChromeCastWrapper: ChromeCastWrapper

    @MockK
    lateinit var mockYtJavaApi: YoutubeJavaApiWrapper

    @MockK
    lateinit var mockShareWrapper: AndroidShareWrapper

    @MockK
    lateinit var mockTimeProvider: TimeProvider

    @MockK
    lateinit var mockResources: ResourceWrapper

    @MockK
    lateinit var mockDbInit: DatabaseInitializer

    @MockK
    lateinit var mockPlaylistOrDefaultUsecase: PlaylistOrDefaultUsecase

    @MockK
    lateinit var mockRecentLocalPlaylists: RecentLocalPlaylists

    @MockK
    lateinit var mockPlayUseCase: PlayUseCase

    @MockK
    lateinit var mockItemMapper: ItemModelMapper

    @MockK
    lateinit var mockMultiPrefs: MultiPlatformPreferencesWrapper

    private val testCoroutineDispatcher = TestCoroutineDispatcher()// use standard test dispatcher
    private val testCoroutineScope = TestCoroutineScope(TestCoroutineDispatcher())// use createCoroutineScope

    private val coroutines: CoroutineContextProvider =
        CoroutineContextTestProvider(testCoroutineDispatcher)
    private val playlistMutator: PlaylistMutator = PlaylistMutator()
    private val log: LogWrapper = SystemLogWrapper()

    private val fixture = JFixture()
    private lateinit var fixtCurrentPlaylist: PlaylistDomain
    private lateinit var fixtCurrentIdentifier: Identifier<Long>
    private val fixtCurrentSource: Source = fixture.build()
    private val fixtCurrentCurentIndex: Int = 1
    private lateinit var fixtCurrentPlaylistMapped: PlaylistContract.Model

    private lateinit var fixtNextPlaylist: PlaylistDomain
    private lateinit var fixtNextIdentifier: Identifier<Long>
    private val fixtNextSource: Source = LOCAL
    private val fixtNextCurrentIndex: Int = 5
    private lateinit var fixtNextPlaylistMapped: PlaylistContract.Model

    private lateinit var fixtPlaylistOrchestratorFlow: MutableSharedFlow<Triple<Operation, Source, PlaylistDomain>>
    private lateinit var fixtPlaylistItemOrchestratorFlow: MutableSharedFlow<Triple<Operation, Source, PlaylistItemDomain>>
    private lateinit var queueItemFlow: MutableStateFlow<PlaylistItemDomain?>
    private lateinit var queuePlaylistFlow: MutableSharedFlow<PlaylistDomain>

    private var _idCounter: Long = 0
    private val idCounter: Long
        get() {
            _idCounter++
            return _idCounter
        }
    private var fixtState: State? = null
    private lateinit var sut: PlaylistPresenter

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(testCoroutineDispatcher)
        // current
        fixtCurrentPlaylist = fixture.build<PlaylistDomain>()
        fixtCurrentPlaylist = fixtCurrentPlaylist.copy(
            currentIndex = fixtCurrentCurentIndex,
            type = PlaylistDomain.PlaylistTypeDomain.USER, // todo APP playlists
            mode = PlaylistDomain.PlaylistModeDomain.SINGLE,
            items = fixture.buildCollection<PlaylistItemDomain, List<PlaylistItemDomain>>(10)
                .map { it.copy(id = idCounter, order = idCounter * 1000, playlistId = fixtCurrentPlaylist.id) })
        fixtCurrentPlaylistMapped = fixture.build<PlaylistContract.Model>()
            .copy(
                items = fixture.buildCollection<PlaylistItemMviContract.Model.Item, List<PlaylistItemMviContract.Model.Item>>(
                    10
                )
            )
        fixtCurrentIdentifier = Identifier(fixtCurrentPlaylist.id!!, fixtCurrentSource)
        coEvery {
            mockPlaylistOrDefaultUsecase.getPlaylistOrDefault(
                fixtCurrentIdentifier.id,
                fixtCurrentIdentifier.source.flatOptions()
            )
        } returns (fixtCurrentPlaylist to fixtCurrentSource)
        log.d("cuurrent: id:${fixtCurrentIdentifier.id} opts:${fixtCurrentIdentifier.source.flatOptions()}")
        every {
            mockModelMapper.map(
                any(),
                any(),
                true,
                fixtCurrentIdentifier,
                any(),
                any(),
                any()
            )
        } returns fixtCurrentPlaylistMapped


        // next
        fixtNextPlaylist = fixture.build<PlaylistDomain>()
            .copy(
                type = PlaylistDomain.PlaylistTypeDomain.USER, // todo APP playlists
                currentIndex = fixtNextCurrentIndex,
                items = fixture.buildCollection(12)
            )
        fixtNextIdentifier = Identifier(fixtNextPlaylist.id!!, fixtNextSource)
        fixtNextPlaylistMapped = fixture.build<PlaylistContract.Model>()
            .copy(
                items = fixture.buildCollection<PlaylistItemMviContract.Model.Item, List<PlaylistItemMviContract.Model.Item>>(
                    12
                )
            )
        coEvery {
            mockPlaylistOrDefaultUsecase.getPlaylistOrDefault(
                fixtNextIdentifier.id,
                fixtNextIdentifier.source.flatOptions()
            )
        } returns (fixtNextPlaylist to fixtNextSource)
        every {
            mockModelMapper.map(
                any(),
                any(),
                true,
                fixtNextIdentifier,
                any(),
                any(),
                any()
            )
        } returns fixtNextPlaylistMapped

        // fixme this *should* work for app playlists (executeRefresh.if (it.first.type == APP) {)
        //coEvery{mockPlaylistOrchestrator.loadList(AllFilter(), Options(Source.LOCAL))} returns listOf(fixtCurrentPlaylist)

        coEvery {
            mockPlaylistOrchestrator.loadList(any<Filter.AllFilter>(), LOCAL.flatOptions())
        } returns listOf()// fixme: problems building the tree will need pt play with data

        // orchestrator
        fixtPlaylistOrchestratorFlow = MutableSharedFlow()
        fixtPlaylistItemOrchestratorFlow = MutableSharedFlow()
        every { mockPlaylistOrchestrator.updates } returns fixtPlaylistOrchestratorFlow
        every { mockPlaylistItemOrchestrator.updates } returns fixtPlaylistItemOrchestratorFlow

        // other
        queueItemFlow = MutableStateFlow(null)
        every { mockQueue.currentItemFlow } returns queueItemFlow
        queuePlaylistFlow = MutableSharedFlow()
        every { mockQueue.currentPlaylistFlow } returns queuePlaylistFlow

        // queue
        every { mockMultiPrefs.lastViewedPlaylistId } returns (Identifier(
            fixtCurrentIdentifier.id,
            fixtCurrentIdentifier.source
        ))
    }

    private fun createSut() {
        if (fixtState == null) {
            fixtState = createDefaultState()
        }
        sut = PlaylistPresenter(
            view = mockView,
            state = fixtState!!,
            mediaOrchestrator = mockMediaOrchestrator,
            addPlaylistUsecase = mockAddPlaylistUsecase,
            playlistOrchestrator = mockPlaylistOrchestrator,
            playlistItemOrchestrator = mockPlaylistItemOrchestrator,
            playlistUpdateUsecase = mockPlaylistUpdateUsecase,
            modelMapper = mockModelMapper,
            queue = mockQueue,
            toastWrapper = mockToastWrapper,
            ytCastContextHolder = mockYtContextHolder,
            chromeCastWrapper = mockChromeCastWrapper,
            ytJavaApi = mockYtJavaApi,
            shareWrapper = mockShareWrapper,
            playlistMutator = playlistMutator,
            log = log,
            timeProvider = mockTimeProvider,
            coroutines = coroutines,
            res = mockResources,
            dbInit = mockDbInit,
            playlistOrDefaultUsecase = mockPlaylistOrDefaultUsecase,
            recentLocalPlaylists = mockRecentLocalPlaylists,
            playUseCase = mockPlayUseCase,
            itemMapper = mockItemMapper,
            multiPrefs = mockMultiPrefs,
            appPlaylistInteractors = mapOf()
        )
        sut.onResume()
    }

    private fun createDefaultState() = State(
        playlistIdentifier = fixtCurrentIdentifier,
        playlist = fixtCurrentPlaylist,
    )

    private fun setCurrentlyPlaying(id: Identifier<Long>, playlist: PlaylistDomain, currentIndex: Int = playlist.currentIndex) {
        every { mockQueue.playlistId } returns id
        every { mockQueue.playlist } returns playlist
        every { mockQueue.currentItem } returns playlist.items.get(currentIndex)
        every { mockQueue.currentItemIndex } returns currentIndex
    }

    @After
    fun tearDown() {
        fixtState = null
    }

    @Test
    fun `test flow playlist header changed`() {
        testCoroutineScope.runTest {
            createSut()
            val fixChangedHeader = fixtCurrentPlaylist.copy(currentIndex = 3)

            // test
            fixtPlaylistOrchestratorFlow
                .emit((Operation.FLAT to fixtCurrentSource then fixChangedHeader))

            //verify
            assertThat(fixtState?.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size)
            assertThat(fixtState?.playlist!!.items).isEqualTo(fixtCurrentPlaylist.items)
            assertTrue(fixtState?.playlist?.matchesHeader(fixChangedHeader) ?: false)

            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), false, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setHeaderModel(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun `test flow playlist changed`() {
        runTest {
            createSut()
            val currentIndex = 3
            val fixChanged = fixture.build<PlaylistDomain>()
                .copy(id = fixtCurrentPlaylist.id, currentIndex = currentIndex, items = fixture.buildCollection(15))

            // test
            fixtPlaylistOrchestratorFlow
                .emit((Operation.FULL to fixtCurrentSource then fixChanged))

            //verify
            assertThat(fixtState?.playlist!!.items.size).isEqualTo(fixChanged.items.size)
            assertThat(fixtState?.playlist).isEqualTo(fixChanged)
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun `test flow playlist deleted`() {
        runTest {
            val fixtErrString: String = fixture.build()
            every { mockResources.getString(R.string.playlist_msg_deleted) } returns fixtErrString
            createSut()

            // test
            fixtPlaylistOrchestratorFlow
                .emit((Operation.DELETE to fixtCurrentSource then fixtCurrentPlaylist))

            //verify
            verify { mockToastWrapper.show(fixtErrString) }
            verify { mockView.exit() }
        }
    }

    @Test
    fun `test flow playlistitem changed not current`() {
        runTest {
            createSut()
            val replaceItemIndex = 3
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id,
                order = fixtCurrentPlaylist.items.get(replaceItemIndex).order
            )
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((Operation.FLAT to fixtCurrentSource then fixtChanged))

            //verify
            assertThat(fixtState!!.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size)
            assertThat(fixtState!!.playlist!!.items.get(replaceItemIndex)).isEqualTo(fixtChanged)
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun `test flow current playlistitem changed`() {
        runTest {
            createSut()
            val replaceItemIndex = fixtCurrentCurentIndex
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id,
                order = fixtCurrentPlaylist.items.get(replaceItemIndex).order
            )
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((Operation.FLAT to fixtCurrentSource then fixtChanged))

            //verify
            assertThat(fixtState!!.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size)
            assertThat(fixtState!!.playlist!!.items.get(replaceItemIndex)).isEqualTo(fixtChanged)
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun `test flow current playlistitem moved`() {
        runTest {
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
                .emit((Operation.FLAT to fixtCurrentSource then fixtChanged))

            //verify
            assertThat(fixtState!!.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size)
            assertThat(fixtState!!.playlist!!.items.get(targetItemIndex)).isEqualTo(fixtChanged)
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun `test flow current playlistitem moved out of playlist`() {
        runTest {
            createSut()
            val replaceItemIndex = fixtCurrentCurentIndex
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )
            //val nextItem = fixtCurrentPlaylist.items.get(replaceItemIndex + 1)

            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((Operation.FLAT to fixtCurrentSource then fixtChanged))


            //verify
            assertThat(fixtState!!.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(fixtState!!.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun `test flow playlistitem moved out of playlist before current`() {
        runTest {
            createSut()
            val replaceItemIndex = fixtCurrentCurentIndex - 1
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(replaceItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )

            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((Operation.FLAT to fixtCurrentSource then fixtChanged))

            //verify
            assertThat(fixtState!!.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(fixtState!!.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun `test flow playlistitem added to playlist before current`() {
        runTest {
            createSut()
            val addItemIndex = fixtCurrentCurentIndex - 1
            val fixtAdded: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixture.build(),
                playlistId = fixtCurrentPlaylist.id,
                order = fixtCurrentPlaylist.items.get(addItemIndex).order - 100
            )

            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((Operation.FLAT to fixtCurrentSource then fixtAdded))

            //verify
            assertThat(fixtState!!.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size + 1)
            assertThat(fixtState!!.playlist!!.items.find { it.id == fixtAdded.id }).isEqualTo(fixtAdded)
            assertThat(fixtState!!.playlist!!.items.indexOfFirst { it.id == fixtAdded.id }).isEqualTo(addItemIndex)
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun `test flow playlistitem deleted before current`() {
        runTest {
            createSut()
            val deleteItemIndex = fixtCurrentCurentIndex - 1
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(deleteItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )

            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((Operation.DELETE to fixtCurrentSource then fixtChanged))

            //verify
            assertThat(fixtState!!.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(fixtState!!.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun `test flow playlistitem deleted is current`() {
        runTest {
            createSut()
            val deleteItemIndex = fixtCurrentCurentIndex
            val fixtChanged: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().copy(
                id = fixtCurrentPlaylist.items.get(deleteItemIndex).id,
                playlistId = fixtCurrentPlaylist.id?.let { it + 100L }
            )
            //val expectedPlayingAfter = fixtCurrentPlaylist.items.get(deleteItemIndex + 1)
            // test
            fixtPlaylistItemOrchestratorFlow
                .emit((Operation.DELETE to fixtCurrentSource then fixtChanged))

            //verify
            assertThat(fixtState!!.playlist!!.items.size).isEqualTo(fixtCurrentPlaylist.items.size - 1)
            assertThat(fixtState!!.playlist!!.items.find { it.id == fixtChanged.id }).isNull()
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtState!!.playlist!!.currentIndex) }
        }
    }

    @Test
    fun onResume() {
        runTest {

            createSut()

            assertThat(fixtPlaylistOrchestratorFlow.subscriptionCount.value).isEqualTo(1)
            assertThat(fixtPlaylistItemOrchestratorFlow.subscriptionCount.value).isEqualTo(1)
            verify { mockYtContextHolder.addConnectionListener(any()) }
            assertThat(queuePlaylistFlow.subscriptionCount.value).isEqualTo(1)
        }
    }

    @Test
    fun onPause() {
        runTest {
            createSut()

            sut.onPause()


            assertThat(fixtPlaylistOrchestratorFlow.subscriptionCount.value).isEqualTo(0)
            assertThat(fixtPlaylistItemOrchestratorFlow.subscriptionCount.value).isEqualTo(0)
            verify { mockYtContextHolder.removeConnectionListener(any()) }
            assertThat(queuePlaylistFlow.subscriptionCount.value).isEqualTo(0)

            val fixtTestValue: PlaylistDomain = fixture.build()
            queuePlaylistFlow.emit(fixtTestValue)
            verify(exactly = 0) { mockView.highlightPlayingItem(fixtTestValue.currentIndex) }
        }
    }
//
//    @Test
//    fun initialise() {
//    }
//
//    @Test
//    fun refreshList() {
//    }
//
//    @Test
//    fun destroy() {
//    }
//
//    @Test
//    fun onItemSwipeRight() {
//
//    }
//
//    @Test
//    fun onPlaylistSelected() {
//    }
//
//    @Test
//    fun onPlayModeChange() {
//    }
//
//    @Test
//    fun onPlayPlaylist() {
//    }
//
//    @Test
//    fun onStarPlaylist() {
//    }
//
//    @Test
//    fun onFilterNewItems() {
//    }
//
//    @Test
//    fun onEdit() {
//    }
//
//    @Test
//    fun onFilterPlaylistItems() {
//    }

//    @Test
//    fun onItemViewClick() {
//    }
//
//    @Test
//    fun onItemClicked() {
//    }
//
//    @Test
//    fun onPlayStartClick() {
//    }
//
//    @Test
//    fun scroll() {
//    }
//
//    @Test
//    fun onItemPlay() {
//    }
//
//    @Test
//    fun onItemShowChannel() {
//    }
//
//    @Test
//    fun onItemStar() {
//        // todo
//    }
//
//    @Test
//    fun onItemShare() {
//    }
//
//    @Test
//    fun moveItem() {
//    }

    @Test
    fun `onItemSwipeLeft - delete item before current`() {
        runTest {
            createSut()
            val deletedItemIndex = 0
            val deletedItem = fixtCurrentPlaylist.items.get(deletedItemIndex)
            val fixtExpectedPlaylist = playlistMutator.remove(fixtCurrentPlaylist, deletedItem)
            val fixtExpectedMapped: PlaylistContract.Model = fixture.build()
            val fixtSwipeItemModel = fixture.build<PlaylistItemMviContract.Model.Item>().copy(id = deletedItem.id!!)
            setCurrentlyPlaying(fixtCurrentIdentifier, fixtExpectedPlaylist)
            every {
                mockModelMapper
                    .map(fixtExpectedPlaylist, any(), true, fixtCurrentIdentifier, any(), null, any())
            } returns fixtExpectedMapped //
            log.d("before:${fixtCurrentPlaylist.scanOrder()}")
            fixtState?.model = fixtCurrentPlaylistMapped.copy(
                itemsIdMap = fixtCurrentPlaylistMapped.itemsIdMap.toMutableMap()
                    .apply { put(fixtSwipeItemModel.id, deletedItem) }
            )
            // test
            sut.onItemSwipeLeft(fixtSwipeItemModel)
            testCoroutineDispatcher.advanceUntilIdle()
            fixtPlaylistItemOrchestratorFlow.emit(Operation.DELETE to fixtCurrentSource then deletedItem)
            queuePlaylistFlow.emit(fixtExpectedPlaylist)

            // verify
            log.d("state:${fixtState!!.playlist!!.scanOrder()}")
            log.d("expected:${fixtExpectedPlaylist.scanOrder()}")
            assertThat(fixtState!!.playlist!!).isEqualTo(fixtExpectedPlaylist)
            assertThat(fixtState!!.playlist!!.currentIndex).isEqualTo(fixtCurrentCurentIndex - 1)
            assertThat(fixtState!!.playlistIdentifier).isEqualTo(fixtCurrentIdentifier)
            verify {
                mockModelMapper
                    .map(fixtExpectedPlaylist, any(), true, fixtCurrentIdentifier, any(), null, any())
            }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtExpectedPlaylist.currentIndex) }
            coVerify {
                mockPlaylistItemOrchestrator.delete(deletedItem, LOCAL.flatOptions())
            }
            verify { mockView.setModel(fixtExpectedMapped) }
            assertThat(fixtState!!.deletedPlaylistItem).isEqualTo(deletedItem)
        }
    }

    @Test
    fun `commitMove ahead to behind - current playlist`() {// todo behind -> ahead
        runTest {
            fixtState = createDefaultState().copy(
                dragFrom = 2, dragTo = 0
            )
            createSut()
            val movedItem = fixtCurrentPlaylist.items.get(2)
            val fixtExpectedPlaylist = playlistMutator.moveItem(fixtCurrentPlaylist, 2, 0)
            val fixtExpectedChangedItem = fixtExpectedPlaylist.items[0]
            val fixtExpectedMapped: PlaylistContract.Model = fixture.build()
            setCurrentlyPlaying(fixtCurrentIdentifier, fixtExpectedPlaylist)
            every {
                mockModelMapper
                    .map(fixtExpectedPlaylist, any(), true, fixtCurrentIdentifier, any(), null, any())
            } returns fixtExpectedMapped //

            // test
            sut.commitMove()
            queuePlaylistFlow.emit(fixtExpectedPlaylist)

            // verify
            assertThat(fixtState!!.playlist!!).isEqualTo(fixtExpectedPlaylist)
            assertThat(fixtState!!.playlistIdentifier).isEqualTo(fixtCurrentIdentifier)
            assertThat(movedItem.id).isEqualTo(fixtExpectedChangedItem.id)
            verify {
                mockModelMapper
                    .map(fixtExpectedPlaylist, any(), true, fixtCurrentIdentifier, any(), any(), any())
            }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtExpectedPlaylist.currentIndex) }
            coVerify {
                mockPlaylistItemOrchestrator.save(fixtExpectedChangedItem, fixtCurrentSource.flatOptions())
            }
            verify { mockView.setModel(fixtExpectedMapped, false) }
            assertThat(fixtState!!.dragFrom).isNull()
            assertThat(fixtState!!.dragTo).isNull()
        }
    }

    @Test
    fun `setPlaylistData no playlist id - no item - no playNow - is current queued `() {
        runTest {
            fixtState = State()
            coEvery { mockDbInit.isInitialized() } returns true
            setCurrentlyPlaying(fixtCurrentIdentifier, fixtCurrentPlaylist, fixtCurrentPlaylist.currentIndex)
            createSut()

            // test
            sut.setPlaylistData()

            // verify
            assertThat(fixtState!!.playlist!!).isEqualTo(fixtCurrentPlaylist)
            assertThat(fixtState!!.playlistIdentifier).isEqualTo(fixtCurrentIdentifier)
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtCurrentIdentifier, any(), any(), any())
            }
            verify { mockView.setModel(fixtCurrentPlaylistMapped) }
            verify { mockView.scrollToItem(fixtCurrentPlaylist.currentIndex) }
            verify { mockView.highlightPlayingItem(fixtCurrentPlaylist.currentIndex) }
            verify { mockMultiPrefs.lastViewedPlaylistId }
            verify(exactly = 0) { mockRecentLocalPlaylists.addRecent(any()) }
        }

    }

    @Test
    fun `setPlaylistData playlist id - no item - no playNow`() {
        runTest {
            fixtState = State()
            setCurrentlyPlaying(fixtCurrentIdentifier, fixtCurrentPlaylist, fixtCurrentPlaylist.currentIndex)
            createSut()

            // test
            sut.setPlaylistData(fixtNextPlaylist.id, source = fixtNextSource)

            // verify
            assertThat(fixtState!!.playlist!!).isEqualTo(fixtNextPlaylist)
            assertThat(fixtState!!.playlistIdentifier).isEqualTo(fixtNextIdentifier)
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtNextIdentifier, any(), any(), any())
            }
            verify { mockView.setModel(fixtNextPlaylistMapped) }
            verify { mockView.scrollToItem(fixtNextPlaylist.currentIndex) }
            verify { mockView.highlightPlayingItem(fixtNextPlaylist.currentIndex) }
            verify { mockMultiPrefs.lastViewedPlaylistId = fixtNextIdentifier }
            verify() { mockRecentLocalPlaylists.addRecent(fixtNextPlaylist) }
        }
    }

    @Test
    fun `setPlaylistData playlist id - has item - no playNow`() {
        runTest {
            fixtState = State()
            setCurrentlyPlaying(fixtCurrentIdentifier, fixtCurrentPlaylist, fixtCurrentPlaylist.currentIndex)
            createSut()
            val selectedItemIndex = 8

            // test
            sut.setPlaylistData(
                fixtNextPlaylist.id,
                fixtNextPlaylist.items[selectedItemIndex].id,
                source = fixtNextSource
            )

            // verify
            assertThat(fixtState!!.playlist!!).isEqualTo(fixtNextPlaylist)
            assertThat(fixtState!!.playlistIdentifier).isEqualTo(fixtNextIdentifier)
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtNextIdentifier, any(), any(), any())
            }
            verify { mockView.setModel(fixtNextPlaylistMapped) }
//            verify { mockView.scrollToItem(selectedItemIndex) }
            verify { mockView.scrollToItem(fixtNextPlaylist.currentIndex) }
            verify { mockView.highlightPlayingItem(fixtNextPlaylist.currentIndex) }
            verify { mockMultiPrefs.lastViewedPlaylistId = fixtNextIdentifier }
            verify { mockRecentLocalPlaylists.addRecent(fixtNextPlaylist) }
        }
    }

    @Test
    fun `setPlaylistData playlist id - has item - playNow`() {
        runTest {
            fixtState = State()
            setCurrentlyPlaying(fixtCurrentIdentifier, fixtCurrentPlaylist, fixtCurrentPlaylist.currentIndex)
            createSut()
            val selectedItemIndex = 8
            val plItem = fixtNextPlaylist.items[selectedItemIndex]

            // test
            sut.setPlaylistData(fixtNextPlaylist.id, plItem.id, true, source = fixtNextSource)
            setCurrentlyPlaying(fixtNextIdentifier, fixtNextPlaylist, selectedItemIndex)
            queuePlaylistFlow.emit(fixtNextPlaylist.copy(currentIndex = selectedItemIndex))

            // verify
            assertThat(fixtState!!.playlist!!).isEqualTo(fixtNextPlaylist)
            assertThat(fixtState!!.playlistIdentifier).isEqualTo(fixtNextIdentifier)
            coVerify { mockQueue.playNow(fixtNextIdentifier, plItem.id) }
            verify {
                mockModelMapper
                    .map(fixtState?.playlist!!, any(), true, fixtNextIdentifier, any(), any(), any())
            }
            verify { mockView.setModel(fixtNextPlaylistMapped) }
//            verify { mockView.scrollToItem(selectedItemIndex) }
            verify { mockView.scrollToItem(fixtNextPlaylist.currentIndex) }
            verify { mockView.highlightPlayingItem(selectedItemIndex) } // comes from emitter
            verify { mockMultiPrefs.lastViewedPlaylistId = fixtNextIdentifier }
            verify { mockRecentLocalPlaylists.addRecent(fixtNextPlaylist) }
        }
    }

    @Test
    fun `undoDelete - item before current`() {
        runTest {
            createSut()
            val deletedItem = fixture.build<PlaylistItemDomain>().copy(
                id = idCounter,
                order = fixtCurrentPlaylist.items.get(0).order - 100,
                playlistId = fixtCurrentPlaylist.id
            )
            fixtState!!.deletedPlaylistItem = deletedItem
            val fixtExpectedPlaylist = playlistMutator.addOrReplaceItem(fixtCurrentPlaylist, deletedItem)
            val fixtExpectedMapped: PlaylistContract.Model = fixture.build()
            setCurrentlyPlaying(fixtCurrentIdentifier, fixtExpectedPlaylist)
            every {
                mockModelMapper
                    .map(fixtExpectedPlaylist, any(), true, fixtCurrentIdentifier, any(), any(), any())
            } returns fixtExpectedMapped //
            log.d("before:${fixtCurrentPlaylist.scanOrder()}")
            // test
            sut.undoDelete()
            testCoroutineDispatcher.advanceUntilIdle()
            fixtPlaylistItemOrchestratorFlow.emit(Operation.FLAT to fixtCurrentSource then deletedItem)
            queuePlaylistFlow.emit(fixtExpectedPlaylist)

            // verify
            assertThat(fixtState!!.playlist!!).isEqualTo(fixtExpectedPlaylist)
            assertThat(fixtState!!.playlist!!.currentIndex).isEqualTo(fixtCurrentCurentIndex + 1)
            assertThat(fixtState!!.playlistIdentifier).isEqualTo(fixtCurrentIdentifier)
            verify {
                mockModelMapper
                    .map(fixtExpectedPlaylist, any(), true, fixtCurrentIdentifier, any(), any(), any())
            }
            verify(exactly = 0) { mockView.scrollToItem(any()) }
            verify { mockView.highlightPlayingItem(fixtExpectedPlaylist.currentIndex) }
            coVerify {
                mockPlaylistItemOrchestrator.save(deletedItem, LOCAL.deepOptions())
            }
            verify { mockView.setModel(fixtExpectedMapped) }
            assertThat(fixtState!!.deletedPlaylistItem).isNull()
        }
    }
}