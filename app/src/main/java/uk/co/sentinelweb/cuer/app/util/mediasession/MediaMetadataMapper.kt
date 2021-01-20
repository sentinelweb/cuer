package uk.co.sentinelweb.cuer.app.util.mediasession

import android.support.v4.media.MediaMetadataCompat
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class MediaMetadataMapper constructor(
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    fun map(domain: MediaDomain): MediaMetadataCompat {
        val builder = MediaMetadataCompat.Builder()
            .putText(MediaMetadataCompat.METADATA_KEY_TITLE, domain.title)
            .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, domain.channelData.title)
            .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, domain.id.toString())
        //log.d("map media meta data:${domain.duration}")
        if (!domain.isLiveBroadcast) {
            domain.duration?.apply {
                builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this)
            }
        }
        return builder.build()
    }
}