package uk.co.sentinelweb.cuer.app.ui.ytplayer.portrait

import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipModelMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipView
import uk.co.sentinelweb.cuer.app.ui.play_control.mvi.CastPlayerMviFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.player.PlayerModelMapper
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ItemLoader

interface YoutubePortraitContract {
    companion object {

        @JvmStatic
        val activityModule = module {
            scope(named<YoutubePortraitActivity>()) {
                scoped {
                    PlayerController(
                        itemLoader = get(),
                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        queueConsumer = get(),
                        queueProducer = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = getSource<YoutubePortraitActivity>().lifecycle.asMviLifecycle(),
                        skip = get(),
                        log = get()
                    )
                }
                scoped<PlayerContract.PlaylistItemLoader> { ItemLoader(getSource(), get()) }
                scoped { PlayerModelMapper(get(), get(), get()) }
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
                            context = getSource<YoutubePortraitActivity>()
                        )
                    )
                }
                scoped {
                    (getSource<YoutubePortraitActivity>()
                        .supportFragmentManager
                        .findFragmentById(R.id.portrait_player_controls) as CastPlayerMviFragment)
                }
                scoped { navigationMapper(false, getSource(), withNavHost = false) }
            }
        }
    }
}