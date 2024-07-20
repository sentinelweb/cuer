package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.State
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromeCastContract
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper

class PlaylistMviUtil(
    private val queue: QueueMediatorContract.Producer,
    private val ytCastContextHolder: ChromeCastContract.PlayerContextHolder,
    private val multiPrefs: MultiPlatformPreferencesWrapper
) {
    fun isQueuedPlaylist(state: State): Boolean = state.playlistIdentifier == queue.playlistId

    fun isPlaylistPlaying(state: State): Boolean = isQueuedPlaylist(state) && ytCastContextHolder.isConnected()

    fun isPlaylistPinned(state: State): Boolean =
        state.playlist
            ?.takeIf { multiPrefs.pinnedPlaylistId != null }
            ?.let { multiPrefs.pinnedPlaylistId == state.playlist?.id?.id }
            ?: false
}