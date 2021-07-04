package uk.co.sentinelweb.cuer.app.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.backup.version.ParserFactory
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.MemoryRepository
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.LocalSearchPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.NewMediaPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RecentItemsPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RemoteSearchPlayistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.util.*
import uk.co.sentinelweb.cuer.app.queue.QueueMediator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorState
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

object SharedAppModule {
    private val queueModule = module {
        single<QueueMediatorContract.Producer> {
            QueueMediator(
                state = QueueMediatorState(),
                playlistOrchestrator = get(),
                playlistItemOrchestrator = get(),
                coroutines = get(),
                playlistMutator = get(),
                log = get(),
                prefsWrapper = get(),
                mediaUpdate = get(),
                playlistOrDefaultOrchestrator = get()
            )
        }
        single { get<QueueMediatorContract.Producer>() as QueueMediatorContract.Consumer }
    }

    private val orchectratorModule = module {
        single { PlaylistOrchestrator(get(), get(), get()) }
        single { PlaylistItemOrchestrator(get(), get(), get()) }
        single { MediaOrchestrator(get(), get()) }
        single { ChannelOrchestrator(get(), get()) }
        single { PlaylistStatsOrchestrator(get()) }
        factory { PlaylistUpdateOrchestrator(get(), get(), get(), get(), get(), get(), get()) }
        factory<PlaylistUpdateOrchestrator.UpdateCheck> { PlaylistUpdateOrchestrator.PlatformUpdateCheck() }
        factory { PlaylistMergeOrchestrator(get(), get()) }
        factory { PlaylistMediaLookupOrchestrator(get(), get()) }
        factory { NewMediaPlayistInteractor(get()) }
        factory { RecentItemsPlayistInteractor(get()) }
        factory { AddLinkOrchestrator(get(), get(), get(), get(), get()) }
        factory { LocalSearchPlayistInteractor(get(), get()) }
        factory { RemoteSearchPlayistOrchestrator(get(), get(), get(), RemoteSearchPlayistOrchestrator.State()) }
        factory { PlaylistMediaUpdateOrchestrator(get()) }
        factory { PlaylistOrDefaultOrchestrator(get(), get()) }
    }

    private val objectModule = module {
        factory { ParserFactory() }
        single { PlaylistMemoryRepository(get(), get(), get(), get(), get()) }
        single<MemoryRepository<PlaylistItemDomain>> { get<PlaylistMemoryRepository>().playlistItemMemoryRepository }
    }

    val modules = listOf(objectModule)
        .plus(orchectratorModule)
        .plus(queueModule)
        .plus(DescriptionContract.viewModule)

}