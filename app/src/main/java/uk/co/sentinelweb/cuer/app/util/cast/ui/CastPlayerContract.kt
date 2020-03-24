package uk.co.sentinelweb.cuer.app.util.cast.ui

interface CastPlayerContract {

    interface Presenter {
        fun playPressed()
        fun pausePressed()
        fun seekBackPressed()
        fun seekFwdPressed()
        fun trackBackPressed()
        fun trackFwdPressed()
        fun onSeekChanged(ratio: Float)
    }

    interface PresenterExternal {
        fun initMediaRouteButton()
        fun setConnectionState(s: ConnectionState)
    }

    interface View {
        val presenterExternal: PresenterExternal
        fun initMediaRouteButton()
        fun setConnectionText(text: String)
    }

    enum class ConnectionState {
        CC_DISCONNECTED, CC_CONNECTING, CC_CONNECTED,

    }
}