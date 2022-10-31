package uk.co.sentinelweb.cuer.app.ui.common.mapper

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class DurationTextColorMapper(
    private val res: ResourceWrapper
) {

    fun mapInfoText(media: MediaDomain) =
        if (media.isLiveBroadcastUpcoming) {
            R.color.upcoming_background
        } else if (media.isLiveBroadcast) {
            R.color.live_background
        } else {
            R.color.color_on_surface
        }

    fun mapInfoBackgroundItem(media: MediaDomain) =
        if (media.isLiveBroadcastUpcoming) {
            R.color.upcoming_background
        } else if (media.isLiveBroadcast) {
            R.color.live_background
        } else {
            R.color.black_transparent_background
        }
}