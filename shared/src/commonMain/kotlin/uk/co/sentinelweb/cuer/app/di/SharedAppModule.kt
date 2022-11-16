package uk.co.sentinelweb.cuer.app.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.backup.BackupCheck
import uk.co.sentinelweb.cuer.app.backup.BackupUseCase
import uk.co.sentinelweb.cuer.app.backup.IBackupJsonManager
import uk.co.sentinelweb.cuer.app.backup.version.ParserFactory
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.db.init.JsonDatabaseInitializer
import uk.co.sentinelweb.cuer.app.db.repository.file.PlatformFileOperation
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
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

    private val dbModule = module {
        single<DatabaseInitializer> { JsonDatabaseInitializer(get(), get(), get(), get(), get()) }
    }

    private val orchestratorModule = module {
        single { PlaylistOrchestrator(get(), get()) }
        single { PlaylistItemOrchestrator(get(), get()) }
        single { MediaOrchestrator(get(), get()) }
        single { ChannelOrchestrator(get(), get()) }
        single { PlaylistStatsOrchestrator(get()) }
        factory { PlaylistUpdateOrchestrator(get(), get(), get(), get(), get(), get(), get()) }
        factory<PlaylistUpdateOrchestrator.UpdateCheck> { PlaylistUpdateOrchestrator.PlatformUpdateCheck() }
        factory { PlaylistMergeOrchestrator(get(), get()) }
        factory { PlaylistMediaLookupOrchestrator(get(), get(), get()) }
        single { NewMediaPlayistInteractor(get(), get(), get(), get(named(NewItems))) }
        single { RecentItemsPlayistInteractor(get(), get()) }
        single { StarredItemsPlayistInteractor(get(), get(), get(), get(named(Starred))) }
        single { UnfinishedItemsPlayistInteractor(get(), get(), get(), get(named(Unfinished))) }
        factory { AddLinkOrchestrator(get(), get(), get(), get(), get()) }
        single { LocalSearchPlayistInteractor(get(), get(), get()) }
        single {
            YoutubeSearchPlayistInteractor(
                get(),
                get(),
                get(),
                YoutubeSearchPlayistInteractor.State()
            )
        }
        factory { PlaylistMediaUpdateOrchestrator(get()) }
        factory { PlaylistOrDefaultOrchestrator(get(), get()) }
        factory {
            mapOf(
                NewItems.id to get<NewMediaPlayistInteractor>(),
                Recent.id to get<RecentItemsPlayistInteractor>(),
                LocalSearch.id to get<LocalSearchPlayistInteractor>(),
                YoutubeSearch.id to get<YoutubeSearchPlayistInteractor>(),
                Starred.id to get<StarredItemsPlayistInteractor>(),
                Unfinished.id to get<UnfinishedItemsPlayistInteractor>(),
            )
        }
    }

    private val objectModule = module {
        factory { ParserFactory() }
        single { PlaylistMemoryRepository(get(), get(), get(), get(), get(), get(), get()) }
        single { get<PlaylistMemoryRepository>().playlistItemMemoryRepository }
        single { get<PlaylistMemoryRepository>().mediaMemoryRepository }
        single<MultiPlatformPreferencesWrapper> { MultiPlatformPreferencesWrapperImpl() }
        factory { BrowseRecentCategories(get(), get()) }
        factory { RecentLocalPlaylists(get(), get()) }
        factory { PlatformFileOperation() }
        factory { LinkExtractor() }
        factory { TimecodeExtractor() }
        factory { BackupCheck(get(), get(), get(), get()) }
        factory<IBackupJsonManager> {
            BackupUseCase(
                channelRepository = get(),
                mediaRepository = get(),
                playlistRepository = get(),
                playlistItemRepository = get(),
                imageDatabaseRepository = get(),
                contextProvider = get(),
                parserFactory = get(),
                playlistItemCreator = get(),
                timeProvider = get(),
                log = get()
            )
        }
    }

    val modules = listOf(objectModule)
        .plus(orchestratorModule)
        .plus(queueModule)
        .plus(dbModule)
        .plus(DescriptionContract.viewModule)
}
