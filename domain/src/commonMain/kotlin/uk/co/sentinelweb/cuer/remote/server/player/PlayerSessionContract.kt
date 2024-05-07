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
    sealed class PlayerMessage : Message {
        object SkipFwd : PlayerMessage()
        object SkipBack : PlayerMessage()
        object TrackFwd : PlayerMessage()
        object TrackBack : PlayerMessage()
        data class PlayPause(val isPlaying: Boolean?) : PlayerMessage()
        data class TrackSelected(val itemId: OrchestratorContract.Identifier<GUID>, val resetPosition: Boolean) :
            PlayerMessage()

        data class SeekToFraction(val fraction: Float) : PlayerMessage()

    }

    interface Listener {
        fun messageRecieved(message: PlayerMessage)
    }
}
