package uk.co.sentinelweb.cuer.app.util.mediasession

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.util.wrapper.LogWrapper
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

    fun createMediaSession() {
        if (appState.mediaSession == null) {
            appState.mediaSession = MediaSessionCompat(context, "CuerService")
                .apply {
                    setCallback(CuerMediaSessionCallback())
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
    inner class CuerMediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            log.d("onPlay")
        }

        override fun onPause() {
            log.d("onPause")
        }

        override fun onRewind() {
            log.d("onRewind")
        }

        override fun onSkipToPrevious() {
            log.d("onSkipToPrevious")
        }

        override fun onFastForward() {
            log.d("onFastForward")
        }

        override fun onStop() {
            log.d("onStop")
        }

        override fun onSkipToNext() {
            log.d("onSkipToNext")
        }
    }
}