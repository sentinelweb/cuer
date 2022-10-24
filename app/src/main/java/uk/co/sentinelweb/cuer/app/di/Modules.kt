package uk.co.sentinelweb.cuer.app.di

import com.roche.mdas.util.wrapper.SoftKeyboardWrapper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.backup.AutoBackupFileExporter
import uk.co.sentinelweb.cuer.app.backup.BackupFileManager
import uk.co.sentinelweb.cuer.app.backup.IBackupManager
import uk.co.sentinelweb.cuer.app.db.AppDatabaseModule
import uk.co.sentinelweb.cuer.app.db.repository.file.ImageFileRepository
import uk.co.sentinelweb.cuer.app.net.CuerPixabayApiKeyProvider
import uk.co.sentinelweb.cuer.app.net.CuerYoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.app.receiver.ScreenStateReceiver
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceContract
import uk.co.sentinelweb.cuer.app.service.remote.RemoteContract
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseFragment
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DatePickerCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.appselect.AppSelectorBottomSheet
import uk.co.sentinelweb.cuer.app.ui.common.dialog.playlist.PlaylistSelectDialogModelCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.support.SupportDialogFragment
import uk.co.sentinelweb.cuer.app.ui.common.mapper.BackgroundMapper
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.AndroidRibbonCreator
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonCreator
import uk.co.sentinelweb.cuer.app.ui.common.views.PlayYangProgress
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionView
import uk.co.sentinelweb.cuer.app.ui.main.MainContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.ui.play_control.mvi.CastPlayerMviFragment
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditContract
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchContract
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageContract
import uk.co.sentinelweb.cuer.app.ui.settings.PrefBackupContract
import uk.co.sentinelweb.cuer.app.ui.settings.PrefPlayerContract
import uk.co.sentinelweb.cuer.app.ui.settings.PrefRootContract
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder
import uk.co.sentinelweb.cuer.app.ui.ytplayer.PlayerModule
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_land.AytLandContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait.AytPortraitContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.yt_land.YoutubeFullScreenContract
import uk.co.sentinelweb.cuer.app.util.cast.CastModule
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseModule
import uk.co.sentinelweb.cuer.app.util.image.BitmapSizer
import uk.co.sentinelweb.cuer.app.util.image.ImageProvider
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaMetadataMapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.mediasession.PlaybackStateMapper
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.share.SharingShortcutsManager
import uk.co.sentinelweb.cuer.app.util.share.scan.AndroidLinkScanner
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.app.util.share.scan.urlMediaMappers
import uk.co.sentinelweb.cuer.app.util.wrapper.*
import uk.co.sentinelweb.cuer.app.util.wrapper.log.AndroidLogWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.log.CompositeLogWrapper
import uk.co.sentinelweb.cuer.core.di.SharedCoreModule
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.db.di.AndroidDatabaseModule
import uk.co.sentinelweb.cuer.db.di.DatabaseModule
import uk.co.sentinelweb.cuer.domain.di.SharedDomainModule
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.NetModule
import uk.co.sentinelweb.cuer.net.NetModuleConfig
import uk.co.sentinelweb.cuer.net.di.SharedNetModule
import uk.co.sentinelweb.cuer.net.retrofit.ServiceType.PIXABAY
import uk.co.sentinelweb.cuer.net.retrofit.ServiceType.YOUTUBE
import uk.co.sentinelweb.cuer.remote.server.di.RemoteModule

@ExperimentalCoroutinesApi
object Modules {

    val IMAGES_REPO_NAME = "images"

    private val scopedModules = listOf(
        PlaylistContract.fragmentModule,
        PlaylistsContract.fragmentModule,
        PlaylistsDialogContract.fragmentModule,
        MainContract.activityModule,
        CastPlayerContract.viewModule,
        ShareContract.activityModule,
        ScanContract.fragmentModule,
        PlaylistItemEditContract.fragmentModule,
        PlaylistEditContract.fragmentModule,
        YoutubeCastServiceContract.serviceModule,
        PrefBackupContract.fragmentModule,
        PrefRootContract.fragmentModule,
        PrefPlayerContract.fragmentModule,
        SearchContract.fragmentModule,
        SearchImageContract.fragmentModule,
        RemoteContract.serviceModule,
        YoutubeFullScreenContract.activityModule,
        AytPortraitContract.activityModule,
        AytLandContract.activityModule,
        CastPlayerMviFragment.fragmentModule,
        DescriptionView.viewModule,
        BrowseFragment.fragmentModule,
        FloatingPlayerContract.serviceModule,
        SupportDialogFragment.fragmentModule,
        AppSelectorBottomSheet.fragmentModule,
    )

