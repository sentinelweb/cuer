package uk.co.sentinelweb.cuer.app.util.mediasession

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class MediaSessionManager constructor(
    private val appState: CuerAppState,
    private val state: State,
    private val context: Context,
    private val log: LogWrapper,
    private val metadataMapper: MediaMetadataMapper,
    private val playbackStateMapper: PlaybackStateMapper,
) {
    init {
        log.tag(this)
    }

    data class State constructor(
        var bitmapUrl: String? = null,
        var bitmap: Bitmap? = null,
    )

    fun checkCreateMediaSession(controls: CastPlayerContract.PlayerControls.Listener) {
        if (appState.mediaSession == null) {
            appState.mediaSession = MediaSessionCompat(context, "CuerCastService")
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

    fun setMedia(media: MediaDomain, playlist: PlaylistDomain?) {
        if (media.thumbNail == null) {
            state.bitmapUrl = null
            state.bitmap = null
        } else if (media.thumbNail?.url != state.bitmapUrl) {
            state.bitmapUrl = media.thumbNail?.url
            Glide.with(context).asBitmap().load(state.bitmapUrl).into(BitmapLoadTarget(media, playlist))
        }
        appState.mediaSession?.setMetadata(metadataMapper.map(media, state.bitmap, playlist))
    }

    inner class BitmapLoadTarget constructor(
        private val media: MediaDomain,
        val playlist: PlaylistDomain?,
    ) : CustomTarget<Bitmap?>() {

        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
            state.bitmap = bitmap
            appState.mediaSession?.setMetadata(metadataMapper.map(media, state.bitmap, playlist))
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
    }

    fun updatePlaybackState(media: MediaDomain, state: PlayerStateDomain, liveOffset: Long?) {
        appState.mediaSession?.setPlaybackState(playbackStateMapper.map(media, state, liveOffset))
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