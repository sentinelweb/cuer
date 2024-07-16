package uk.co.sentinelweb.cuer.remote.server.player

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.remote.server.Message

interface PlayerSessionContract {
    interface Manager {
        fun checkCreateMediaSession(controls: Listener)
        fun destroyMediaSession()
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
        var media: MediaDomain? = null,
        var playbackState: PlayerStateDomain? = null,
        var liveOffset: Long? = null,
    ) : Message

    interface Listener {
        fun messageRecieved(message: PlayerCommandMessage)
    }
}
