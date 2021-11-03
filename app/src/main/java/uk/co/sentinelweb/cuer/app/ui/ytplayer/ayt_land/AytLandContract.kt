package uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_land

import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipModelMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipView
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.player.PlayerListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ItemLoader
import uk.co.sentinelweb.cuer.app.ui.ytplayer.LocalPlayerCastListener
import uk.co.sentinelweb.cuer.app.ui.ytplayer.PlayerModule
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ShowHideUi

interface AytLandContract {
    companion object {

        @ExperimentalCoroutinesApi
        @JvmStatic
        val activityModule = module {
            scope(named<AytLandActivity>()) {
                scoped {
                    PlayerController(
                        queueConsumer = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = getSource<AytLandActivity>().lifecycle.asMviLifecycle(),
                        log = get(),
                        playControls = PlayerListener(get(), get()),
                        store = get()
                    )
                }
                scoped {
                    PlayerStoreFactory(
                        // storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory,
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
                scoped { ShowHideUi(getSource()) }
                scoped<PlayerContract.PlaylistItemLoader> { ItemLoader(getSource(), get()) }
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
                            context = getSource<AytLandActivity>()
                        )
                    )
                }
                scoped { LocalPlayerCastListener(getSource(), get()) }
            }
        }
    }
}