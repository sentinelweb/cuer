package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonCreator
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.Command
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory
import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class VlcPlayerUiCoordinator(
    private val parent: HomeUiCoordinator,
) :
    PlayerContract.View,
    UiCoordinator<Model>,
    BaseMviView<Model, Event>(),
    DesktopScopeComponent,
    KoinComponent {

    override val scope: Scope = desktopScopeWithSource(this)

    override var modelObservable = MutableStateFlow(Model.blankModel())
        private set

    private val controller: PlayerController by scope.inject()
    private val log: LogWrapper by inject()
    private val lifecycle: LifecycleRegistry by inject()
    private lateinit var playerWindow: VlcPlayerSwingWindow

    override fun create() {
        log.tag(this)
        lifecycle.onCreate()
        controller.onViewCreated(listOf(this), lifecycle)
        lifecycle.onStart()
        lifecycle.onResume()
        playerWindow = VlcPlayerSwingWindow.showWindow(this) ?: throw IllegalStateException("Cant find VLC")
    }

    override fun destroy() {
        playerWindow.destroy()
        lifecycle.onPause()
        lifecycle.onStop()
        lifecycle.onDestroy()
        scope.close()
    }

    override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
        when (label) {
            is Command -> playerWindow.playStateChanged(label.command)
            else -> log.d("Unprocessed label: $label")
        }
    }

    override val renderer: ViewRenderer<Model> = diff {
        diff(get = Model::playState, set = {
            playerWindow.updateUiPlayState(it)
        })
        diff(get = Model::times, set = {
            playerWindow.updateUiTimes(it)
        })

    }

//    override fun render(model: Model) {
//        this.modelObservable.value = model
//    }

    fun playerWindowDestroyed() {
        parent.killPlayer()
    }

    companion object {
        val uiModule = module {
            factory { (parent: HomeUiCoordinator) -> VlcPlayerUiCoordinator(parent) }
            factory<AppPlaylistInteractor.CustomisationResources>(named(NewItems)) { EmptyCustomisationResources() }
            factory<AppPlaylistInteractor.CustomisationResources>(named(Starred)) { EmptyCustomisationResources() }
            factory<AppPlaylistInteractor.CustomisationResources>(named(Unfinished)) { EmptyCustomisationResources() }
            factory<RibbonCreator> { EmptyRibbonCreator() }
            factory<LivePlaybackContract.Controller> { EmptyLivePlaybackController() }
            factory<SkipContract.External> { EmptySkip() }
            factory<MediaSessionContract.Manager> { EmptyMediaSessionManager() }
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
                        playControls = get(),
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
                        playerControls = get(),
                        mediaOrchestrator = get(),
                        playlistItemOrchestrator = get()
                    ).create()
                }
            }
        }
    }
}
