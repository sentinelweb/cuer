package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator

class YoutubeCastServiceController constructor(
    private val service:YoutubeCastService,
    private val creator: YoutubePlayerContextCreator,
    private val chromeCastWrapper: ChromeCastWrapper
) {
    private var wrapper: ChromecastYouTubePlayerContextWrapper? = null

    fun initialise() {
        wrapper = creator.createContext(chromeCastWrapper.getCastContext())
    }

    fun destroy() {
        wrapper = null
    }
}