    private val uiModule = module {
        factory { PlaylistSelectDialogModelCreator(get(), get()) }
        factory { DatePickerCreator() }
        factory { IconMapper() }
        factory { BackgroundMapper(get()) }
        single { AytViewHolder(get(), get()) }
        factory { PlayYangProgress(get()) }
        factory<RibbonCreator> { AndroidRibbonCreator(get()) }
    }

    private val receiverModule = module {
        single { ScreenStateReceiver() }
    }

    private val utilModule = module {
        factory<LinkScanner> { AndroidLinkScanner(log = get(), mappers = urlMediaMappers) }
        single { CuerAppState() }

        factory<MediaSessionContract.Manager> {
            MediaSessionManager(
                get(),
                MediaSessionManager.State(),
                androidApplication(),
                get(),
                get(),
                get()
            )
        }
        factory { MediaMetadataMapper(get(), get()) }
        factory { PlaybackStateMapper() }
        factory { PlaylistMutator() }
        factory { SharingShortcutsManager() }
        factory<IBackupManager> {
            BackupFileManager(
                channelRepository = get(),
                mediaRepository = get(),
                playlistRepository = get(),
                playlistItemRepository = get(),
                imageDatabaseRepository = get(),
                contextProvider = get(),
                parserFactory = get(),
                playlistItemCreator = get(),
                timeProvider = get(),
                timeStampMapper = get(),
                imageFileRepository = get(),
                context = androidApplication(),
                log = get()
            )
        }
        factory { AutoBackupFileExporter(get(), get(), get(), get(), get()) }
        factory { ImageProvider(get(), get()) }
        single {
            ImageFileRepository(
                androidApplication().filesDir.absolutePath,
                HttpClient(CIO),
                get(),
                get(),
                get(),
                IMAGES_REPO_NAME
            )
        }
        factory { ContentUriUtil(androidApplication()) }
        factory { BitmapSizer() }
    }

    private val wrapperModule = module {
        factory { ToastWrapper(androidApplication()) }
        factory { StethoWrapper(androidApplication()) }
        factory { NotificationWrapper(androidApplication()) }
        factory { ResourceWrapper(androidApplication()) }
        factory<LogWrapper> { CompositeLogWrapper(get(), get()) }
        factory<ConnectivityWrapper> { AndroidConnectivityWrapper(androidApplication()) }
        factory { AndroidLogWrapper() }
        factory { ContentProviderFileWrapper(androidApplication()) }
        factory { SoftKeyboardWrapper() }
        single<GeneralPreferencesWrapper> {
            SharedPrefsWrapper(androidApplication(), get())
        }
        factory { ServiceWrapper(androidApplication(), get()) }
        factory { EdgeToEdgeWrapper() }
        factory { AppListBuilder(androidApplication(), get()) }
    }

    private val appNetModule = module {
        factory<ApiKeyProvider>(named(YOUTUBE)) { CuerYoutubeApiKeyProvider() }
        factory<ApiKeyProvider>(named(PIXABAY)) { CuerPixabayApiKeyProvider() }
        single { NetModuleConfig(debug = BuildConfig.DEBUG) }
    }

    val allModules = listOf(utilModule)
        .plus(uiModule)
        .plus(wrapperModule)
        .plus(scopedModules)
        .plus(appNetModule)
        .plus(receiverModule)
        .plus(DatabaseModule.modules)
        .plus(AppDatabaseModule.module)
        .plus(AndroidDatabaseModule.modules)
        .plus(NetModule.netModule)
        .plus(SharedCoreModule.objectModule)
        .plus(SharedDomainModule.objectModule)
        .plus(SharedNetModule.objectModule)
        .plus(SharedAppModule.modules)
        .plus(CastModule.castModule)
        .plus(FirebaseModule.fbModule)
        .plus(RemoteModule.objectModule)
        .plus(PlayerModule.localPlayerModule)
}
