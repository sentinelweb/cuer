package uk.co.sentinelweb.cuer.app.ui.ytplayer

import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipModelMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipView
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController

interface YoutubeFullScreenContract {

    companion object {

        @JvmStatic
        val activityModule = module {
            scope(named<YoutubeFullScreenActivity>()) {
                scoped {
                    PlayerController(
                        itemLoader = get(),
                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        queueConsumer = get(),
                        queueProducer = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = null,
                        skip = get(),
                        log = get(),
                        livePlaybackController = get(named(PlayerModule.LOCAL_PLAYER))
                    )
                }
                scoped<PlayerContract.PlaylistItemLoader> { ItemLoader(getSource(), get()) }
                scoped { ShowHideUi(getSource()) }
//                scoped { PlayerModelMapper(get(), get()) }
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
                            context = getSource<YoutubeFullScreenActivity>()
                        )
                    )
                }
            }
        }
    }
}