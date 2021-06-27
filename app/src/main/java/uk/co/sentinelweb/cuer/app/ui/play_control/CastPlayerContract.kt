package uk.co.sentinelweb.cuer.app.ui.play_control

import androidx.annotation.ColorRes
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.FragmentNavigator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipModelMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipView
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
        fun onResume()
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
        fun setPlaylistItem(playlistItem: PlaylistItemDomain?, source: OrchestratorContract.Source)

        interface Listener {
            fun play()
            fun pause()
            fun trackBack()
            fun trackFwd()
            fun seekTo(positionMs: Long)
            fun getLiveOffsetMs(): Long
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
        fun setState(state: PlayerStateDomain?)
    }

    enum class ConnectionState {
        CC_DISCONNECTED, CC_CONNECTING, CC_CONNECTED,
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
        var source: OrchestratorContract.Source = OrchestratorContract.Source.LOCAL
    ) : ViewModel() {

    }

    companion object {
        @JvmStatic
        val viewModule = module {
            scope(named<CastPlayerFragment>()) {
                scoped<View> { getSource() }
                scoped<Presenter> {
                    CastPlayerPresenter(
                        view = get(),
                        mapper = get(),
                        state = get(),
                        log = get(),
                        skipControl = get(),
                        res = get()
                    )
                }
                scoped<SkipContract.External> {
                    SkipPresenter(
                        view = get(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = SkipModelMapper(timeSinceFormatter = get(), res = get()),
                        prefsWrapper = get()
                    )
                }
                scoped<SkipContract.View> {
                    SkipView(
                        selectDialogCreator = SelectDialogCreator(
                            context = getSource<CastPlayerFragment>().requireContext()
                        )
                    )
                }
                scoped { CastPlayerUiMapper(get(), get()) }
                viewModel { State() }
            }
        }
    }

}