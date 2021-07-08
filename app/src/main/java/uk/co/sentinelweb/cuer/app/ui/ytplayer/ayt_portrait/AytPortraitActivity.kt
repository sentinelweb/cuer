package uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait

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
import uk.co.sentinelweb.cuer.app.databinding.ActivityAytPortraitBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.ui.play_control.mvi.CastPlayerMviFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Screen.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.ui.ytplayer.LocalPlayerCastListener
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise
import kotlin.math.abs

class AytPortraitActivity : AppCompatActivity(),
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
    private val toast: ToastWrapper by inject()
    private val castListener: LocalPlayerCastListener by inject()
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder by inject()

    private lateinit var mviView: AytPortraitActivity.MviViewImpl
    private lateinit var binding: ActivityAytPortraitBinding

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAytPortraitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
        //ytContextHolder.create()
        castListener.listen()
    }

    override fun onDestroy() {
        super.onDestroy()
        castListener.release()
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
        binding.portraitPlayerFullscreen.setOnClickListener { mviView.dispatch(FullScreenClick) }
        binding.portraitPlayerPip.setOnClickListener { mviView.dispatch(PipClick); }
    }

    // region MVI view
    inner class MviViewImpl(playerView: YouTubePlayerView) :
        BaseMviView<Model, Event>(),
        PlayerContract.View {
        private var player: YouTubePlayer? = null
        private var lastPositionSec: Float = -1f
        private var lastPositionSend: Float = -1f

        init {
            playerView.addYouTubePlayerListener(object : YouTubePlayerListener {
                override fun onApiChange(youTubePlayer: YouTubePlayer) = Unit

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    player = youTubePlayer
                    lastPositionSec = second
                    if (abs(lastPositionSend - second) > 1) {
                        lastPositionSend = second
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
                        is Load -> player?.loadVideo(command.platformId, command.startPosition / 1000f)
                        is Play -> player?.play()
                        is Pause -> player?.pause()
                        is SkipBack -> player?.seekTo(lastPositionSec - command.ms / 1000f)
                        is SkipFwd -> player?.seekTo(lastPositionSec + command.ms / 1000f)
                        is SeekTo -> {
                            log.d(command.toString())
                            player?.seekTo(command.ms.toFloat() / 1000f)
                        }
                    }
                }
                is LinkOpen ->
                    navMapper.navigate(NavigationModel(WEB_LINK, mapOf(LINK to label.url)))
                is ChannelOpen ->
                    label.channel.platformId?.let { id -> navMapper.navigate(NavigationModel(YOUTUBE_CHANNEL, mapOf(CHANNEL_ID to id))) }
                is FullScreenPlayerOpen -> label.also {
                    navMapper.navigate(NavigationModel(LOCAL_PLAYER_FULL, mapOf(PLAYLIST_ITEM to it.item)))
                    finish()
                }
                is PipPlayerOpen -> toast.show("PIP Open")
                is PortraitPlayerOpen -> toast.show("Already in protrait mode - shouldnt get here")
            }
        }
    }

    private val playlistInteractions = object : PlaylistContract.Interactions {

        override fun onPlayStartClick(item: PlaylistItemDomain) {
            mviView.dispatch(TrackClick(item, true))
        }

        override fun onRelated(item: PlaylistItemDomain) {

        }

        override fun onView(item: PlaylistItemDomain) {
            mviView.dispatch(TrackClick(item, false))
        }

        override fun onPlay(item: PlaylistItemDomain) {
            mviView.dispatch(TrackClick(item, false))
        }

    }

    companion object {

        fun start(c: Context, playlistItem: PlaylistItemDomain) = c.startActivity(
            Intent(c, AytPortraitActivity::class.java).apply {
                putExtra(PLAYLIST_ITEM.toString(), playlistItem.serialise())
            })
    }


}