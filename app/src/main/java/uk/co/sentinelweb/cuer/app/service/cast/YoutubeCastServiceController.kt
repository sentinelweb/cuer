package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator

class YoutubeCastServiceController constructor(
    private val service: YoutubeCastService,
    private val creator: YoutubePlayerContextCreator,
    private val chromeCastWrapper: ChromeCastWrapper,
    private val state: YoutubeCastServiceState
) {

    fun initialise() {

    }

    fun destroy() {
//        wrapper = null
//        state.youtubePlayerContext?.destroy()
//        state.youtubePlayerContext = null
    }

    fun pause() {
    }

}