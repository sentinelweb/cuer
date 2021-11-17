package uk.co.sentinelweb.cuer.app.ui.ytplayer

import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.player.PlayerListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerModelMapper
import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract
import uk.co.sentinelweb.cuer.app.util.ayt_player.live.LivePlaybackController
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.*

object PlayerModule {

    const val LOCAL_PLAYER = "LOCAL_PLAYER"

    val localPlayerModule = module {
        factory<LivePlaybackContract.Controller>(named(LOCAL_PLAYER)) {
            LivePlaybackController(
                state = LivePlaybackContract.State(),
                prefKeys = object : LivePlaybackContract.PrefKeys {
                    override val durationValue = LOCAL_DURATION
                    override val durationObtainedTime = LOCAL_DURATION_TIME
                    override val durationVideoId = LOCAL_DURATION_ID
                },
                prefs = get(),
                timeProvider = get(),
                log = get()
            )
        }
        factory { PlayerModelMapper(get(), get(), get(), get()) }
        single { PlayerListener(get(), get()) }
    }
}