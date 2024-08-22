package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import kotlinx.datetime.Clock
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract.DescriptionModel
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.State.TargetDetails
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.*
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.UNKNOWN

interface PlayerContract {

    data class PlayerConfig(
        val maxVolume: Float
    )

    interface MviStore : Store<Intent, State, Label> {
        fun endSession()

        sealed class Intent {
            object SkipFwd : Intent()
            object SkipBack : Intent()
            object TrackFwd : Intent()
            object TrackBack : Intent()
            object SkipFwdSelect : Intent()
            object SkipBackSelect : Intent()
            object PlaylistView : Intent()
            object PlaylistItemView : Intent()
            object ChannelOpen : Intent()
            object FullScreenPlayerOpen : Intent()
            object PortraitPlayerOpen : Intent()
            object PipPlayerOpen : Intent()
            object Support : Intent()
            object StarClick : Intent()
            object Stop : Intent()
            object FocusWindow : Intent()

            object OpenInApp : Intent()
            object Share : Intent()

            data class InitFromService(val playlistAndItem: PlaylistAndItemDomain) : Intent()
            data class PlayItemFromService(val playlistAndItem: PlaylistAndItemDomain) : Intent()
            data class PlayPause(val isPlaying: Boolean?) : Intent()
            data class Position(val ms: Long) : Intent()
            data class PlayState(val state: PlayerStateDomain) : Intent()
            data class TrackChange(val item: PlaylistItemDomain) : Intent()
            data class TrackSelected(val item: PlaylistItemDomain, val resetPosition: Boolean) : Intent()
            data class PlaylistChange(val item: PlaylistDomain) : Intent()
            data class SeekToFraction(val fraction: Float) : Intent()
            data class SeekToPosition(val ms: Long) : Intent()
            data class VolumeChanged(val vol: Float) : Intent()
            data class LinkOpen(val link: LinkDomain.UrlLinkDomain) : Intent()
            data class Duration(val ms: Long) : Intent()
            data class Id(val videoId: String) : Intent()
            data class ScreenAcquired(val screen: PlayerNodeDomain.Screen) : Intent()
        }

        sealed class Label {
            object Stop : Label()
            object FocusWindow : Label()
            data class Command(val command: PlayerCommand) : Label()
            data class LinkOpen(val link: LinkDomain.UrlLinkDomain) : Label()
            data class ChannelOpen(val channel: ChannelDomain) : Label()
            data class FullScreenPlayerOpen(val item: PlaylistAndItemDomain) : Label()
            data class PortraitPlayerOpen(val item: PlaylistAndItemDomain) : Label()
            data class PipPlayerOpen(val item: PlaylistAndItemDomain) : Label()
            data class ShowSupport(val item: PlaylistItemDomain) : Label()
            data class Share(val item: PlaylistItemDomain) : Label()
            data class ItemOpen(val item: PlaylistItemDomain) : Label()
        }

        enum class Content { DESCRIPTION, PLAYLIST, PLAYLISTS }

        data class State(
            val item: PlaylistItemDomain? = null,
            val playlist: PlaylistDomain? = null,
            val playerState: PlayerStateDomain = UNKNOWN,
            val skipFwdText: String = "-",
            val skipBackText: String = "-",
            val content: Content = Content.DESCRIPTION,
            val position: Long = -1,
            val volume: Float = 0f,
            val screen: PlayerNodeDomain.Screen? = null
        ) {
            fun playlistAndItem(): PlaylistAndItemDomain? = item?.let {
                PlaylistAndItemDomain(
                    item = it,
                    playlistId = playlist?.id,
                    playlistTitle = playlist?.title
                )
            }
        }
    }

    interface View : MviView<View.Model, View.Event> {
        suspend fun processLabel(label: Label)

