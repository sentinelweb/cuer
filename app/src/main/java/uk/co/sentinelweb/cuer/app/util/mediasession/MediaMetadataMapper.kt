package uk.co.sentinelweb.cuer.app.util.mediasession

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.isLiveOrUpcoming

class MediaMetadataMapper constructor(
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
                ?.also { builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, it.toLong()) }

        }

        bitmap?.apply {
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, this)
        }

        //log.d("map media meta data:${domain.duration}")
        if (!domain.isLiveOrUpcoming()) {
            domain.duration?.apply {
                builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this)
            }
        }
        return builder.build()
    }
}