package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

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
        fun onDestroyView()
        fun initialise()
    }

    // todo think about this maybe sub with android MediaControl interface
    interface PlayerControls {
        fun initMediaRouteButton()
        fun setConnectionState(connState: ConnectionState)
        fun setPlayerState(playState: PlayerStateDomain)
        fun addListener(l: Listener)
        fun removeListener(l: Listener)
        fun setCurrentSecond(second: Float) // todo ms long
        fun setDuration(duration: Float) // todo ms long
        fun error(msg: String)
        fun setTitle(title: String)
        fun reset()
        fun restoreState()
        fun setMedia(media: MediaDomain)
        fun setPlaylistName(name: String)
        fun setPlaylistImage(image: ImageDomain?)

        interface Listener {
            fun play()
            fun pause()
            fun trackBack()
            fun trackFwd()
            fun seekTo(positionMs: Long)
        }
    }

    interface View {
        val playerControls: PlayerControls
        fun initMediaRouteButton()
        fun setConnectionText(text: String)
        fun setCurrentSecond(second: String)
        fun setDuration(duration: String)
        fun setPlaying()
        fun setPaused()
        fun setBuffering()
        fun showMessage(msg: String)
        fun setTitle(title: String)
        fun updateSeekPosition(ratio: Float)
        fun setImage(url: String)
        fun clearImage()
        fun setPlaylistName(name: String)
        fun setPlaylistImage(url: String?)
    }

    enum class ConnectionState {
        CC_DISCONNECTED, CC_CONNECTING, CC_CONNECTED,
    }

}