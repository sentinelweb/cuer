package uk.co.sentinelweb.cuer.app.ui.ytplayer.portrait

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.databinding.ActivityYoutubePortraitBinding
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.CHANNEL_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.LINK
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.WEB_LINK
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.YOUTUBE_CHANNEL
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.ui.play_control.mvi.CastPlayerMviFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.ChannelOpen
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.LinkOpen
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise
import kotlin.math.abs

class YoutubePortraitActivity : AppCompatActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScopeWithSource()

    private val controller: PlayerController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val playerFragment: CastPlayerMviFragment by inject()
    private val navMapper: NavigationMapper by inject()

    private lateinit var mviView: YoutubePortraitActivity.MviViewImpl
    private lateinit var binding: ActivityYoutubePortraitBinding

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubePortraitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mviView = MviViewImpl(binding.portraitPlayerVideo)
        getLifecycle().addObserver(binding.portraitPlayerVideo)
        playerFragment.initMediaRouteButton()
        controller.onViewCreated(listOf(mviView, playerFragment.mviView), lifecycle.asMviLifecycle())
        binding.portraitPlayerDescription.interactions = object : DescriptionContract.Interactions {
            override fun onLinkClick(urlString: String) {
                mviView.dispatch(Event.LinkClick(urlString))
            }

            override fun onChannelClick() {
                mviView.dispatch(Event.ChannelClick)
            }

            override fun onSelectPlaylistChipClick(model: ChipModel) = Unit

            override fun onRemovePlaylist(chipModel: ChipModel) = Unit

        }
    }

    // region MVI view
    inner class MviViewImpl(playerView: YouTubePlayerView) :
        BaseMviView<Model, Event>(),
        PlayerContract.View {
        private var player: YouTubePlayer? = null
        private var lastPositionSec: Float = -1f

        init {
            playerView.addYouTubePlayerListener(object : YouTubePlayerListener {
                override fun onApiChange(youTubePlayer: YouTubePlayer) = Unit

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    player = youTubePlayer
                    if (abs(lastPositionSec - second) > 1) {
                        dispatch(Event.SendPosition((second * 1000).toInt()))
                        lastPositionSec = second
                    }
                }

                override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                    player = youTubePlayer
                    dispatch(Event.PlayerStateChanged(PlayerStateDomain.ERROR))
                }

                override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {
                    player = youTubePlayer
                }

                override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {
                    player = youTubePlayer
                }

                override fun onReady(youTubePlayer: YouTubePlayer) {
                    player = youTubePlayer
                    apply { dispatch(Event.Initialised) }
                }

                override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                    player = youTubePlayer
                    val playStateDomain = when (state) {
                        PlayerConstants.PlayerState.ENDED -> PlayerStateDomain.ENDED
                        PlayerConstants.PlayerState.PAUSED -> PlayerStateDomain.PAUSED
                        PlayerConstants.PlayerState.PLAYING -> PlayerStateDomain.PLAYING
                        PlayerConstants.PlayerState.BUFFERING -> PlayerStateDomain.BUFFERING
                        PlayerConstants.PlayerState.UNSTARTED -> PlayerStateDomain.UNSTARTED
                        PlayerConstants.PlayerState.UNKNOWN -> PlayerStateDomain.UNKNOWN
                        PlayerConstants.PlayerState.VIDEO_CUED -> PlayerStateDomain.VIDEO_CUED
                    }
                    dispatch(Event.PlayerStateChanged(playStateDomain))
                    //updateMediaSessionManagerPlaybackState()// todo ??
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    player = youTubePlayer
                }

                override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                    player = youTubePlayer
                    dispatch(Event.PlayerStateChanged(PlayerStateDomain.VIDEO_CUED))
                }

                override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
                    player = youTubePlayer
                }

            })
        }

        override val renderer: ViewRenderer<Model> = diff {
            // todo use compare and get start pos
            diff(get = Model::platformId, set = { id: String? ->
                log.d("got id : $id")
                id?.apply { player?.loadVideo(this, 0f) }
            })
            diff(get = Model::description, set = binding.portraitPlayerDescription::setModel)
        }

        override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
            when (label) {
                is PlayerContract.MviStore.Label.Command -> label.command.let { command ->
                    when (command) {
                        is PlayerContract.PlayerCommand.Play -> player?.play()
                        is PlayerContract.PlayerCommand.Pause -> player?.pause()
                        is PlayerContract.PlayerCommand.SkipBack -> player?.seekTo(lastPositionSec - command.ms / 1000)
                        is PlayerContract.PlayerCommand.SkipFwd -> player?.seekTo(lastPositionSec + command.ms / 1000)
                        is PlayerContract.PlayerCommand.SeekTo -> player?.seekTo(command.ms.toFloat() / 1000)
                    }
                }
                is LinkOpen ->
                    navMapper.navigate(NavigationModel(WEB_LINK, mapOf(LINK to label.url)))
                is ChannelOpen ->
                    navMapper.navigate(NavigationModel(YOUTUBE_CHANNEL, mapOf(CHANNEL_ID to label.channel.platformId!!)))
            }
        }
    }

    companion object {

        fun start(c: Context, playlistItem: PlaylistItemDomain) = c.startActivity(
            Intent(c, YoutubePortraitActivity::class.java).apply {
                putExtra(NavigationModel.Param.PLAYLIST_ITEM.toString(), playlistItem.serialise())
            })
    }

}