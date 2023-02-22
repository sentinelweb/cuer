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
import uk.co.sentinelweb.cuer.app.usecase.*
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
        single { PlaylistItemOrchestrator(get(), get()) }
        single { MediaOrchestrator(get(), get()) }
        single { ChannelOrchestrator(get(), get()) }
        single { PlaylistStatsOrchestrator(get()) }
        single { NewMediaPlayistInteractor(get(), get(), get(), get(named(NewItems))) }
        single { RecentItemsPlayistInteractor(get(), get()) }
        single { StarredItemsPlayistInteractor(get(), get(), get(), get(named(Starred))) }
        single { UnfinishedItemsPlayistInteractor(get(), get(), get(), get(named(Unfinished))) }
        single { LocalSearchPlayistInteractor(get(), get(), get()) }
        single { YoutubeSearchPlayistInteractor(get(), get(), get(), YoutubeSearchPlayistInteractor.State()) }
        factory {
            mapOf(
                NewItems.identifier() to get<NewMediaPlayistInteractor>(),
                Recent.identifier() to get<RecentItemsPlayistInteractor>(),
                LocalSearch.identifier() to get<LocalSearchPlayistInteractor>(),
                YoutubeSearch.identifier() to get<YoutubeSearchPlayistInteractor>(),
                Starred.identifier() to get<StarredItemsPlayistInteractor>(),
                Unfinished.identifier() to get<UnfinishedItemsPlayistInteractor>(),
            )
        }
    }

    private val usecaseModule = module {
        factory { PlaylistUpdateUsecase(get(), get(), get(), get(), get(), get(), get()) }
        factory<PlaylistUpdateUsecase.UpdateCheck> { PlaylistUpdateUsecase.PlatformUpdateCheck() }
        factory { PlaylistMergeUsecase(get(), get()) }
        factory { PlaylistMediaLookupUsecase(get(), get(), get()) }
        factory { AddLinkUsecase(get(), get(), get(), get(), get()) }
        factory { PlaylistMediaUpdateUsecase(get()) }
        factory { PlaylistOrDefaultUsecase(get(), get()) }
        factory { AddPlaylistUsecase(get(), get(), get(), get()) }
        factory { AddBrowsePlaylistUsecase(get(), get(), get(), get()) }
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
        factory<RemoteServerContract.AvailableMessageHandler> { AvailableMessageHandler(get(), get(), get(), get(), get()) }
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
