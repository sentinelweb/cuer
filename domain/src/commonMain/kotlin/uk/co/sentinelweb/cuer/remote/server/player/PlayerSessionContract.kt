package uk.co.sentinelweb.cuer.remote.server.player

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
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
    }

    @Serializable
    sealed class PlayerCommandMessage : Message {
        object SkipFwd : PlayerCommandMessage()
        object SkipBack : PlayerCommandMessage()
        object TrackFwd : PlayerCommandMessage()
        object TrackBack : PlayerCommandMessage()
        data class PlayPause(val isPlaying: Boolean?) : PlayerCommandMessage()
        data class TrackSelected(val itemId: OrchestratorContract.Identifier<GUID>, val resetPosition: Boolean) :
            PlayerCommandMessage()

        data class SeekToFraction(val fraction: Float) : PlayerCommandMessage()
    }

    @Serializable
    data class PlayerStatusMessage(
        val id: OrchestratorContract.Identifier<GUID>,
        var item: PlaylistItemDomain,
        var playbackState: PlayerStateDomain,
        var liveOffset: Long,
    ) : Message

    interface Listener {
        fun messageRecieved(message: PlayerCommandMessage)
    }
}
