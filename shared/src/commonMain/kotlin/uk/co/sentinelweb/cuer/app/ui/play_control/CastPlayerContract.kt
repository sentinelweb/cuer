package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.Disconnected
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ControlTarget.Local
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface CastPlayerContract {

    interface Presenter {
        fun setCastController(castController: CastController)
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
        fun onResume()
        fun onSupport()
        fun onCastClick()
    }

    enum class DurationStyle {
        Normal, Upcoming, Live
    }

    interface View {
        val playerControls: PlayerContract.PlayerControls

        //fun initMediaRouteButton()
        fun setPosition(second: String)
        fun setLiveTime(second: String?)
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

        // fun makeItemTransitionExtras(): androidx.navigation.fragment.FragmentNavigator.Extras
        //fun setDurationColors(@androidx.annotation.ColorRes text: Int, @androidx.annotation.ColorRes upcomingBackground: Int)
        fun setDurationStyle(style: DurationStyle)
        fun setSeekEnabled(enabled: Boolean)
        fun setState(state: PlayerStateDomain?)
        fun showSupport(media: MediaDomain)
        fun setNextTrackEnabled(nextTrackEnabled: Boolean)
        fun setPrevTrackEnabled(prevTrackEnabled: Boolean)
        fun setTargetDetails(details: State.TargetDetails)
    }

    data class State(
        var playState: PlayerStateDomain = PlayerStateDomain.UNKNOWN,
        var positionMs: Long = 0,
        var seekPositionMs: Long = 0,
        var durationMs: Long = 0,
        var title: String = "",
        var isDestroyed: Boolean = false,
        var playlistItem: PlaylistItemDomain? = null,
        var isLiveStream: Boolean = false,
        var isUpcoming: Boolean = false,
        var playlistName: String? = null,
        var buttons: PlayerContract.View.Model.Buttons? = null,
        var targetDetails: TargetDetails = TargetDetails(),
        var volumeFraction: Float = 0f,
    ) {
        data class TargetDetails(
            val target: PlayerContract.ControlTarget = Local,
            val connectionState: CastConnectionState = Disconnected,
            val name: String? = null
        )
    }
}
