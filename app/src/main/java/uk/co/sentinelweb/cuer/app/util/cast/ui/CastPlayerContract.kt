package uk.co.sentinelweb.cuer.app.util.cast.ui

interface CastPlayerContract {

    interface Presenter {
        fun onPlayPressed()
        fun onPausePressed()
        fun onSeekBackPressed()
        fun onSeekFwdPressed()
        fun onTrackBackPressed()
        fun onTrackFwdPressed()
        fun onSeekChanged(ratio: Float)
        fun onSeekFinished()
    }

    interface PresenterExternal {

        fun initMediaRouteButton()
        fun setConnectionState(connState: ConnectionState)
        fun setPlayerState(playState: PlayerStateUi)
        fun addListener(l: Listener)
        fun removeListener(l: Listener)
        fun setCurrentSecond(second: Float)// todo ms long
        fun setDuration(duration: Float)// todo ms long
        fun error(msg:String)
        fun setTitle(title:String)
        fun reset()

        interface Listener {
            fun playPressed()
            fun pausePressed()
            fun trackBackPressed()
            fun trackFwdPressed()
            fun onSeekChanged(positionMs: Long)
        }
    }

    interface View {
        val presenterExternal: PresenterExternal
        fun initMediaRouteButton()
        fun setConnectionText(text: String)
        fun setCurrentSecond(second: String)
        fun setDuration(duration: String)
        fun setPlaying()
        fun setPaused()
        fun setBuffering()
        fun showMessage(msg:String)
        fun setTitle(title:String)
        fun updateSeekPosition(ratio: Float)
    }

    enum class ConnectionState {
        CC_DISCONNECTED, CC_CONNECTING, CC_CONNECTED,
    }

    enum class PlayerStateUi {
        UNKNOWN, UNSTARTED, ENDED, PLAYING, PAUSED, BUFFERING, VIDEO_CUED
    }
}