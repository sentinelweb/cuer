package uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait

import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
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
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ItemLoader
import uk.co.sentinelweb.cuer.app.ui.ytplayer.LocalPlayerCastListener
import uk.co.sentinelweb.cuer.app.ui.ytplayer.PlayerModule

interface AytPortraitContract {
    companion object {

        @JvmStatic
        val activityModule = module {
            scope(named<AytPortraitActivity>()) {
                scoped {
                    PlayerController(
                        itemLoader = get(),
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory,
                        queueConsumer = get(),
                        queueProducer = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = getSource<AytPortraitActivity>().lifecycle.asMviLifecycle(),
                        skip = get(),
                        log = get(),
                        livePlaybackController = get(named(PlayerModule.LOCAL_PLAYER)),
                        mediaSessionManager = get()
                    )
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
            }
        }
    }
}