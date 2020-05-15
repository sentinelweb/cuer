package uk.co.sentinelweb.cuer.app.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.db.DatabaseModule
import uk.co.sentinelweb.cuer.app.net.CuerYoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.app.queue.MediaToPlaylistItemMapper
import uk.co.sentinelweb.cuer.app.queue.QueueMediator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorState
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceModule
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseFragment
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerFragment
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditFragment
import uk.co.sentinelweb.cuer.app.ui.share.LinkScanner
import uk.co.sentinelweb.cuer.app.ui.share.ShareActivity
import uk.co.sentinelweb.cuer.app.util.cast.CastModule
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.helper.PlaylistMutator
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaMetadataMapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.mediasession.PlaybackStateMapper
import uk.co.sentinelweb.cuer.app.util.wrapper.*
import uk.co.sentinelweb.cuer.core.di.CoreModule
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.net.NetModule
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider

object Modules {
    private val scopedModules = listOf(
        PlaylistFragment.fragmentModule,
        PlayerFragment.fragmentModule,
        BrowseFragment.fragmentModule,
        MainActivity.activityModule,
        CastPlayerFragment.viewModule,
        ShareActivity.activityModule,
        PlaylistItemEditFragment.fragmentModule,
        YoutubeCastServiceModule.serviceModule
    )

    private val utilModule = module {
        factory { LinkScanner() }
        single { CuerAppState() }
        single<QueueMediatorContract.Mediator> {
            QueueMediator(
                state = QueueMediatorState(),
                repository = get(),
                mediaMapper = MediaToPlaylistItemMapper(),
                contextProvider = get(),
                mediaSessionManager = get(),
                playlistMutator = get()
            )
        }
        single { ChromecastYouTubePlayerContextHolder(get(), get()) }
        factory { MediaSessionManager(get(), androidApplication(), get(), get(), get()) }
        factory { MediaMetadataMapper() }
        factory { PlaybackStateMapper() }
        factory { PlaylistMutator() }
    }

    private val wrapperModule = module {
        factory { ToastWrapper(androidApplication()) }
        factory { PhoenixWrapper(androidApplication()) }
        factory { StethoWrapper(androidApplication()) }
        factory { NotificationWrapper(androidApplication()) }
        factory<LogWrapper> { AndroidLogWrapper() }
    }

    private val appNetModule = module {
        factory<YoutubeApiKeyProvider> { CuerYoutubeApiKeyProvider() }
    }

    val allModules = listOf(utilModule)
        .plus(wrapperModule)
        .plus(scopedModules)
        .plus(appNetModule)
        .plus(DatabaseModule.dbModule)
        .plus(NetModule.netModule)
        .plus(CoreModule.objectModule)
        .plus(CastModule.castModule)
}
