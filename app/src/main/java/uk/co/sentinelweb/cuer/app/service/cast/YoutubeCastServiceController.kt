package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import java.lang.IllegalStateException

class YoutubeCastServiceController constructor(
    private val service:YoutubeCastService,
    private val creator: YoutubePlayerContextCreator,
    private val chromeCastWrapper: ChromeCastWrapper
) {
    private var wrapper: ChromecastYouTubePlayerContextWrapper? = null

    private val control = Control()

    fun initialise() {
        wrapper = creator.createContext(chromeCastWrapper.getCastContext()).apply {
            //playerUi = control
        }

    }

    fun destroy() {
        wrapper = null
    }

    fun pause() {
        // doesnt work - may have to retain the one ChromecastYouTubePlayerContextWrapper
        control.listeners.get(0).pause()
    }

    inner class Control() :CastPlayerContract.PlayerControls {

        val listeners : MutableList<CastPlayerContract.PlayerControls.Listener> = mutableListOf()

        override fun initMediaRouteButton() {

        }

        override fun setConnectionState(connState: CastPlayerContract.ConnectionState) {

        }

        override fun setPlayerState(playState: CastPlayerContract.PlayerStateUi) {

        }

        override fun addListener(l: CastPlayerContract.PlayerControls.Listener) {
            if (listeners.size>0) throw IllegalStateException("more than 1 listener in cast player - might not be a problem")
            listeners.add(l)
        }

        override fun removeListener(l: CastPlayerContract.PlayerControls.Listener) {
            listeners.remove(l)
        }

        override fun setCurrentSecond(second: Float) {

        }

        override fun setDuration(duration: Float) {

        }

        override fun error(msg: String) {

        }

        override fun setTitle(title: String) {

        }

        override fun reset() {

        }

        override fun restoreState() {

        }

    }
}