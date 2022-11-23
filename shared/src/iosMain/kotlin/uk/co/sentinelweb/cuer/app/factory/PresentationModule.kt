package uk.co.sentinelweb.cuer.app.factory

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor.CustomisationResources
import uk.co.sentinelweb.cuer.app.ui.browse.*
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviController
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviStoreFactory
import uk.co.sentinelweb.cuer.app.ui.resources.NewPlaylistCustomisationResources
import uk.co.sentinelweb.cuer.app.ui.resources.StarredPlaylistCustomisationResources
import uk.co.sentinelweb.cuer.app.ui.resources.UnfinishedPlaylistCustomisationResources

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
        factory<CustomisationResources>(named(PlaylistMemoryRepository.MemoryPlaylist.NewItems)) { NewPlaylistCustomisationResources() }
        factory<CustomisationResources>(named(PlaylistMemoryRepository.MemoryPlaylist.Starred)) { StarredPlaylistCustomisationResources() }
        factory<CustomisationResources>(named(PlaylistMemoryRepository.MemoryPlaylist.Unfinished)) { UnfinishedPlaylistCustomisationResources() }
    }

    val modules = listOf(browserModule, playlistsModule, resourcesModule)
}