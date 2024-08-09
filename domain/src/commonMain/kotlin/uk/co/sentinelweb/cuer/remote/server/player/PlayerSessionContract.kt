package uk.co.sentinelweb.cuer.remote.server.player

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.remote.server.Message

interface PlayerSessionContract {
    interface Manager {
        fun checkCreateMediaSession(controls: Listener)
        fun destroyMediaSession()
//        fun setMedia(item: PlaylistItemDomain, playlist: PlaylistDomain?)
//        fun updatePlaybackState(
//            item: PlaylistItemDomain,
//            state: PlayerStateDomain,
//            liveOffset: Long?,
//            playlist: PlaylistDomain?
//        )

        fun setMedia(media: MediaDomain, playlist: PlaylistDomain?)
        fun updatePlaybackState(
            media: MediaDomain,
            state: PlayerStateDomain,
            liveOffset: Long?,
            playlist: PlaylistDomain?
        )
        fun setVolume(volume: Float)
        fun setVolumeMax(volumeMax: Float)
        fun setScreen(screen: PlayerNodeDomain.Screen)
    }

    @Serializable
    sealed class PlayerCommandMessage : Message {
        object SkipFwd : PlayerCommandMessage()
        object SkipBack : PlayerCommandMessage()
        object TrackFwd : PlayerCommandMessage()
        object TrackBack : PlayerCommandMessage()
        object Stop : PlayerCommandMessage()
        data class PlayPause(val isPlaying: Boolean?) : PlayerCommandMessage()
        data class TrackSelected(val itemId: Identifier<GUID>, val resetPosition: Boolean) : PlayerCommandMessage()

        data class SeekToFraction(val fraction: Float) : PlayerCommandMessage()
        data class Volume(val volume: Float) : PlayerCommandMessage()
    }

    @Serializable
    data class PlayerStatusMessage(
        val id: Identifier<GUID>,
        val item: PlaylistItemDomain,
        val playbackState: PlayerStateDomain,
        val liveOffset: Long,
        val volume: Float,
        val volumeMax: Float,
        val screen: PlayerNodeDomain.Screen?
    ) : Message

    interface Listener {
        fun messageRecieved(message: PlayerCommandMessage)
    }
}
