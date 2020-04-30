package uk.co.sentinelweb.cuer.app.util.mediasession

//import com.google.android.gms.cast.MediaInfo
//import com.google.android.gms.cast.MediaMetadata
import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.util.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class MediaSessionManager constructor(
    private val appState: CuerAppState,
    private val context: Context,
    private val log: LogWrapper
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
        val builder = MediaMetadataCompat.Builder()
            .putText(MediaMetadataCompat.METADATA_KEY_TITLE, media.title)
            .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, media.channelTitle)
            .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, media.id.toString())
        media.duration?.apply {
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this)
        }
        appState.mediaSession?.setMetadata(builder.build())
        appState.currentMedia = media
    }

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

//    fun buildMediaInfo(media: MediaDomain): MediaInfo {
//        val metadata = buildMediaMetadata(media)
//
//        return MediaInfo.Builder(media.url)
//            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
//            .setContentType("videos/mp4")
//            .setMetadata(metadata)
//            .build()
//    }
//
//    private fun buildMediaMetadata(media: MediaDomain): MediaMetadata {
//        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
//
//        metadata.putString(MediaMetadata.KEY_TITLE, media.title)
//        metadata.putString(
//            MediaMetadata.KEY_SUBTITLE,
//            media.description?.substring(0, min(media.description?.length ?: 0, 50))
//        )
//        metadata.addImage(WebImage(Uri.parse("https://www.stateofdigital.com/wp-content/uploads/2012/01/slap-on-wrist.jpg")))
//        return metadata
//    }
}