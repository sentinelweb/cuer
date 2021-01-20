package uk.co.sentinelweb.cuer.app.ui.play_control

import androidx.annotation.ColorRes
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.FragmentNavigator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface CastPlayerContract {

    interface Presenter {
        fun onSeekBackPressed()
        fun onSeekFwdPressed()
        fun onTrackBackPressed()
        fun onTrackFwdPressed()
        fun onSeekChanged(ratio: Float)
        fun onSeekFinished()
        fun onDestroyView()
        fun initialise()
        fun onPlayPausePressed()
        fun onPlaylistClick()
        fun onPlaylistItemClick()
        fun onSeekBackSelectTimePressed(): Boolean
        fun onSeekSelectTimeFwdPressed(): Boolean
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

        //fun setMedia(media: MediaDomain)// todo remove - use playlistitem
        fun setPlaylistName(name: String)
        fun setPlaylistImage(image: ImageDomain?)
        fun setPlaylistItem(playlistItem: PlaylistItemDomain?)

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
        fun setCurrentSecond(second: String)
        fun setDuration(duration: String)
        fun setPlaying()
        fun setPaused()
        fun showBuffering()
        fun hideBuffering()
        fun showMessage(msg: String)
        fun setTitle(title: String)
        fun updateSeekPosition(ratio: Float)
        fun setImage(url: String)
        fun clearImage()
        fun setPlaylistName(name: String)
        fun setPlaylistImage(url: String?)
        fun setSkipFwdText(text: String)
        fun setSkipBackText(text: String)
        fun navigate(navModel: NavigationModel)
        fun makeItemTransitionExtras(): FragmentNavigator.Extras
        fun setDurationColors(@ColorRes text: Int, @ColorRes upcomingBackground: Int)
        fun setSeekEnabled(enabled: Boolean)
    }

    enum class ConnectionState {
        CC_DISCONNECTED, CC_CONNECTING, CC_CONNECTED,
    }

    data class State(
        val listeners: MutableList<PlayerControls.Listener> = mutableListOf(), // todo data only here move to presenter?
        var playState: PlayerStateDomain = PlayerStateDomain.UNKNOWN,
        var positionMs: Long = 0,
        var seekPositionMs: Long = 0,
        var durationMs: Long = 0,
        var title: String = "",
        var isDestroyed: Boolean = false,
        var playlistItem: PlaylistItemDomain? = null,
        var isLiveStream: Boolean = false,
        var isUpcoming: Boolean = false
    ) : ViewModel() {

    }


}