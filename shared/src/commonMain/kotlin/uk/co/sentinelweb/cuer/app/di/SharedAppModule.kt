package uk.co.sentinelweb.cuer.app.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.backup.BackupCheck
import uk.co.sentinelweb.cuer.app.backup.version.ParserFactory
import uk.co.sentinelweb.cuer.app.db.repository.file.PlatformFileOperation
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.LocalSearchPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.NewMediaPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RecentItemsPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RemoteSearchPlayistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.util.*
import uk.co.sentinelweb.cuer.app.queue.QueueMediator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorState
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseRecentCategories
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.util.link.LinkExtractor
import uk.co.sentinelweb.cuer.app.util.link.TimecodeExtractor
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapperImpl
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists

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
                playlistOrDefaultOrchestrator = get(),
                recentLocalPlaylists = get()
            )
        }
        single { get<QueueMediatorContract.Producer>() as QueueMediatorContract.Consumer }
    }

    private val orchestratorModule = module {
        single { PlaylistOrchestrator(get(), get(), get()) }
        single { PlaylistItemOrchestrator(get(), get(), get()) }
        single { MediaOrchestrator(get(), get(), get()) }
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
        factory {
            RemoteSearchPlayistOrchestrator(
                get(),
                get(),
                get(),
                RemoteSearchPlayistOrchestrator.State()
            )
        }
        factory { PlaylistMediaUpdateOrchestrator(get()) }
        factory { PlaylistOrDefaultOrchestrator(get(), get()) }
    }

    private val objectModule = module {
        factory { ParserFactory() }
        single { PlaylistMemoryRepository(get(), get(), get(), get(), get()) }
        single { get<PlaylistMemoryRepository>().playlistItemMemoryRepository }
        single { get<PlaylistMemoryRepository>().mediaMemoryRepository }
        single<MultiPlatformPreferencesWrapper> { MultiPlatformPreferencesWrapperImpl() }
        factory { BrowseRecentCategories(get(), get()) }
        factory { RecentLocalPlaylists(get(), get()) }
        factory { PlatformFileOperation() }
        factory { LinkExtractor() }
        factory { TimecodeExtractor() }
        factory { BackupCheck(get(), get(), get()) }
    }

    val modules = listOf(objectModule)
        .plus(orchestratorModule)
        .plus(queueModule)
        .plus(DescriptionContract.viewModule)
}
