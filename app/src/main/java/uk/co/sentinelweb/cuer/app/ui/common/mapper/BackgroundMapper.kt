package uk.co.sentinelweb.cuer.app.ui.common.mapper

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class BackgroundMapper(
    private val res: ResourceWrapper
) {
    fun mapInfoBackground(media: MediaDomain) =
        if (media.isLiveBroadcastUpcoming) {
            R.color.upcoming_background
        } else if (media.isLiveBroadcast) {
            R.color.live_background
        } else {
            R.color.info_text_overlay_background
        }
}