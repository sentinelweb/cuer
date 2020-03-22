package uk.co.sentinelweb.cuer.app.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseFragment
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.player.PlayerFragment
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper

object Modules {
    private val scopedModules = listOf(
        PlaylistFragment.fragmentModule,
        PlayerFragment.fragmentModule,
        BrowseFragment.fragmentModule,
        MainActivity.activityModule
    )

    private val utilModule = module {

    }

    private val wrapperModule = module {
        single { ChromeCastWrapper(androidApplication()) }
    }

    val allModules = listOf(utilModule)
        .plus(wrapperModule)
        .plus(scopedModules)
}
