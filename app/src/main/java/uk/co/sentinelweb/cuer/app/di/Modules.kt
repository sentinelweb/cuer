package uk.co.sentinelweb.cuer.app.di

import com.roche.mdas.util.wrapper.ToastWrapper
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.DatabaseModule
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseFragment
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.ItemListModule
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.player.PlayerFragment
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.ui.share.LinkScanner
import uk.co.sentinelweb.cuer.app.ui.share.ShareActivity
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerFragment
import uk.co.sentinelweb.cuer.app.util.provider.CoroutineContextProvider
import uk.co.sentinelweb.cuer.app.util.wrapper.StethoWrapper
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.ItemListView

object Modules {
    private val scopedModules = listOf(
        PlaylistFragment.fragmentModule,
        PlayerFragment.fragmentModule,
        BrowseFragment.fragmentModule,
        MainActivity.activityModule,
        CastPlayerFragment.viewModule,
        ItemListModule.listModule,
        ShareActivity.activityModule,
        ItemListView.viewModule
    )

    private val utilModule = module {
        factory { CoroutineContextProvider() }
        factory { StethoWrapper(androidApplication()) }
        factory { LinkScanner() }
    }

    private val wrapperModule = module {
        single { ChromeCastWrapper(androidApplication()) }
        single { YoutubePlayerContextCreator() }
        single { ToastWrapper(androidApplication()) }
    }

    val allModules = listOf(utilModule)
        .plus(wrapperModule)
        .plus(scopedModules)
        .plus(DatabaseModule.dbModule)
}
