package uk.co.sentinelweb.cuer.app.util.cast

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract
import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackController
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences

object CastModule {
    const val CAST = "CAST"
    val castModule = module {
        single { ChromecastYouTubePlayerContextHolder(get(), get()) }
        single { CuerCastSessionListener(get(), get()) }
        factory { ChromeCastWrapper(androidApplication()) }
        factory { CuerSimpleVolumeController(get()) }
        factory {
            YoutubePlayerContextCreator(
                queue = get(),
                log = get(),
                mediaSessionManager = get(),
                castWrapper = get(),
                timeProvider = get(),
                livePlayback = get(named(CAST)),
                coroutines = get()
            )
        }
        factory<LivePlaybackContract.Controller>(named(CAST)) {
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
        factory { CastDialogWrapper(get()) }
    }
}