package uk.co.sentinelweb.cuer.app.ui.common.mapper

import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.PlatformDomain

class PlatformMapper {
    @DrawableRes
    fun mapIcon(platfrom: PlatformDomain): Int = when (platfrom) {
        PlatformDomain.YOUTUBE -> R.drawable.ic_platform_youtube_24_black
        PlatformDomain.WEB -> R.drawable.ic_platfrom_web
        PlatformDomain.VIMEO -> R.drawable.ic_platform_vimeo_24_black
        PlatformDomain.PODCAST -> R.drawable.ic_platform_podcast
        else -> R.drawable.ic_platfrom_web
    }
}