        data class Model(
            val texts: Texts,
            val playState: PlayerStateDomain,
            val buttons: Buttons,
            val times: Times,
            val itemImage: String?,
            val description: DescriptionModel,
            val content: Content,
            val playlistItem: PlaylistItemDomain?,
            val playlistAndItem: PlaylistAndItemDomain? = null,
            val volume: Float = 0f,
        ) {
            data class Buttons(
                val nextTrackEnabled: Boolean,
                val prevTrackEnabled: Boolean,
                val seekEnabled: Boolean,
            )

            data class Texts(
                val title: String?,
                val playlistTitle: String?,
                val playlistData: String?,
                val nextTrackText: String?,
                val lastTrackText: String?,
                val skipFwdText: String?,
                val skipBackText: String?,
                val volumeText: String,
            )

            data class Times(
                val positionText: String,
                val durationText: String,
                val liveTime: String?,
                val isLive: Boolean,
                val seekBarFraction: Float,
            )

            companion object {
                fun blankModel(): Model = Model(
                    texts = Texts(
                        title = "No title",
                        playlistTitle = "playlistTitle",
                        playlistData = "playlistData",
                        nextTrackText = "nextTrackText",
                        lastTrackText = "lastTrackText",
                        skipFwdText = "skipFwdText",
                        skipBackText = "skipBackText",
                        volumeText = "0",
                    ),
                    buttons = Buttons(false, false, false),
                    description = DescriptionModel(
                        title = "Title",
                        description = "Description",
                        playlistChips = listOf(),
                        channelTitle = "channelTitle",
                        channelThumbUrl = null,
                        channelDescription = "channelDescription",
                        pubDate = "pubDate",
                        ribbonActions = listOf(),
                        info = DescriptionModel.Info(platform = PlatformDomain.OTHER, platformId = "platformId")
                    ),
                    itemImage = null,
                    playState = UNKNOWN,
                    playlistItem = PlaylistItemDomain(
                        id = null,
                        dateAdded = Clock.System.now(),
                        order = 0,
                        playlistId = null,
                        media = MediaDomain(
                            id = null,
                            url = "media url",
                            platform = PlatformDomain.OTHER,
                            platformId = "platformId",
                            mediaType = MediaDomain.MediaTypeDomain.WEB,
                            channelData = ChannelDomain(
                                id = null,
                                title = "channel title",
                                platform = PlatformDomain.OTHER,
                                platformId = "platformId",
                            )
                        )
                    ),
                    playlistAndItem = null,
                    content = Content.DESCRIPTION,
                    times = Times("00:00", "??:??", liveTime = null, isLive = false, seekBarFraction = 0f)
                )
            }
        }

        sealed class Event {
            object TrackFwdClicked : Event()
            object TrackBackClicked : Event()
            object SkipFwdClicked : Event()
            object SkipBackClicked : Event()
            object SkipFwdSelectClicked : Event()
            object SkipBackSelectClicked : Event()
            object ItemClicked : Event()
            object PlaylistClicked : Event()
            object FullScreenClick : Event()
            object PortraitClick : Event()
            object PipClick : Event()
            object ChannelClick : Event()
            object Support : Event()
            object StarClick : Event()
            object ShareClick : Event()
            object OpenClick : Event()

            data class VolumeChanged(val vol: Float) : Event()
            data class SeekBarChanged(val fraction: Float) : Event()
            data class PlayPauseClicked(val isPlaying: Boolean? = null) : Event()
            data class PositionReceived(val ms: Long) : Event()
            data class PlayerStateChanged(val state: PlayerStateDomain) : Event()
            data class TrackClick(val item: PlaylistItemDomain, val resetPosition: Boolean) : Event()
            data class LinkClick(val link: LinkDomain.UrlLinkDomain) : Event()
            data class DurationReceived(val ms: Long) : Event()
            data class IdReceived(val videoId: String) : Event()
            data class OnInitFromService(val playlistAndItem: PlaylistAndItemDomain) : Event()
            data class OnPlayItemFromService(val playlistAndItem: PlaylistAndItemDomain) : Event()
            data class OnSeekToPosition(val ms: Long) : Event()
            data class OnScreenAcquired(val screen: PlayerNodeDomain.Screen) : Event()
        }
    }

    sealed class PlayerCommand {
        object Play : PlayerCommand()
        object Pause : PlayerCommand()
        data class Load(val platformId: String, val startPosition: Long) : PlayerCommand()
        data class SkipFwd(val ms: Int) : PlayerCommand()
        data class SkipBack(val ms: Int) : PlayerCommand()
        data class SeekTo(val ms: Long) : PlayerCommand()
    }

    interface PlaylistItemLoader {
        fun load(): PlaylistAndItemDomain?
    }

    enum class ControlTarget {
        Local, ChromeCast, CuerCast, FloatingWindow
    }

    enum class CastConnectionState {
        Connected, Connecting, Disconnected
    }

    interface PlayerControls {
        //        fun initMediaRouteButton()
        //fun setConnectionState(connState: CastConnectionState)
        fun setCastDetails(details: TargetDetails)
        fun setPlayerState(playState: PlayerStateDomain)
        fun addListener(l: Listener)
        fun removeListener(l: Listener)
        fun setCurrentSecond(secondsFloat: Float)
        fun setDuration(durationFloat: Float)
        fun error(msg: String)
        fun setTitle(title: String)
        fun reset()
        fun restoreState()
        fun setPlaylistName(name: String)
        fun setPlaylistImage(image: ImageDomain?)
        fun setPlaylistItem(playlistItem: PlaylistItemDomain?)
        fun disconnectSource()
        fun seekTo(ms: Long)
        fun getPlaylistItem(): PlaylistItemDomain?
        fun setButtons(buttons: View.Model.Buttons)

        interface Listener {
            fun play()
            fun pause()
            fun trackBack()
            fun trackFwd()
            fun seekTo(positionMs: Long)
            fun getLiveOffsetMs(): Long
            fun skipBack()
            fun skipFwd()
        }

    }
}
