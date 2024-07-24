package uk.co.sentinelweb.cuer.app.util.chromecast

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract
import uk.co.sentinelweb.cuer.app.util.ayt_player.live.LivePlaybackController
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences

object CastModule {
    const val CAST_PLAYER = "CAST_PLAYER"
    val castModule = module {
        single { ChromecastYouTubePlayerContextHolder(get(), get()) }
        single<ChromecastContract.PlayerContextHolder> { get<ChromecastYouTubePlayerContextHolder>() }
        single { ChromeCastSessionListener(get(), get()) }
        factory { ChromeCastWrapper(androidApplication()) }
        factory<ChromecastContract.Wrapper> { get<ChromeCastWrapper>() }
        factory {
            YoutubePlayerContextCreator(
                queue = get(),
                log = get(),
                mediaSessionManager = get(),
                castWrapper = get(),
                timeProvider = get(),
                livePlayback = get(named(CAST_PLAYER)),
                coroutines = get()
            )
        }
        factory<LivePlaybackContract.Controller>(named(CAST_PLAYER)) {
            LivePlaybackController(
                state = LivePlaybackContract.State(),
                prefKeys = object : LivePlaybackContract.PrefKeys {
                    override val durationValue = GeneralPreferences.LIVE_DURATION
                    override val durationObtainedTime = GeneralPreferences.LIVE_DURATION_TIME
                    override val durationVideoId = GeneralPreferences.LIVE_DURATION_ID
                },
                prefs = get(),
                timeProvider = get(),
                log = get()
            )
        }
    }
}