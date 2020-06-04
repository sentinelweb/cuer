package uk.co.sentinelweb.cuer.app.util.mediasession

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

class MediaSessionManager constructor(
    private val appState: CuerAppState,
    private val context: Context,
    private val log: LogWrapper,
    private val metadataMapper: MediaMetadataMapper,
    private val playbackStateMapper: PlaybackStateMapper
) {
    init {
        log.tag = this::class.java.simpleName
    }

    fun createMediaSession(controls: CastPlayerContract.PlayerControls.Listener) {
        if (appState.mediaSession == null) {
            appState.mediaSession = MediaSessionCompat(context, "CuerService")
                .apply {
                    setCallback(CuerMediaSessionCallback(controls))
                    isActive = true
                }
        }
    }

    fun destroyMediaSession() {
        appState.mediaSession?.release()
        appState.mediaSession = null
    }

    fun setMedia(media: MediaDomain) {
        appState.mediaSession?.setMetadata(metadataMapper.map(media))
    }

    fun updatePlaybackState(media: MediaDomain?, state: PlayerStateDomain) {
        appState.mediaSession?.setPlaybackState(playbackStateMapper.map(media, state))
    }

    // todo this will go somewhere near the player controls
    inner class CuerMediaSessionCallback(private val controls: CastPlayerContract.PlayerControls.Listener) :
        MediaSessionCompat.Callback() {

        override fun onPlay() {
            controls.play()
            log.d("onPlay")
        }

        override fun onPause() {
            controls.pause()
            log.d("onPause")
        }

        override fun onSkipToPrevious() {
            controls.trackBack()
            log.d("onSkipToPrevious")
        }

        override fun onStop() {
            controls.pause()
            log.d("onStop")
        }

        override fun onSkipToNext() {
            controls.trackFwd()
            log.d("onSkipToNext")
        }

        override fun onSeekTo(pos: Long) {
            controls.seekTo(pos)
            log.d("onSeekTo: $pos")
        }

        override fun onRewind() {
            // todo add methods in interface (if needed)
            log.d("onRewind")
        }

        override fun onFastForward() {
            // todo add methods in interface (if needed)
            log.d("onFastForward")
        }
    }
}