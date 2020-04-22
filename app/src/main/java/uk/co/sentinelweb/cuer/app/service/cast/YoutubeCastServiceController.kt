package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import java.lang.IllegalStateException

class YoutubeCastServiceController constructor(
    private val service: YoutubeCastService,
    private val creator: YoutubePlayerContextCreator,
    private val chromeCastWrapper: ChromeCastWrapper,
    private val state: YoutubeCastServiceState
) {

    fun initialise() {
//        wrapper = creator.createContext(chromeCastWrapper.getCastContext()).apply {
//            //playerUi = control
//        }

    }

    fun destroy() {
//        wrapper = null
        state.youtubePlayerContext?.destroy()
        state.youtubePlayerContext = null
    }

    fun pause() {
        // doesnt work - may have to retain the one ChromecastYouTubePlayerContextWrapper
        //control.listeners.get(0).pause()
    }

    fun pullYoutubeContext(): ChromecastYouTubePlayerContextWrapper? {
        val wrapper = state.youtubePlayerContext
        state.youtubePlayerContext = null
        state.youtubePlayerContext?.playerUi = null
        return wrapper
    }

    fun pushYoutubeContext(youtubePlayerContext: ChromecastYouTubePlayerContextWrapper) {
        if (state.youtubePlayerContext != null) throw IllegalStateException("wrapper is already connected to service")
        state.youtubePlayerContext = youtubePlayerContext
        state.youtubePlayerContext?.playerUi = service.getPlayerControls()
    }
}