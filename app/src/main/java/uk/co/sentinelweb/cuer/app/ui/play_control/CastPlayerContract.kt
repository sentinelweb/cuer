package uk.co.sentinelweb.cuer.app.ui.play_control

import androidx.annotation.ColorRes
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.FragmentNavigator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.play.PlayDialog
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipModelMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipView
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
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
        fun onSupport()
    }

    interface View {
        val playerControls: PlayerContract.PlayerControls
        fun initMediaRouteButton()
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
        fun makeItemTransitionExtras(): FragmentNavigator.Extras
        fun setDurationColors(@ColorRes text: Int, @ColorRes upcomingBackground: Int)
        fun setSeekEnabled(enabled: Boolean)
        fun setState(state: PlayerStateDomain?)
        fun showSupport(media: MediaDomain)
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
        var playlistName: String? = null
    ) : ViewModel()

    companion object {
        @JvmStatic
        val viewModule = module {
            factory { CompactPlayerScroll() }
            scope(named<CastPlayerFragment>()) {
                viewModel { State() }
                scoped<View> { get<CastPlayerFragment>() }
                scoped<Presenter> {
                    CastPlayerPresenter(
                        view = get(),
                        mapper = get(),
                        state = get(),
                        log = get(),
                        skipControl = get(),
                        res = get(),
                        playUseCase = get(),
                        playlistAndItemMapper = get()
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
                        selectDialogCreator = SelectDialogCreator(context = this.getFragmentActivity())
                    )
                }
                scoped { CastPlayerUiMapper(get(), get(), get()) }

                // todo play usecase - extract
                scoped<PlatformLaunchWrapper> { YoutubeJavaApiWrapper(this.getFragmentActivity(), get()) }
                // fixme needed for play dialog - but shouldn't be needed - remove
                scoped { navigationRouter(false, this.getFragmentActivity(), false) }
                scoped {
                    PlayUseCase(
                        queue = get(),
                        ytCastContextHolder = get(),
                        prefsWrapper = get(),
                        coroutines = get(),
                        floatingService = get(),
                        playDialog = get(),
                        strings = get()
                    )
                }
                scoped<PlayUseCase.Dialog> {
                    PlayDialog(
                        get<CastPlayerFragment>(),
                        itemFactory = get(),
                        itemModelMapper = get(),
                        navigationRouter = get(),
                        castDialogWrapper = get(),
                        floatingService = get(),
                        log = get(),
                        alertDialogCreator = get(),
                        youtubeApi = get(),
                    )
                }
                scoped { ItemFactory(get(), get(), get()) }
                scoped { ItemModelMapper(get(), get(), get(), get()) }
            }
        }
    }

}