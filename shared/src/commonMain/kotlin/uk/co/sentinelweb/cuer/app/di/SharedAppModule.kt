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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Inject.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
import uk.co.sentinelweb.cuer.app.queue.QueueMediator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorState
import uk.co.sentinelweb.cuer.app.service.remote.AvailableMessageHandler
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.service.remote.WifiStartChecker
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseRecentCategories
import uk.co.sentinelweb.cuer.app.ui.common.mapper.DurationTextColorMapper
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.ui.playlist.IdGenerator
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingPresenter
import uk.co.sentinelweb.cuer.app.usecase.*
import uk.co.sentinelweb.cuer.app.util.link.LinkExtractor
import uk.co.sentinelweb.cuer.app.util.link.TimecodeExtractor
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapperImpl
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.domain.*

object SharedAppModule {
    private val queueModule = module {
        single<QueueMediatorContract.Producer> {
            QueueMediator(
                state = QueueMediatorState(),
                playlistOrchestrator = get(named(PlaylistOrch)),
                playlistItemOrchestrator = get(named(PlaylistItemOrch)),
                coroutines = get(),
                playlistMutator = get(),
                log = get(),
                prefsWrapper = get(),
                mediaUpdate = get(),
                playlistOrDefaultUsecase = get(),
                recentLocalPlaylists = get(),
                playlistAndItemMapper = get()
            )
        }
        single { get<QueueMediatorContract.Producer>() as QueueMediatorContract.Consumer }
    }

    private val dbModule = module {
        single<DatabaseInitializer> { JsonDatabaseInitializer(get(), get(), get(), get(), get(), get(), get()) }
    }

    private val orchestratorModule = module {
        single { PlaylistOrchestrator(get(), get()) }
        single<OrchestratorContract<PlaylistDomain>>(named(PlaylistOrch)) { get<PlaylistOrchestrator>() }
        single { PlaylistItemOrchestrator(get(), get()) }
        single<OrchestratorContract<PlaylistItemDomain>>(named(PlaylistItemOrch)) { get<PlaylistItemOrchestrator>() }
        single { MediaOrchestrator(get(), get()) }
        single<OrchestratorContract<MediaDomain>>(named(MediaOrch)) { get<MediaOrchestrator>() }
        single { ChannelOrchestrator(get(), get()) }
        single<OrchestratorContract<ChannelDomain>>(named(ChannelOrch)) { get<ChannelOrchestrator>() }
        single { PlaylistStatsOrchestrator(get()) }
        single<OrchestratorContract<PlaylistStatDomain>>(named(PlaylistStatsOrch)) { get<PlaylistStatsOrchestrator>() }

        single { NewMediaPlayistInteractor(get(), get(), get(), get(named(NewItems))) }
        single { RecentItemsPlayistInteractor(get(), get()) }
        single { StarredItemsPlayistInteractor(get(), get(), get(), get(named(Starred))) }
        single { UnfinishedItemsPlayistInteractor(get(), get(), get(), get(named(Unfinished))) }
        single { LocalSearchPlayistInteractor(get(), get(), get()) }
        single { LiveUpcomingItemsPlayistInteractor(get(), get(), get()) }
        single { YoutubeSearchPlayistInteractor(get(), get(), get(), YoutubeSearchPlayistInteractor.State(), get()) }
        factory {
            mapOf(
                NewItems.identifier() to get<NewMediaPlayistInteractor>(),
                Recent.identifier() to get<RecentItemsPlayistInteractor>(),
                LocalSearch.identifier() to get<LocalSearchPlayistInteractor>(),
                YoutubeSearch.identifier() to get<YoutubeSearchPlayistInteractor>(),
                Starred.identifier() to get<StarredItemsPlayistInteractor>(),
                Unfinished.identifier() to get<UnfinishedItemsPlayistInteractor>(),
                LiveUpcoming.identifier() to get<LiveUpcomingItemsPlayistInteractor>(),
            )
        }
    }

    private val usecaseModule = module {
        factory {
            PlaylistUpdateUsecase(
                playlistOrchestrator = get(named(PlaylistOrch)),
                playlistItemOrchestrator = get(named(PlaylistItemOrch)),
                mediaOrchestrator = get(named(MediaOrch)),
                playlistMediaLookupUsecase = get(),
                timeProvider = get(),
                log = get(),
                updateChecker = get(),
                updateServiceManager = get()
            )
        }
        factory<PlaylistUpdateUsecase.UpdateCheck> { PlaylistUpdateUsecase.PlatformUpdateCheck() }
        factory { PlaylistMergeUsecase(get(), get()) }
        factory { PlaylistMediaLookupUsecase(get(), get(), get()) }
        factory { AddLinkUsecase(get(), get(), get(), get(), get()) }
        factory { PlaylistMediaUpdateUsecase(get()) }
        factory { PlaylistOrDefaultUsecase(get(), get(), get()) }
        factory { AddPlaylistUsecase(get(), get(), get(), get()) }
        factory { AddBrowsePlaylistUsecase(get(), get(), get(), get()) }
        factory { MediaUpdateFromPlatformUseCase(get(), get()) }
    }

    private val objectModule = module {
        factory { ParserFactory() }
        single {
            PlaylistMemoryRepository(
                coroutines = get(),
                newItemsInteractor = get(),
                recentItemsInteractor = get(),
                localSearchInteractor = get(),
                starredItemsInteractor = get(),
                remoteSearchOrchestrator = get(),
                unfinishedItemsInteractor = get(),
                liveUpcomingItemsPlayistInteractor = get()
            )
        }
        single { get<PlaylistMemoryRepository>().playlistItemMemoryRepository }
        single { get<PlaylistMemoryRepository>().mediaMemoryRepository }
        single<MultiPlatformPreferencesWrapper> { MultiPlatformPreferencesWrapperImpl() }
        factory { BrowseRecentCategories(get(), get()) }
        factory { RecentLocalPlaylists(get(), get()) }
        factory { PlatformFileOperation() }
        factory { LinkExtractor() }
        factory { TimecodeExtractor() }
        factory { BackupCheck(get(), get(), get(), get()) }
        factory { IdGenerator(get()) }
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
        factory { WifiStartChecker(get(), get()) }
        factory<RemoteServerContract.AvailableMessageHandler> {
            AvailableMessageHandler(
                remoteRepo = get(),
                availableMessageMapper = get(),
                remoteInteractor = get(),
                localRepo = get(),
                log = get()
            )
        }
        factory<UpcomingContract.Presenter> {
            UpcomingPresenter(
                view = get(),
                playlistItemOrchestrator = get(named(PlaylistItemOrch)),
                mediaOrchestrator = get(named(MediaOrch)),
                coroutines = get(),
                timeProvider = get(),
                log = get()
            )
        }
    }

    private val uiModule = module {
        factory { IconMapper() }
        factory { DurationTextColorMapper() }
    }

    val modules = listOf(objectModule)
        .plus(orchestratorModule)
        .plus(queueModule)
        .plus(dbModule)
        .plus(usecaseModule)
        .plus(uiModule)
        .plus(DescriptionContract.viewModule)
}
