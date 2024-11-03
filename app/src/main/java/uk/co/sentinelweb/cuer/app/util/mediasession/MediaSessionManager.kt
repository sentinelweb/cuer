package uk.co.sentinelweb.cuer.app.util.mediasession

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class MediaSessionManager(
    private val appState: CuerAppState,
    private val state: State,
    private val context: Context,
    private val log: LogWrapper,
    private val metadataMapper: MediaMetadataMapper,
    private val playbackStateMapper: PlaybackStateMapper,
) : MediaSessionContract.Manager {

    data class State(
        var bitmapUrl: String? = null,
        var bitmap: Bitmap? = null,
    )

    init {
        log.tag(this)
    }

    override fun checkCreateMediaSession(controls: PlayerContract.PlayerControls.Listener) {
        if (appState.mediaSession == null) {
            appState.mediaSession = MediaSessionCompat(context, "CuerCastService")
                .apply {
                    setCallback(CuerMediaSessionCallback(controls))
                    isActive = true
                }
        } else {
            appState.mediaSession?.setCallback(CuerMediaSessionCallback(controls))
        }
        appState.mediaSession?.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
    }

    override fun destroyMediaSession() {
        appState.mediaSession?.isActive = false
        appState.mediaSession?.release()
        appState.mediaSession = null
        state.bitmap = null
        state.bitmapUrl = null
    }

    override fun setItem(item: PlaylistItemDomain, playlist: PlaylistDomain?) {
        if (item.media.thumbNail == null) {
            state.bitmapUrl = null
            state.bitmap = null
        } else if (item.media.thumbNail?.url != state.bitmapUrl) {
            state.bitmapUrl = item.media.thumbNail?.url
            Glide.with(context).asBitmap().load(state.bitmapUrl).into(BitmapLoadTarget(item.media, playlist))
        }
        appState.mediaSession?.setMetadata(metadataMapper.map(item.media, state.bitmap, playlist))
    }

    inner class BitmapLoadTarget(
        private val media: MediaDomain,
        val playlist: PlaylistDomain?,
    ) : CustomTarget<Bitmap?>() {

        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
            state.bitmap = bitmap
            appState.mediaSession?.setMetadata(metadataMapper.map(media, state.bitmap, playlist))
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
    }

    override fun updatePlaybackState(
        item: PlaylistItemDomain,
        state: PlayerStateDomain,
        liveOffset: Long?,
        playlist: PlaylistDomain?
    ) {
        appState.mediaSession?.setPlaybackState(playbackStateMapper.map(item.media, state, liveOffset, playlist))
    }

//    override fun setRemotePlaybackType() {
//        // todo make a volume provider for cuercast and chrome cast
//        // or maybe from cast controller
//        val myVolumeProvider = object : VolumeProviderCompat(
//            VolumeProviderCompat.VOLUME_CONTROL_ABSOLUTE, /* max volume */
//            100, /* current volume */
//            50
//        ) {
//            override fun onAdjustVolume(direction: Int) {
//                if (direction > 0) {
//                    // volume was increased
//                } else {
//                    // volume was decreased
//                }
//            }
//
//            override fun onSetVolumeTo(volume: Int) {
//                // volume was set to a specific value
//            }
//        }
//
//        appState.mediaSession?.setPlaybackToRemote(myVolumeProvider)
//    }

    inner class CuerMediaSessionCallback(private val controls: PlayerContract.PlayerControls.Listener) :
        MediaSessionCompat.Callback() {

        override fun onPlay() {
            controls.play()
            log.d("onPlay")
        }

        override fun onPause() {
            controls.pause()
            log.d("onPause")
        }

        override fun onStop() {
            controls.pause()
            log.d("onStop")
        }

        override fun onSkipToPrevious() {
            controls.trackBack()
            log.d("onSkipToPrevious")
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
            controls.skipBack()
            log.d("onRewind")
        }

        override fun onFastForward() {
            controls.skipFwd()
            log.d("onFastForward")
        }
    }
}
