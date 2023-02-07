package uk.co.sentinelweb.cuer.app.factory

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module
import summarise
import uk.co.sentinelweb.cuer.app.impl.IosStringDecoder
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor.CustomisationResources
import uk.co.sentinelweb.cuer.app.ui.browse.*
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.playlist.*
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviController
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviStoreFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogViewModel
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.resources.NewPlaylistCustomisationResources
import uk.co.sentinelweb.cuer.app.ui.resources.StarredPlaylistCustomisationResources
import uk.co.sentinelweb.cuer.app.ui.resources.UnfinishedPlaylistCustomisationResources
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.cast.listener.CastPlayerContextHolder
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

object PresentationModule {

    private val browserModule = module {
//        factory {LifecycleRegistryKt.LifecycleRegistry()}
        factory { (lifecycle: Lifecycle) ->
            BrowseController(
                storeFactory = get(),
                modelMapper = get(),
                lifecycle = lifecycle,// todo supply from ios// get<BrowseFragment>().lifecycle.asEssentyLifecycle(),
                log = get()
            )
        }
        factory {
            BrowseStoreFactory(
                storeFactory = LoggingStoreFactory(DefaultStoreFactory()),
//                storeFactory = DefaultStoreFactory(),
                repository = get(),
                playlistOrchestrator = get(),
                playlistStatsOrchestrator = get(),
                browseStrings = get(),
                log = get(),
                prefs = get(),
                recentCategories = get()
            )
        }
        factory<BrowseContract.Strings> { BrowseStrings() }
        factory { BrowseRepository(BrowseRepositoryJsonLoader(get()), "browse_categories") }
        factory { BrowseModelMapper(get(), get()) }
    }

    class BrowseStrings : BrowseContract.Strings {
        override val allCatsTitle: String
            get() = "All Categories"
        override val recent: String
            get() = "Recent"
        override val errorNoPlaylistConfigured = "No playlist configured"
        override fun errorNoCatWithID(id: Long) = "Cant find that category"
    }

    private val playlistsModule = module {
        factory { (lifecycle: Lifecycle) ->
            PlaylistsMviController(
                store = get(),
                modelMapper = get(),
                lifecycle = lifecycle,
                log = get(),
                playlistOrchestrator = get(),
            )
        }
        factory {
            PlaylistsMviStoreFactory(
                storeFactory = DefaultStoreFactory(),
                playlistOrchestrator = get(),
                playlistStatsOrchestrator = get(),
                coroutines = get(),
                log = get(),
                prefsWrapper = get(),
                newMedia = get(),
                recentItems = get(),
                localSearch = get(),
                remoteSearch = get(),
                recentLocalPlaylists = get(),
                starredItems = get(),
                unfinishedItems = get(),
                strings = get(),
                platformLauncher = get(),
                shareWrapper = get(),
                merge = get()
            ).create()
        }
        factory { PlaylistsMviContract.Strings() }
        factory { PlaylistsMviModelMapper(get()) }
    }

    private val resourcesModule = module {
        factory<CustomisationResources>(named(MemoryPlaylist.NewItems)) { NewPlaylistCustomisationResources() }
        factory<CustomisationResources>(named(MemoryPlaylist.Starred)) { StarredPlaylistCustomisationResources() }
        factory<CustomisationResources>(named(MemoryPlaylist.Unfinished)) { UnfinishedPlaylistCustomisationResources() }
        factory<StringDecoder> { IosStringDecoder() }
    }

    val playlistsDialogModule = module {
        factory {
            PlaylistsDialogViewModel(
                state = get(),
                playlistOrchestrator = get(),
                playlistStatsOrchestrator = get(),
                modelMapper = get(),
                dialogModelMapper = get(),
                log = get(),
                prefsWrapper = get(),
                coroutines = get(),
                recentLocalPlaylists = get()
            )
        }
        factory { PlaylistsModelMapper(get()) }
        factory { PlaylistsDialogModelMapper() }
        factory { PlaylistsMviDialogContract.State() }
        factory { PlaylistsMviDialogContract.Strings() }
    }

