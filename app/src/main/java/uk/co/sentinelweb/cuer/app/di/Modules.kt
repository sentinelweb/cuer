package uk.co.sentinelweb.cuer.app.di

import com.roche.mdas.util.wrapper.ToastWrapper
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
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.ItemListModule
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.ItemListView
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.player.PlayerFragment
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.ui.share.LinkScanner
import uk.co.sentinelweb.cuer.app.ui.share.ShareActivity
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerFragment
import uk.co.sentinelweb.cuer.app.util.provider.CoroutineContextProvider
import uk.co.sentinelweb.cuer.app.util.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.StethoWrapper
import uk.co.sentinelweb.cuer.net.NetModule

object Modules {
    private val scopedModules = listOf(
        PlaylistFragment.fragmentModule,
        PlayerFragment.fragmentModule,
        BrowseFragment.fragmentModule,
        MainActivity.activityModule,
        CastPlayerFragment.viewModule,
        ItemListModule.listModule,
        ShareActivity.activityModule,
        ItemListView.viewModule,
        YoutubeCastServiceModule.serviceModule
    )

    private val utilModule = module {
        factory { CoroutineContextProvider() }
        factory { LinkScanner() }
        single { CuerAppState() }
        single<QueueMediatorContract.Mediator> { QueueMediator(
            state = QueueMediatorState(),
            repository = get(),
            mediaMapper = MediaToPlaylistItemMapper(),
            contextProvider = get()
        ) }
    }

    private val wrapperModule = module {
        factory { ChromeCastWrapper(androidApplication()) }
        factory { YoutubePlayerContextCreator(get()) }
        factory { ToastWrapper(androidApplication()) }
        factory { StethoWrapper(androidApplication()) }
        factory { NotificationWrapper(androidApplication()) }
        factory { LogWrapper() }
    }

    private val appNetModule = module {
        factory { CuerYoutubeApiKeyProvider() }
    }

    val allModules = listOf(utilModule)
        .plus(wrapperModule)
        .plus(scopedModules)
        .plus(appNetModule)
        .plus(DatabaseModule.dbModule)
        .plus(NetModule.netModule)
}
