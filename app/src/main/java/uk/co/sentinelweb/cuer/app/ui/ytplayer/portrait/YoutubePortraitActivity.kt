package uk.co.sentinelweb.cuer.app.ui.ytplayer.portrait

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.WEB_LINK
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.YOUTUBE_CHANNEL
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.ui.play_control.mvi.CastPlayerMviFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Screen.DESCRIPTION
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Screen.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
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
    private val playlistFragment: PlaylistFragment by inject()
    private val navMapper: NavigationMapper by inject()
    private val itemLoader: PlayerContract.PlaylistItemLoader by inject()

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
                mviView.dispatch(LinkClick(urlString))
            }

            override fun onChannelClick() {
                mviView.dispatch(ChannelClick)
            }

            override fun onSelectPlaylistChipClick(model: ChipModel) = Unit

            override fun onRemovePlaylist(chipModel: ChipModel) = Unit

        }
        val playlistItem = itemLoader.load()
        playlistFragment.arguments = bundleOf(
            HEADLESS.name to true,
            SOURCE.name to OrchestratorContract.Source.LOCAL.toString(),
            PLAYLIST_ID.name to (playlistItem?.playlistId ?: throw IllegalArgumentException("Playlist ID is null")),
            PLAYLIST_ITEM_ID.name to (playlistItem.id ?: throw IllegalArgumentException("Playlist item is null")),
        )
        playlistFragment.external.interactions = playlistInteractions
    }

    // region MVI view
    inner class MviViewImpl(playerView: YouTubePlayerView) :
        BaseMviView<Model, Event>(),
        PlayerContract.View {
        private var player: YouTubePlayer? = null
        private var lastPositionSec: Float = -1f

        init {
            val playWhenReady = object {
                var id: String? = null
                var pos: Long = 0
            }
            playerView.addYouTubePlayerListener(object : YouTubePlayerListener {
                override fun onApiChange(youTubePlayer: YouTubePlayer) = Unit

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    player = youTubePlayer
                    if (abs(lastPositionSec - second) > 0.1) {
                        lastPositionSec = second
                    }
                    if (abs(lastPositionSec - second) > 1) {
                        dispatch(PositionReceived((second * 1000).toLong()))
                    }
                }

                override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                    player = youTubePlayer
                    dispatch(PlayerStateChanged(ERROR))
                }

                override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {
                    player = youTubePlayer
                }

                override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {
                    player = youTubePlayer
                }

                override fun onReady(youTubePlayer: YouTubePlayer) {
                    player = youTubePlayer
                    log.d("onReady")

                    dispatch(PlayerStateChanged(VIDEO_CUED))
                }

                override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                    player = youTubePlayer
                    val playStateDomain = when (state) {
                        PlayerConstants.PlayerState.ENDED -> ENDED
                        PlayerConstants.PlayerState.PAUSED -> PAUSED
                        PlayerConstants.PlayerState.PLAYING -> PLAYING
                        PlayerConstants.PlayerState.BUFFERING -> BUFFERING
                        PlayerConstants.PlayerState.UNSTARTED -> UNSTARTED
                        PlayerConstants.PlayerState.UNKNOWN -> UNKNOWN
                        PlayerConstants.PlayerState.VIDEO_CUED -> VIDEO_CUED
                    }
                    dispatch(PlayerStateChanged(playStateDomain))
                    //updateMediaSessionManagerPlaybackState()// todo ??
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    player = youTubePlayer
                    dispatch(DurationReceived((duration * 1000).toLong()))
                }

                override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                    player = youTubePlayer
                    dispatch(IdReceived(videoId))
                    //dispatch(PlayerStateChanged(VIDEO_CUED))
                }

                override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
                    player = youTubePlayer
                }

            })

        }

        override val renderer: ViewRenderer<Model> = diff {
            // todo use compare and get start pos
//            diff(get = Model::platformId, set = { id: String? ->
//                id?.apply {
//                    log.d("loadVideo:$this")
//                    player?.loadVideo(this, 0f)
//                }
//            })
            diff(get = Model::description, set = {
                log.d("set description")
                binding.portraitPlayerDescription.setModel(it)
            })
            diff(get = Model::screen, set = {
                when (it) {
                    DESCRIPTION -> {
                        binding.portraitPlayerDescription.isVisible = true
                        binding.portraitPlayerPlaylist.isVisible = false
                    }
                    PLAYLIST -> {
                        binding.portraitPlayerDescription.isVisible = false
                        binding.portraitPlayerPlaylist.isVisible = true
                    }
                }
            })
        }

        override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
            when (label) {

                is Command -> label.command.let { command ->
                    log.d(command.toString())
                    when (command) {
                        is PlayerContract.PlayerCommand.Load -> player?.loadVideo(command.platformId, command.startPosition / 1000f)
                        is PlayerContract.PlayerCommand.Play -> player?.play()
                        is PlayerContract.PlayerCommand.Pause -> player?.pause()
                        is PlayerContract.PlayerCommand.SkipBack -> player?.seekTo(lastPositionSec - command.ms / 1000f)
                        is PlayerContract.PlayerCommand.SkipFwd -> player?.seekTo(lastPositionSec + command.ms / 1000f)
                        is PlayerContract.PlayerCommand.SeekTo -> {
                            log.d(command.toString())
                            player?.seekTo(command.ms.toFloat() / 1000f)
                        }
                    }
                }
                is LinkOpen ->
                    navMapper.navigate(NavigationModel(WEB_LINK, mapOf(LINK to label.url)))
                is ChannelOpen ->
                    label.channel.platformId?.let { id -> navMapper.navigate(NavigationModel(YOUTUBE_CHANNEL, mapOf(CHANNEL_ID to id))) }
            }
        }
    }

    private val playlistInteractions = object : PlaylistContract.Interactions {

        override fun onPlayStartClick(item: PlaylistItemDomain) {
            mviView.dispatch(Event.TrackClick(item, true))
        }

        override fun onRelated(item: PlaylistItemDomain) {

        }

        override fun onView(item: PlaylistItemDomain) {
            mviView.dispatch(Event.TrackClick(item, false))
        }

        override fun onPlay(item: PlaylistItemDomain) {
            mviView.dispatch(Event.TrackClick(item, false))
        }

    }

    companion object {

        fun start(c: Context, playlistItem: PlaylistItemDomain) = c.startActivity(
            Intent(c, YoutubePortraitActivity::class.java).apply {
                putExtra(PLAYLIST_ITEM.toString(), playlistItem.serialise())
            })
    }


}