    private val playlistModule = module {
        factory { (lifecycle: Lifecycle) ->
            PlaylistMviController(
                modelMapper = get(),
                log = get(),
                store = get(),
                playlistOrchestrator = get(),
                playlistItemOrchestrator = get(),
                mediaOrchestrator = get(),
                queue = get(),
                lifecycle = lifecycle,
            )
        }
        factory {
            PlaylistMviStoreFactory(
                storeFactory = DefaultStoreFactory(),
                coroutines = get(),
                log = get(),
                playlistOrchestrator = get(),
                playlistItemOrchestrator = get(),
                playlistUpdateUsecase = get(),
                playlistOrDefaultUsecase = get(),
                prefsWrapper = get(),
                dbInit = get(),
                recentLocalPlaylists = get(),
                queue = get(),
                appPlaylistInteractors = get(),
                playlistMutator = get(),
                util = get(),
                modelMapper = get(),
                itemModelMapper = get(),
                playUseCase = get(),
                strings = get(),
                timeProvider = get(),
                addPlaylistUsecase = get(),
                multiPrefs = get(),
                idGenerator = get(),
            ).create()
        }
        factory {
            PlaylistMviModelMapper(
                itemModelMapper = get(),
                iconMapper = get(),
                strings = get(),
                appPlaylistInteractors = get(),
                util = get(),
                multiPlatformPreferences = get(),
                log = get(),
            )
        }
        factory {
            PlaylistMviItemModelMapper(
                timeSinceFormatter = get(),
                timeFormatter = get(),
                durationTextColorMapper = get(),
                stringDecoder = get(),
                log = get(),
            )
        }
        factory {
            PlaylistMviUtil(
                queue = get(),
                ytCastContextHolder = get(),
                multiPrefs = get(),
            )
        }
        factory {
            PlayUseCase(
                queue = get(),
                ytCastContextHolder = get(),
                prefsWrapper = get(),
                coroutines = get(),
                floatingService = get(),
                playDialog = get(),
                strings = get()
            )
        }
        factory<PlayUseCase.Dialog> {
            object : PlayUseCase.Dialog {
                val log: LogWrapper = get()

                init {
                    log.tag(this)
                }

                override var playUseCase: PlayUseCase
                    get() = get()
                    set(value) {}

                override fun showPlayDialog(item: PlaylistItemDomain?, playlistTitle: String?) {
                    log.d("showPlayDialog: title: $playlistTitle item: ${item?.summarise()}")
                }

                override fun showDialog(model: AlertDialogModel) {
                    log.d("showDialog: model: ${model.title}")
                }

            }
        }
        factory<CastPlayerContextHolder> {
            object : CastPlayerContextHolder {
                val log: LogWrapper = get()

                init {
                    log.tag(this)
                }

                override var playerUi: PlayerContract.PlayerControls?
                    get() = null
                    set(value) {}

                override fun create() {
                    log.d("create()")
                }

                override fun isCreated(): Boolean {
                    log.d("isCreated()")
                    return false
                }

                override fun isConnected(): Boolean {
                    log.d("isConnected()")
                    return false
                }

                override fun onDisconnected() {
                    log.d("onDisconnected()")
                }

                override fun destroy() {
                    log.d("destroy()")
                }

            }
        }
        factory<FloatingPlayerContract.Manager> {

            object : FloatingPlayerContract.Manager {
                val log = SystemLogWrapper()// get() below overrides

                init {
                    log.tag(this)
                }

                override fun get(): FloatingPlayerContract.Service? = null

                override fun isRunning(): Boolean = false

                override fun playItem(item: PlaylistItemDomain) {
                    log.d("playItem: ${item.summarise()}")
                }
            }
        }
    }

    val modules = listOf(browserModule, playlistsModule, resourcesModule, playlistsDialogModule, playlistModule)
}