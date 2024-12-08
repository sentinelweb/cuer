package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.EmptySkipView
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.player.PlayerComposeables
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.sleep.SleepPreventer
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class VlcPlayerUiCoordinator(
    private val parent: HomeUiCoordinator,
) : PlayerContract.View,
    UiCoordinator<Model>,
    BaseMviView<Model, Event>(),
    DesktopScopeComponent,
    KoinComponent {

    override val scope: Scope = desktopScopeWithSource(this)

    override var modelObservable = MutableStateFlow(Model.Initial)
        private set

    private val controller: PlayerController by scope.inject()
    private val log: LogWrapper by inject()
    private val lifecycle: LifecycleRegistry by inject()

    private val playerItemLoader: FolderMemoryPlaylistItemLoader by inject()
    private val playlistOrchestrator: PlaylistOrchestrator by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val queueProducer: QueueMediatorContract.Producer by inject()
    private val sleepPreventer: SleepPreventer by inject()

    private lateinit var playerWindow: VlcPlayerSwingWindow

    private var playlistId: OrchestratorContract.Identifier<GUID>? = null
    private lateinit var screen: PlayerNodeDomain.Screen

    @Composable
    fun PlayerDesktopUi() {
        val state = modelObservable
            .collectAsState(Model.Initial, coroutines.Main)

        PlayerComposeables.PlayerTransport(
            state.value,
            this@VlcPlayerUiCoordinator,
            contentColor = MaterialTheme.colorScheme.onSurface,
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.height(100.dp)
        )
    }

    @Composable
    fun PlayerSystrayUi() {
        val state = modelObservable
            .onEach { log.d("PlayerDesktopUi:${it.playState}") }
            .collectAsState(Model.Initial)

        PlayerComposeables.PlayerTransport(
            state.value,
            this@VlcPlayerUiCoordinator,
            contentColor = MaterialTheme.colorScheme.onSurface,
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.height(100.dp)
        )
    }

    override fun create() {
        log.tag(this)
        lifecycle.onCreate()
        controller.onViewCreated(listOf(this), lifecycle)
        lifecycle.onStart()
        lifecycle.onResume()
        playerWindow = if (VlcPlayerSwingWindow.checkShowWindow()) {
            scope.get<VlcPlayerSwingWindow>(VlcPlayerSwingWindow::class)
                .apply { assemble(screen) }
                .also { sleepPreventer.preventSleep() }
        } else error("Can't find VLC")
    }

    override fun destroy() {
        coroutines.mainScope.launch {
            playlistId
                ?.takeIf { it.source == MEMORY }
                ?.also { playlistOrchestrator.delete(it.id, it.deepOptions()) }
            playerWindow.destroy()
            sleepPreventer.allowSleep()
            lifecycle.onPause()
            lifecycle.onStop()
            controller.onViewDestroyed()
            controller.onDestroy(endSession = true)
            queueProducer.resetQueue()
            lifecycle.onDestroy()
            scope.close()
            coroutines.cancel()
        }
    }

    override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
        log.d("processLabel: $label")
        when (label) {
            is Command -> playerWindow.playStateChanged(label.command)
            Stop -> destroyPlayerWindow()
            FocusWindow -> focusPlayerWindow()
            else -> log.d("Unprocessed label: $label")
        }
    }

    val modelDiffer: ViewRenderer<Model> = diff {
        diff(get = Model::playState, set = {
            playerWindow.updateUiPlayState(it)
        })
        diff(get = Model::volume, set = {
            playerWindow.updateVolume(it)
        })
        diff(get = Model::times, set = {
            playerWindow.updateUiTimes(it)
        })
        diff(get = Model::texts, set = {
            playerWindow.updateTexts(it)
        })
        diff(get = Model::buttons, set = {
            playerWindow.updateButtons(it)
        })
    }

    override val renderer: ViewRenderer<Model> = object : ViewRenderer<Model> {
        override fun render(model: Model) {
            log.d("coord.renderer: ${model.playState}")
            modelObservable.value = model
            modelDiffer.render(model)
        }
    }

    fun focusPlayerWindow() {
        playerWindow.doFocus()
    }

    fun destroyPlayerWindow() {
        parent.killPlayer()
    }

    fun setupPlaylistAndItem(
        item: PlaylistItemDomain,
        playlist: PlaylistDomain,
        screen: PlayerNodeDomain.Screen
    ) {
        coroutines.mainScope.launch {
            playlistId = playlist.id
            log.d("showPlayer: id:${playlist.id}")
            if (playlist.id?.source == MEMORY) {
                playlistOrchestrator.save(playlist, playlist.id!!.deepOptions())
            }
            log.d("showPlayer: play itemId:${item.id?.serialise()}")
            playerItemLoader.setPlaylistAndItem(
                PlaylistAndItemDomain(
                    playlistId = playlist.id,
                    playlistTitle = playlist.title,
                    item = item
                )
            )
            this@VlcPlayerUiCoordinator.screen = screen
            create() // initialises the player controller
        }
    }

    companion object {
        const val PREFERRED_SCREEN_DEFAULT = 1

        val uiModule = module {
            factory { (parent: HomeUiCoordinator) -> VlcPlayerUiCoordinator(parent) }
            single { FolderMemoryPlaylistItemLoader() }
            single<PlayerContract.PlaylistItemLoader> { get<FolderMemoryPlaylistItemLoader>() }

            scope(named<VlcPlayerUiCoordinator>()) {
                scoped {
                    PlayerController(
                        queueConsumer = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = get<VlcPlayerUiCoordinator>().lifecycle as Lifecycle,
                        log = get(),
                        mediaSessionListener = get(),
                        playSessionListener = get(),
                        store = get()
                    )
                }
                scoped {
                    PlayerStoreFactory(
                        // storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(),
                        itemLoader = get(),
                        queueConsumer = get(),
                        queueProducer = get(),
                        skip = get(),
                        coroutines = get(),
                        log = get(),
                        livePlaybackController = get(),
                        mediaSessionManager = get(),
                        mediaSessionListener = get(),
                        mediaOrchestrator = get(),
                        playlistItemOrchestrator = get(),
                        playerSessionManager = get(),
                        playerSessionListener = get(),
                        config = PlayerContract.PlayerConfig(maxVolume = 200f),
                        prefs = get()
                    ).create()
                }
                scoped<SkipContract.External> {
                    SkipPresenter(
                        view = EmptySkipView(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = get(),
                        prefsWrapper = get()
                    )
                }
                factory {
                    VlcPlayerSwingWindow(
                        coordinator = get(),
                        folderListUseCase = get(),
                        showHideControls = VlcPlayerShowHideControls(),
                        keyMap = VlcPlayerKeyMap(),
                        localRepository = get(),
                        coroutineContextProvider = get()
                    )
                }
            }
        }
    }
}
