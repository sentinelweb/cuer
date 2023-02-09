package uk.co.sentinelweb.cuer.app.ui.common.mapper

import uk.co.sentinelweb.cuer.app.ui.common.resources.Color
import uk.co.sentinelweb.cuer.domain.MediaDomain

class DurationTextColorMapper {

    fun mapInfoText(media: MediaDomain): Color =
        if (media.isLiveBroadcastUpcoming) {
            Color.upcoming_background
        } else if (media.isLiveBroadcast) {
            Color.live_background
        } else {
            Color.color_on_surface
        }

    fun mapInfoBackgroundItem(media: MediaDomain): Color =
        if (media.isLiveBroadcastUpcoming) {
            Color.upcoming_background
        } else if (media.isLiveBroadcast) {
            Color.live_background
        } else {
            Color.black_transparent_background
        }
}