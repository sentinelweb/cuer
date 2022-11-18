package uk.co.sentinelweb.cuer.app.factory

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.browse.*

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

    class BrowseStrings() : BrowseContract.Strings {
        override val allCatsTitle: String
            get() = "All Categories"
        override val recent: String
            get() = "Recent"
        override val errorNoPlaylistConfigured = "No playlist configured"
        override fun errorNoCatWithID(id: Long) = "Cant find that category"
    }

    val modules = listOf(browserModule)
//        .plus(SharedAppIosModule.utilModule)
//        .plus(SharedAppIosModule.netModule)

//    object:Lifecycle {
//        override val state: Lifecycle.State
//            get() = Lifecycle.State.STARTED
//
//        override fun subscribe(callbacks: Lifecycle.Callbacks) {
//            // todo connect to apple app lifecycle
//            get<LogWrapper>().d("subscribe(callbacks: Lifecycle.Callbacks)")
//        }
//
//        override fun unsubscribe(callbacks: Lifecycle.Callbacks) {
//            // todo connect to apple app lifecycle
//            get<LogWrapper>().d("subscribe(callbacks: Lifecycle.Callbacks)")
//        }
//    }
}