package uk.co.sentinelweb.cuer.app.util.mediasession

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import androidx.core.graphics.scale
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.extension.cropShapedBitmap
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.isLiveOrUpcoming

class MediaMetadataMapper constructor(
    private val res: ResourceWrapper,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun map(domain: MediaDomain, bitmap: Bitmap?, playlist: PlaylistDomain?): MediaMetadataCompat {
        val builder = MediaMetadataCompat.Builder()
            .putText(MediaMetadataCompat.METADATA_KEY_TITLE, domain.title)
            .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, domain.channelData.title)
            .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, domain.id.toString())
        playlist?.apply {
            builder
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, domain.id.toString())
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, playlist.items.size.toLong())
            items.indexOfFirst { it.media.platformId == domain.platformId }
                .takeIf { it != -1 }
                ?.also {
                    builder.putLong(
                        MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER,
                        it.toLong()
                    )
                }

        }

        bitmap?.runCatching {
            val targetWidth = res.getDimensionPixelSize(R.dimen.notif_image_size_sdk_31)
            val aspect = bitmap.height / bitmap.width
            this.scale(targetWidth, targetWidth * aspect)
                .run { cropShapedBitmap(res) }
        }
            ?.onSuccess { builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it) }
            ?.onFailure { builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap) }

        //log.d("map media meta data:${domain.duration}")
        if (!domain.isLiveOrUpcoming()) {
            domain.duration?.apply {
                builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this)
            }
        }
        return builder.build()
    }
}