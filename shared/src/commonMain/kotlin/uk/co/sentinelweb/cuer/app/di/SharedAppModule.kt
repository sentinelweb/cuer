package uk.co.sentinelweb.cuer.app.di

import com.russhwolf.settings.Settings
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
import uk.co.sentinelweb.cuer.app.service.remote.AppRemoteDatabaseAdapter
import uk.co.sentinelweb.cuer.app.service.remote.AvailableMessageHandler
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.service.remote.WifiStartChecker
import uk.co.sentinelweb.cuer.app.service.remote.player.PlayerMessageToIntentMapper
import uk.co.sentinelweb.cuer.app.service.remote.player.PlayerSessionListener
import uk.co.sentinelweb.cuer.app.service.remote.player.PlayerSessionManager
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseRecentCategories
import uk.co.sentinelweb.cuer.app.ui.common.mapper.DurationTextColorMapper
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipModelMapper
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesModelMapper
import uk.co.sentinelweb.cuer.app.ui.player.MediaSessionListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerEventToIntentMapper
import uk.co.sentinelweb.cuer.app.ui.player.PlayerModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.IdGenerator
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingPresenter
import uk.co.sentinelweb.cuer.app.usecase.*
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
import uk.co.sentinelweb.cuer.app.util.link.LinkExtractor
import uk.co.sentinelweb.cuer.app.util.link.TimecodeExtractor
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapperImpl
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionHolder
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionMessageMapper

object SharedAppModule {
    private val queueModule = module {
        single<QueueMediatorContract.Producer> {
            QueueMediator(
                state = QueueMediatorState(),
                playlistOrchestrator = get(named(Playlist)),
                playlistItemOrchestrator = get(named(PlaylistItem)),
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
    }

    private val orchestratorModule = module {
        single { PlaylistOrchestrator(get(), get()) }
        single<OrchestratorContract<PlaylistDomain>>(named(Playlist)) { get<PlaylistOrchestrator>() }
        single { PlaylistItemOrchestrator(get(), get()) }
        single<OrchestratorContract<PlaylistItemDomain>>(named(PlaylistItem)) { get<PlaylistItemOrchestrator>() }
        single { MediaOrchestrator(get(), get()) }
        single<OrchestratorContract<MediaDomain>>(named(Media)) { get<MediaOrchestrator>() }
        single { ChannelOrchestrator(get(), get()) }
        single<OrchestratorContract<ChannelDomain>>(named(Channel)) { get<ChannelOrchestrator>() }
        single { PlaylistStatsOrchestrator(get()) }
        single<OrchestratorContract<PlaylistStatDomain>>(named(PlaylistStats)) { get<PlaylistStatsOrchestrator>() }

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
                playlistOrchestrator = get(named(Playlist)),
                playlistItemOrchestrator = get(named(PlaylistItem)),
                mediaOrchestrator = get(named(Media)),
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
        factory { GetPlaylistsFromDeviceUseCase(get()) }
        factory { GetFolderListUseCase(get(), get(), get(), get(), get()) }
        factory { CastUseCase() }
    }

    private val remoteModule = module {
        factory { WifiStartChecker(get(), get()) }
        factory<RemoteDatabaseAdapter> {
            AppRemoteDatabaseAdapter(
                playlistOrchestrator = get(),
                playlistItemOrchestrator = get(),
                addLinkUsecase = get(),
            )
        }
        factory<RemoteServerContract.AvailableMessageHandler> {
            AvailableMessageHandler(
                remoteRepo = get(),
                availableMessageMapper = get(),
                remoteStatusInteractor = get(),
                localRepo = get(),
                wifiStateProvider = get(),
                vibrateWrapper = get(),
                log = get(),
            )
        }
    }

    private val playerModule = module {
        factory { PlayerModelMapper(get(), get(), get(), get(), get()) }
        single { MediaSessionListener(get(), get()) }
        factory<SkipContract.Mapper> { SkipModelMapper(timeSinceFormatter = get()) }
        single { PlayerSessionHolder() }
        factory { PlayerSessionManager(get(), get()) }
        single { PlayerSessionListener(get(), get(), get()) } // fixme maybe move this to scoped declaration
        single { PlayerEventToIntentMapper }
        single { PlayerSessionMessageMapper() }
        factory { PlayerMessageToIntentMapper(get()) }
        single { CuerCastPlayerWatcher(CuerCastPlayerWatcher.State(), get(), get(), get()) }
    }

    private val utilModule = module {
        factory { ParserFactory() }
        single<MultiPlatformPreferencesWrapper> {
            MultiPlatformPreferencesWrapperImpl(
                getOrNull<Settings>() ?: Settings()
            )
        }
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
                log = get(),
            )
        }
    }

    private val uiModule = module {
        factory { IconMapper() }
        factory { DurationTextColorMapper() }
        factory { BrowseRecentCategories(get(), get()) }
        factory { RecentLocalPlaylists(get(), get()) }
        factory<UpcomingContract.Presenter> {
            UpcomingPresenter(
                view = get(),
                playlistItemOrchestrator = get(named(PlaylistItem)),
                mediaOrchestrator = get(named(Media)),
                coroutines = get(),
                timeProvider = get(),
                log = get(),
            )
        }
        factory { FilesModelMapper() }
    }

    val modules = listOf(utilModule)
        .plus(orchestratorModule)
        .plus(queueModule)
        .plus(dbModule)
        .plus(usecaseModule)
        .plus(uiModule)
        .plus(remoteModule)
        .plus(playerModule)
        .plus(DescriptionContract.viewModule)
}
