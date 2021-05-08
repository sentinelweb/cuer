package uk.co.sentinelweb.cuer.app.di

import com.roche.mdas.util.wrapper.SoftKeyboardWrapper
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.db.DatabaseModule
import uk.co.sentinelweb.cuer.app.db.backup.BackupFileManager
import uk.co.sentinelweb.cuer.app.db.backup.version.ParserFactory
import uk.co.sentinelweb.cuer.app.net.CuerYoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.MemoryRepository
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.LocalSearchPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.NewMediaPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RecentItemsPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RemoteSearchPlayistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistMediaLookupOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistMergeOrchestrator
import uk.co.sentinelweb.cuer.app.queue.QueueMediator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorState
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceModule
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DatePickerCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.playlist.PlaylistSelectDialogModelCreator
import uk.co.sentinelweb.cuer.app.ui.common.mapper.BackgroundMapper
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.main.MainContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditContract
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchContract
import uk.co.sentinelweb.cuer.app.ui.settings.PrefBackupContract
import uk.co.sentinelweb.cuer.app.ui.settings.PrefRootContract
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.cast.CastModule
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseModule
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaMetadataMapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.mediasession.PlaybackStateMapper
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.share.SharingShortcutsManager
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.app.util.share.scan.urlMediaMappers
import uk.co.sentinelweb.cuer.app.util.wrapper.*
import uk.co.sentinelweb.cuer.app.util.wrapper.log.AndroidLogWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.log.CompositeLogWrapper
import uk.co.sentinelweb.cuer.core.di.CoreModule
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.di.DomainModule
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator
import uk.co.sentinelweb.cuer.net.NetModule
import uk.co.sentinelweb.cuer.net.NetModuleConfig
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider

object Modules {

    private val scopedModules = listOf(
        PlaylistContract.fragmentModule,
        PlaylistsContract.fragmentModule,
        PlaylistsDialogContract.fragmentModule,
        PlayerContract.fragmentModule,
        BrowseContract.fragmentModule,
        MainContract.activityModule,
        CastPlayerContract.viewModule,
        ShareContract.activityModule,
        ScanContract.fragmentModule,
        PlaylistItemEditContract.fragmentModule,
        PlaylistEditContract.fragmentModule,
        YoutubeCastServiceModule.serviceModule,
        PrefBackupContract.fragmentModule,
        PrefRootContract.fragmentModule,
        SearchContract.fragmentModule
    )

    private val uiModule = module {
        factory { PlaylistSelectDialogModelCreator(get(), get()) }
        factory { DatePickerCreator() }
        factory { IconMapper() }
        factory { BackgroundMapper(get()) }
    }

    private val orchestratorModule = module {
        single { PlaylistOrchestrator(get(), get(), get(), get()) }
        single { PlaylistItemOrchestrator(get(), get(), get()) }
        single { MediaOrchestrator(get(), get()) }
        single { ChannelOrchestrator(get(), get()) }
        single { PlaylistStatsOrchestrator(get()) }
        single { PlaylistMemoryRepository(get(), get(), get(), get(), get()) }
        single<MemoryRepository<PlaylistItemDomain>> { get<PlaylistMemoryRepository>().playlistItemMemoryRepository }
        factory { PlaylistUpdateOrchestrator(get(), get(), get(), get(), get()) }
        factory { PlaylistMergeOrchestrator(get(), get()) }
        factory { PlaylistMediaLookupOrchestrator(get(), get()) }
        factory { NewMediaPlayistInteractor(get()) }
        factory { RecentItemsPlayistInteractor(get()) }
        factory { LocalSearchPlayistInteractor(get(), get(named<GeneralPreferences>())) }
        factory { RemoteSearchPlayistOrchestrator(get(named<GeneralPreferences>()), get(), get(), RemoteSearchPlayistOrchestrator.State()) }
    }

    private val utilModule = module {
        factory {
            LinkScanner(
                log = get(),
                mappers = urlMediaMappers
            )
        }
        single { CuerAppState() }

        single<QueueMediatorContract.Producer> {
            QueueMediator(
                state = QueueMediatorState(),
                playlistOrchestrator = get(),
                playlistItemOrchestrator = get(),
                coroutines = get(),
                mediaSessionManager = get(),
                playlistMutator = get(),
                prefsWrapper = get(named<GeneralPreferences>()),
                log = get()
            )
        }
        single { get<QueueMediatorContract.Producer>() as QueueMediatorContract.Consumer }
        factory { MediaSessionManager(get(), androidApplication(), get(), get(), get()) }
        factory { MediaMetadataMapper(get()) }
        factory { PlaybackStateMapper() }
        factory { PlaylistMutator() }
        factory { SharingShortcutsManager() }
        factory { BackupFileManager(get(), get(), get(), get(), get(), get(), get()) }
        factory { ParserFactory() }
    }

    private val wrapperModule = module {
        factory { ToastWrapper(androidApplication()) }
        factory { StethoWrapper(androidApplication()) }
        factory { NotificationWrapper(androidApplication()) }
        factory { ResourceWrapper(androidApplication()) }
        factory<LogWrapper> { CompositeLogWrapper(get(), get()) }
        factory<ConnectivityWrapper> { AndroidConnectivityWrapper(androidApplication()) }
        factory { AndroidLogWrapper() }
        factory { FileWrapper(androidApplication()) }
        factory { SoftKeyboardWrapper() }
        single(named<GeneralPreferences>()) {
            SharedPrefsWrapper(GeneralPreferences::class, androidApplication(), get())
        }
        factory { ServiceWrapper(androidApplication(), get()) }
        factory { EdgeToEdgeWrapper() }
    }

    private val appNetModule = module {
        factory<YoutubeApiKeyProvider> { CuerYoutubeApiKeyProvider() }
        single { NetModuleConfig(debug = BuildConfig.DEBUG) }
    }

    val allModules = listOf(utilModule)
        .plus(uiModule)
        .plus(wrapperModule)
        .plus(scopedModules)
        .plus(appNetModule)
        .plus(orchestratorModule)
        .plus(DatabaseModule.dbModule)
        .plus(NetModule.netModule)
        .plus(CoreModule.objectModule)
        .plus(DomainModule.objectModule)
        .plus(CastModule.castModule)
        .plus(FirebaseModule.fbModule)
}
