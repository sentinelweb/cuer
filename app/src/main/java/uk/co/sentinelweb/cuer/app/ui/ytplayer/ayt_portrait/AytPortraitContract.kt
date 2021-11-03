package uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait

import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.inteface.EmptyCommitHost
import uk.co.sentinelweb.cuer.app.ui.common.navigation.EmptyNavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipModelMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipView
import uk.co.sentinelweb.cuer.app.ui.play_control.mvi.CastPlayerMviFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.player.PlayerListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ItemLoader
import uk.co.sentinelweb.cuer.app.ui.ytplayer.LocalPlayerCastListener
import uk.co.sentinelweb.cuer.app.ui.ytplayer.PlayerModule

interface AytPortraitContract {
    companion object {

        @ExperimentalCoroutinesApi
        @JvmStatic
        val activityModule = module {
            scope(named<AytPortraitActivity>()) {
                scoped {
                    PlayerController(
                        queueConsumer = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = getSource<AytPortraitActivity>().lifecycle.asMviLifecycle(),
                        log = get(),
                        playControls = PlayerListener(get(), get()),
                        store = get()
                    )
                }
                scoped {
                    PlayerStoreFactory(
                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        //storeFactory = DefaultStoreFactory,
                        itemLoader = get(),
                        queueConsumer = get(),
                        queueProducer = get(),
                        skip = get(),
                        coroutines = get(),
                        log = get(),
                        livePlaybackController = get(named(PlayerModule.LOCAL_PLAYER)),
                        mediaSessionManager = get(),
                        playerControls = get(),
                    ).create()
                }
                scoped<PlayerContract.PlaylistItemLoader> { ItemLoader(getSource(), get()) }
                scoped {
                    (getSource<AytPortraitActivity>()
                        .supportFragmentManager
                        .findFragmentById(R.id.portrait_player_controls) as CastPlayerMviFragment)
                }
                scoped {
                    (getSource<AytPortraitActivity>()
                        .supportFragmentManager
                        .findFragmentById(R.id.portrait_player_playlist) as PlaylistFragment)
                }
                scoped { navigationMapper(false, getSource(), withNavHost = false) }
                scoped<SkipContract.External> {
                    SkipPresenter(
                        view = get(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = SkipModelMapper(timeSinceFormatter = get(), res = get()),
                        prefsWrapper = get()
                    )
                }
                scoped<SkipContract.View> {
                    SkipView(
                        selectDialogCreator = SelectDialogCreator(
                            context = getSource<AytPortraitActivity>()
                        )
                    )
                }
                scoped { LocalPlayerCastListener(getSource(), get()) }
                scoped<NavigationProvider> { EmptyNavigationProvider() }
                scoped<CommitHost> { EmptyCommitHost() }
            }
        }
    }
}